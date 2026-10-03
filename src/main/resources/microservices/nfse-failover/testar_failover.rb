#!/usr/bin/env ruby
# frozen_string_literal: true

# Teste do proxy de failover da NFS-e.
#
# O ponto deste teste e ser CAPAZ de FALHAR. O defeito que ele caça — o proxy so
# ler o corpo quando vinha Content-Length, e o ERP do Spring manda chunked — passa
# em qualquer teste que use curl com --data-binary, porque o curl manda
# Content-Length. Por isso os dois formatos sao obrigatorios, e o chunked vai em
# socket cru: o Net::HTTP so envia chunked quando o corpo e um IO sem tamanho, e
# ai nao da para escolher o conteudo com precisao.
#
#   ruby testar_failover.rb    # exit 0 passou, 1 falhou
#
# Sobe um backend falso que devolve o corpo que recebeu, e o proxy na frente dele.
# Nao toca em porta de producao: as duas portas sao livres, escolhidas na hora.

require "socket"
require "net/http"
require "json"
require "rbconfig"

AQUI = File.expand_path(__dir__)
PROXY = File.join(AQUI, "nfse-failover.rb")

CORPO = %q({"tomador":{"cnpj":"00000000000192"},"servico":{"codigo":"2919"}})
ESPERADO = CORPO.bytesize

def porta_livre
  s = TCPServer.new("127.0.0.1", 0)
  p = s.addr[1]
  s.close
  p
end

# Backend falso: devolve o que recebeu, em JSON, para o teste comparar byte a byte.
def subir_backend(porta)
  server = TCPServer.new("127.0.0.1", porta)
  Thread.new do
    loop do
      cliente = server.accept
      Thread.new(cliente) do |c|
        begin
          linha = c.gets
          next if linha.nil?

          headers = {}
          while (l = c.gets) && l != "\r\n"
            k, v = l.chomp.split(": ", 2)
            headers[k.to_s.downcase] = v if k
          end

          corpo =
            if headers["transfer-encoding"].to_s.downcase.include?("chunked")
              partes = []
              loop do
                tamanho = c.gets.to_s.strip.split(";").first.to_i(16)
                break if tamanho.zero?

                partes << c.read(tamanho)
                c.read(2)
              end
              partes.join
            elsif headers["content-length"]
              c.read(headers["content-length"].to_i)
            else
              ""
            end

          saida = JSON.generate(recebido: corpo, tamanho: corpo.to_s.bytesize)
          c.write("HTTP/1.1 200 OK\r\n")
          c.write("Content-Type: application/json\r\n")
          c.write("Content-Length: #{saida.bytesize}\r\n")
          c.write("Connection: close\r\n\r\n#{saida}")
        rescue StandardError
          nil
        ensure
          begin
            c.close
          rescue StandardError
            nil
          end
        end
      end
    end
  end
  server
end

def postar_com_content_length(porta, caminho, corpo)
  http = Net::HTTP.new("127.0.0.1", porta)
  http.open_timeout = 10
  http.read_timeout = 20
  req = Net::HTTP::Post.new(caminho, "Content-Type" => "application/json")
  req.body = corpo
  JSON.parse(http.request(req).body.to_s)
end

def postar_chunked(porta, caminho, corpo)
  s = TCPSocket.new("127.0.0.1", porta)
  s.write("POST #{caminho} HTTP/1.1\r\n")
  s.write("Host: 127.0.0.1:#{porta}\r\n")
  s.write("Content-Type: application/json\r\n")
  s.write("Transfer-Encoding: chunked\r\n")
  s.write("Connection: close\r\n\r\n")
  s.write("#{corpo.bytesize.to_s(16)}\r\n#{corpo}\r\n")
  s.write("0\r\n\r\n")
  s.flush
  resposta = s.read.to_s
  s.close
  JSON.parse(resposta.split("\r\n\r\n", 2).last.to_s)
end

def get_sem_corpo(porta, caminho)
  http = Net::HTTP.new("127.0.0.1", porta)
  http.open_timeout = 10
  http.read_timeout = 20
  JSON.parse(http.get(caminho).body.to_s)
end

porta_backend = porta_livre
porta_proxy = porta_livre
backend = subir_backend(porta_backend)

proxy = Process.spawn(
  {
    "NFSE_FAILOVER_PORTA" => porta_proxy.to_s,
    "NFSE_FAILOVER_BACKENDS" => "falso=http://127.0.0.1:#{porta_backend}",
    "NFSE_FAILOVER_INTERVALO" => "0.5",
    "NFSE_FAILOVER_TIMEOUT" => "5"
  },
  RbConfig.ruby, PROXY,
  out: File::NULL, err: File::NULL
)

falhas = 0
begin
  # O proxy so escolhe backend depois de uma checagem de saude. Espera ele subir.
  30.times do
    begin
      break if get_sem_corpo(porta_proxy, "/api/nfse-sp/status")
    rescue StandardError
      sleep 0.2
    end
  end

  puts "  o corpo de teste tem #{ESPERADO} bytes"
  puts

  r = postar_com_content_length(porta_proxy, "/api/nfse-sp/emitir-rps", CORPO)
  ok = r["tamanho"] == ESPERADO && r["recebido"] == CORPO
  puts(ok ? "  ok     Content-Length: corpo chegou inteiro (#{r["tamanho"]} bytes)"
          : "  FALHA  Content-Length: esperava #{ESPERADO} bytes, chegou #{r["tamanho"]}")
  falhas += 1 unless ok

  r = postar_chunked(porta_proxy, "/api/nfse-sp/emitir-rps", CORPO)
  ok = r["tamanho"] == ESPERADO && r["recebido"] == CORPO
  puts(ok ? "  ok     chunked: corpo chegou inteiro (#{r["tamanho"]} bytes)"
          : "  FALHA  chunked: esperava #{ESPERADO} bytes, chegou #{r["tamanho"]}")
  falhas += 1 unless ok

  r = get_sem_corpo(porta_proxy, "/api/nfse-sp/status")
  ok = r["tamanho"].to_i.zero?
  puts(ok ? "  ok     GET sem corpo: {\"recebido\" => \"\", \"tamanho\" => 0}"
          : "  FALHA  GET sem corpo: chegou #{r.inspect}")
  falhas += 1 unless ok
ensure
  Process.kill("TERM", proxy) rescue nil
  backend.close rescue nil
end

puts
if falhas.zero?
  puts "  3 de 3. O proxy le o corpo nos dois enquadramentos."
  exit 0
else
  puts "  #{falhas} de 3 falharam."
  exit 1
end
