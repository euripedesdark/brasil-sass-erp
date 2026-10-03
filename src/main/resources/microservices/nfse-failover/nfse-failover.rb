#!/usr/bin/env ruby
# frozen_string_literal: true

# Failover da NFS-e de Sao Paulo.
#
# Escuta na porta publica e repassa para a implementacao que estiver de pe.
# Ordem de preferencia: a API Java (nfse-sp-api) e, quando ela cai, o bridge em
# Ruby. Ambos falam o mesmo contrato em /api/nfse-sp, entao quem chama nao
# percebe a troca.
#
# Por que proxy em vez de "matar o Ruby e subir o Java na mesma porta": trocar
# processo na porta e uma corrida — o processo que sobe pode perder, e durante a
# janela ninguem responde. Com o proxy a troca e instantanea e nao ha disputa
# por porta: cada implementacao fica na sua.
#
#   ruby nfse-failover.rb
#
# Variaveis de ambiente:
#   NFSE_FAILOVER_BACKENDS  "nome=url,nome=url" em ordem de preferencia
#   NFSE_FAILOVER_PORTA     porta publica (padrao 4567)
#   NFSE_FAILOVER_INTERVALO  segundos entre checagens de saude (padrao 10)
require 'socket'
require 'net/http'
require 'uri'
require 'json'

PORTA = Integer(ENV.fetch('NFSE_FAILOVER_PORTA', '4567'))
INTERVALO = Float(ENV.fetch('NFSE_FAILOVER_INTERVALO', '10'))
TIMEOUT_SAUDE = Float(ENV.fetch('NFSE_FAILOVER_TIMEOUT', '3'))
CORPO_MAXIMO = 8 * 1024 * 1024

BACKENDS = ENV.fetch(
  'NFSE_FAILOVER_BACKENDS',
  'java=http://127.0.0.1:4568,ruby=http://127.0.0.1:4569'
).split(',').map do |item|
  nome, url = item.split('=', 2)
  { nome: nome.to_s.strip, url: url.to_s.strip }
end

ESTADO = { ativo: nil, checado_em: nil, ultimo_ok: {} }
MUTEX = Mutex.new

# Cabecalhos repassados ao backend. Host, Connection e Content-Length sao
# recalculados pelo proxy; os demaiskem de ir como vieram.
REPASSAR = %w[content-type authorization accept soapaction user-agent].freeze

def saudavel?(backend)
  uri = URI("#{backend[:url]}/api/nfse-sp/status")
  http = Net::HTTP.new(uri.host, uri.port)
  http.open_timeout = TIMEOUT_SAUDE
  http.read_timeout = TIMEOUT_SAUDE
  http.get(uri.request_uri).is_a?(Net::HTTPSuccess)
rescue StandardError
  false
end

def escolher_backend
  MUTEX.synchronize do
    agora = Time.now
    return ESTADO[:ativo] if ESTADO[:checado_em] && (agora - ESTADO[:checado_em]) < INTERVALO

    ESTADO[:ultimo_ok] = {}
    ativo = nil
    BACKENDS.each do |b|
      ok = saudavel?(b)
      ESTADO[:ultimo_ok][b[:nome]] = ok
      ativo ||= b if ok
    end
    ESTADO[:checado_em] = agora

    if ativo.nil?
      warn '[failover] NENHUMA implementacao disponivel: ' \
           "#{BACKENDS.map { |b| "#{b[:nome]}=#{ESTADO[:ultimo_ok][b[:nome]] ? 'ok' : 'fora'}" }.join(' ')}"
    elsif ESTADO[:ativo].nil?
      warn "[failover] ATIVO: #{ativo[:nome]} (#{ativo[:url]})"
    elsif ESTADO[:ativo][:nome] != ativo[:nome]
      warn "[failover] TROCA: #{ESTADO[:ativo][:nome]} -> #{ativo[:nome]}"
    end

    ESTADO[:ativo] = ativo
  end
end

def responder_erro(cliente, status, texto, rotulo, detalhe)
  corpo = JSON.generate(success: false, error: texto, detail: detalhe)
  cliente.write("HTTP/1.1 #{status}\r\n" \
                "Content-Type: application/json; charset=utf-8\r\n" \
                "X-Backend: #{rotulo}\r\n" \
                "Content-Length: #{corpo.bytesize}\r\n" \
                "Connection: close\r\n\r\n#{corpo}")
end

# O enquadramento e decidido pelo cabecalho, nao assumido. O ERP do Spring manda
# Transfer-Encoding: chunked; so ler Content-Length descartava o corpo e o backend
# recebia um POST vazio, que e um POST valido e por isso nao gerava erro nenhum.
# Ver docs/pesquisa/pesquisa-failover-chunked.md.
def ler_corpo(origem, headers)
  if headers['transfer-encoding'].to_s.downcase.include?('chunked')
    ler_chunked(origem)
  elsif headers['content-length']
    tamanho = headers['content-length'].to_i
    return nil if tamanho.zero?
    return '' if tamanho > CORPO_MAXIMO

    origem.read(tamanho)
  end
end

# Chunked tem prioridade mesmo que Content-Length venha junto: e ele que define o
# enquadramento na linha da malha. O total e limitado porque o proxy le o corpo
# inteiro na memoria e nao deve aceitar tamanho irrestrito de ninguem.
def ler_chunked(origem)
  partes = []
  total = 0
  loop do
    tamanho = origem.gets.to_s.strip.split(';').first.to_i(16)
    break if tamanho.zero?

    total += tamanho
    return nil if total > CORPO_MAXIMO

    partes << origem.read(tamanho)
    origem.read(2)
  end
  partes.join
end

def encaminhar(origem)
  backend = escolher_backend
  return responder_erro(origem, '503 Service Unavailable',
                         'Nenhuma implementacao de NFS-e disponivel.',
                         'nenhum', BACKENDS.map { |b| b[:nome] }.join(',')) if backend.nil?

  linha = origem.gets
  return if linha.nil?

  metodo, caminho, = linha.split(' ', 3)
  headers = {}
  while (l = origem.gets) && l != "\r\n"
    k, v = l.chomp.split(': ', 2)
    headers[k.to_s.downcase] = v if k
  end
  corpo = ler_corpo(origem, headers)

  uri = URI(backend[:url] + caminho.to_s)
  http = Net::HTTP.new(uri.host, uri.port)
  http.open_timeout = 30
  http.read_timeout = 120

  req = Net::HTTPGenericRequest.new(metodo.to_s.upcase, true, true, uri.request_uri)
  REPASSAR.each { |h| req[h] = headers[h] if headers[h] }
  req.body = corpo if corpo

  resposta = http.request(req)

  saida = resposta.body.to_s
  origem.write("HTTP/1.1 #{resposta.code} #{resposta.message}\r\n")
  resposta.each_header do |k, v|
    next if %w[transfer-encoding content-length connection].include?(k.to_s.downcase)

    origem.write("#{k}: #{v}\r\n")
  end
  origem.write("X-Backend: #{backend[:nome]}\r\n")
  origem.write("Content-Length: #{saida.bytesize}\r\n")
  origem.write("Connection: close\r\n\r\n")
  origem.write(saida)
rescue StandardError => e
  warn "[failover] erro ao encaminhar: #{e.class}: #{e.message}"
  begin
    responder_erro(origem, '502 Bad Gateway',
                   'Falha ao falar com a implementacao de NFS-e.', 'erro', e.message)
  rescue StandardError
    nil
  end
ensure
  begin
    origem.close
  rescue StandardError
    nil
  end
end

server = TCPServer.new('0.0.0.0', PORTA)
warn "[failover] escutando em 0.0.0.0:#{PORTA}"
BACKENDS.each { |b| warn "[failover]   #{b[:nome]} -> #{b[:url]}#{' (preferido)' if b.equal?(BACKENDS.first)}" }
escolher_backend

loop do
  origem = server.accept
  Thread.new(origem) { |c| encaminhar(c) }
end
