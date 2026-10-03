# --- provider legacy do OpenSSL (ANTES de qualquer require) ---------------
# O certificado A1 da ICP-Brasil foi emitido com RC2-40-CBC, cifra que o
# OpenSSL 3 removeu do provider default. Sem o provider "legacy", a leitura
# do .pfx falha e o servico inteiro nao consegue assinar nada.
#
# Isto tem de rodar ANTES do `require 'nfse_prefeitura_sp'`: a gem carrega
# o provider sozinha no initialize do Client, e se o contexto ja estiver
# montado sem o legacy, a cifra RC2 nao aparece mais.
#
# Detalhe: nao basta chamar Provider.load. E preciso apontar
# OPENSSL_MODULES para o diretorio do modulo antes, senao o OpenSSL nao o
# encontra e o PKCS12 falha com "RC2-40-CBC unsupported".
begin
  require 'openssl'
  candidatos = [
    ENV['OPENSSL_LEGACY_PATH'],
    '/usr/lib/x86_64-linux-gnu/ossl-modules/legacy.so',
    '/usr/lib/aarch64-linux-gnu/ossl-modules/legacy.so',
    '/usr/lib64/ossl-modules/legacy.so',
    '/usr/lib/ossl-modules/legacy.so'
  ].compact
  mod = candidatos.find { |p| File.exist?(p) }
  if mod
    ENV['OPENSSL_MODULES'] ||= File.dirname(mod)
    OpenSSL::Provider.load(mod)
  else
    OpenSSL::Provider.load('legacy') rescue nil
  end
rescue => e
  warn "[nfse-sp-bridge] provider legacy nao carregou: #{e.message}"
  warn "[nfse-sp-bridge] certificados em RC2/3DES nao serao lidos; reemita com AES"
end

require 'sinatra'
require 'json'
require 'nfse_prefeitura_sp'

set :port, 4567
set :bind, '0.0.0.0'


# Configuração global do Cliente NFSe SP
def create_client
  NfsePrefeituraSp::Client.new(
    cert_path: ENV['NFSE_SP_CERT_PATH'] || '/etc/brasil-saas/certs/sp_cert.p12',
    cert_password: ENV['NFSE_SP_CERT_PASS'] || 'default_password'
  )
end

helpers do
  def json_response(data, status_code = 200)
    content_type :json
    status status_code
    data.to_json
  end

  # Resposta no contrato unico, compartilhado com a API Java.
  #
  # Ver RespostaNfsePadrao.java. O que mudou aqui, e por que:
  #
  #   antes: { error: { success: true, ... } }  e success em ingles
  #   agora: { sucesso: true, ... }              no primeiro nivel
  #
  # O ERP so lia o formato plano da API Java. Com o fallback ligado, este
  # embrulho fazia a prefeitura emitir a nota e o ERP responder "a prefeitura
  # nao confirmou", marcando a nota como falha sem guardar numero, codigo de
  # verificacao nem chave — que sao o que permite cancelar. Virou as notas 29 e
  # 30, orfas na prefeitura em 26/09/2026.
  #
  # Nao ha segundo formato aceito. Se um dia o ERP precisar ler outra coisa, o
  # problema e no emissor, nao aqui.
  def contrato_nfse(sucesso:, inscricao: '', numero: '', verificacao: '',
                    chave_nacional: '', alertas: [], erro: '', xml: '')
    {
      sucesso: sucesso,
      chave_nfse: inscricao.to_s,
      numero_nfse: numero.to_s,
      codigo_verificacao: verificacao.to_s,
      chave_nota_nacional: chave_nacional.to_s,
      alertas: alertas || [],
      erro: erro.to_s,
      xml_assinado: xml.to_s,
      inscricao_municipal: inscricao.to_s
    }
  end

  def error_response(message, status_code = 400)
    json_response(message, status_code)
  end

  def payload
    JSON.parse(request.body.read, symbolize_names: true)
  end

  # A gem nomeia as chaves de forma especifica (`:cnpj_remetente`), o que e
  # pouco obvio para quem chama a API. Aceitamos os dois nomes.
  def payload_com_alias(hash, alias_map)
    out = hash.dup
    alias_map.each do |amigavel, oficial|
      out[oficial] ||= out[amigavel] if out.key?(amigavel)
    end
    out
  end

  # O retorno da gem vem com chaves geradas por `underscore`, que quebra
  # siglas: "EmiteNFe" vira "emite_n_fe" e "ChaveNFe" vira "chave_n_fe".
  # Traduz para nomes estaveis, que sao o contrato do microservico.
  # Recursivo, porque o `detalhe` da consulta de CNPJ guarda a IM dentro dele.
  def retorno_legivel(retorno)
    case retorno
    when Hash
      retorno.each_with_object({}) do |(k, v), saida|
        saida[apelidar(k.to_s)] = retorno_legivel(v)
      end
    when Array
      retorno.map { |v| retorno_legivel(v) }
    else
      retorno
    end
  end

  def apelidar(chave)
    chave.gsub('_n_fe', '_nfe').gsub('_nfs_e', '_nfse')
  end

  # Navega em hashes aninhados do retorno sem estourar NoMethodError quando
  # um nivel intermediate falta.
  # retorno_legivel devolve chave como STRING (apelidar recebe k.to_s), entao
  # ler com simbolo devolve nil e a resposta sai vazia — o que esconde numero e
  # codigo de verificacao, justamente o que o ERP precisa para registrar e
  # cancelar a nota.
  def campo(hash, chave)
    return nil unless hash.is_a?(Hash)
    hash[chave.to_sym] || hash[chave.to_s]
  end

  def dig_retorno(obj, *chaves)
    chaves.inject(obj) do |atual, chave|
      return nil unless atual.is_a?(Hash)
      atual[chave] || atual[chave.to_s]
    end
  end

  def responder(response, mensagem_de_falha)
    if response.success?
      json_response({ success: true, retorno: retorno_legivel(response.retorno) })
    else
      error_response(response.errors || mensagem_de_falha, 422)
    end
  end
end

# ===========================================================================
# Fallback: as mesmas rotas da API Java (nfse-sp-api)
#
# O failover precisa que os dois servicos falem o MESMO contrato, senao o
# proxy nao consegue trocar um pelo outro sem traduzir payload no caminho.
# Estas rotas entao aceitam o corpo no formato do Java e devolvem a resposta no
# mesmo formato, de modo que quem chama nao sabe qual dos dois atendeu.
# ===========================================================================

#
# O JSON chega com as chaves em camelCase (ex.: "imPrestador") e
# `symbolize_names: true` PRESERVA essa grafia — vira :imPrestador, nao
# :im_prestador. Sem normalizar, toda traducao devolve nil e a gem monta um
# RPS sem ChaveRPS.
def chave_snake(chave)
  chave.to_s
        .gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2')
        .gsub(/([a-z\d])([A-Z])/, '\1_\2')
        .tr('-', '_')
        .downcase
        .to_sym
end

def normalizar_keys(hash)
  return hash unless hash.is_a?(Hash)

  hash.each_with_object({}) { |(k, v), acc| acc[chave_snake(k)] = v }
end

# Payload do Java (camelCase) -> payload da gem (snake_case).
def rps_para_gem(bruto)
  p = normalizar_keys(bruto)
  cpf = p[:cpf_tomador]
  cnpj = p[:cnpj_tomador]
  {
    cnpj_remetente: ENV.fetch('NFSE_SP_CNPJ', ''),
    chave_rps: {
      inscricao_prestador: p[:im_prestador].to_s,
      serie_rps: p[:serie_rps].to_s,
      numero_rps: p[:numero_rps].to_s
    },
    tipo_rps: p[:tipo_rps] || 'RPS',
    data_emissao: p[:data_emissao],
    status_rps: p[:status_rps] || 'N',
    tributacao_rps: p[:tributacao_rps].to_s,
    valor_servicos: p[:valor_servicos].to_s,
    valor_deducoes: (p[:valor_deducoes] || '0.00').to_s,
    valor_pis: (p[:valor_pis] || '0.00').to_s,
    valor_cofins: (p[:valor_cofins] || '0.00').to_s,
    valor_inss: (p[:valor_inss] || '0.00').to_s,
    valor_ir: (p[:valor_ir] || '0.00').to_s,
    valor_csll: (p[:valor_csll] || '0.00').to_s,
    codigo_servico: p[:codigo_servico].to_s,
    aliquota_servicos: (p[:aliquota_servicos] || '0.029').to_s,
    iss_retido: p[:iss_retido] ? 'true' : 'false',
    cpf_cnpj_tomador: if cpf && !cpf.to_s.strip.empty?
                        { cpf: cpf.to_s }
                      elsif cnpj && !cnpj.to_s.strip.empty?
                        { cnpj: cnpj.to_s }
                      else
                        { nao_nif: 'false' }
                      end,
    razao_social_tomador: p[:razao_social_tomador],
    email_tomador: p[:email_tomador],
    discriminacao: p[:discriminacao]
  }.compact
end

post '/api/nfse-sp/consulta-cnpj' do
  begin
    p = normalizar_keys(payload)
    r = create_client.sync_consulta_cnpj(
      cnpj_remetente: ENV.fetch('NFSE_SP_CNPJ', ''),
      cnpj_contribuinte: p[:cnpj] || p[:cnpj_contribuinte]
    )
    leg = retorno_legivel(r.retorno)
    d = campo(leg, :detalhe) || {}
    # No mesmo contrato da emissao. A consulta de CNPJ tambem precisa falar a
    # mesma lingua, porque e ela que o watchdog usa para decidir se esta
    # implementacao serve. O .merge so acrescenta o emite_nfse, que e especifico
    # desta rota e nao existe no contrato de emissao.
    json_response(contrato_nfse(
      sucesso: r.success?,
      inscricao: campo(d, :inscricao_municipal),
      alertas: campo(leg, :alerta) || []
    ).merge(emite_nfse: campo(d, :emite_nfe).to_s == 'true'))
  rescue => e
    error_response("Erro interno no serviço de NFSe SP: #{e.message}", 500)
  end
end

post '/api/nfse-sp/emitir-rps' do
  begin
    dados = rps_para_gem(payload)

    # A gem nao devolve o XML assinado, e o ERP precisa dele para arquivar
    # (prazo legal de 5 anos). Montando o servico aqui, o request_xml ja vem
    # assinado e pode seguir na resposta.
    client = create_client
    signer = client.instance_variable_get(:@signer)
    servico = NfsePrefeituraSp::Services::Sync::EnvioRps.new(dados, signer)
    xml_assinado = servico.request_xml

    r = client.send(:call_service, servico)

    if r.success?
      # retorno_legivel antes do dig: a chave vem como :chave_n_fe_rps, e nao
      # :chave_nfe_rps. Sem o apelidador o dig volta nil e a resposta sai com
      # numero e codigo de verificacao vazios — que e exatamente o que impede
      # o ERP de registrar e cancelar a nota.
      legivel = retorno_legivel(r.retorno)
      chave = dig_retorno(legivel, :chave_nfe_rps, :chave_nfe)
      json_response(contrato_nfse(
        sucesso: true,
        inscricao: campo(chave, :inscricao_prestador),
        numero: campo(chave, :numero_nfe),
        verificacao: campo(chave, :codigo_verificacao),
        chave_nacional: campo(chave, :chave_nota_nacional),
        alertas: campo(legivel, :alerta) || [],
        xml: xml_assinado
      ))
    else
      json_response(contrato_nfse(sucesso: false, erro: r.errors || 'Erro ao emitir RPS'), 422)
    end
  rescue => e
    error_response("Erro interno no serviço de NFSe SP: #{e.message}", 500)
  end
end

post '/api/nfse-sp/cancelar' do
  begin
    p = normalizar_keys(payload)
    detalhes = (p[:detalhes] || []).map do |bruto|
      d = normalizar_keys(bruto)
      c = d[:chave_nfe] ? normalizar_keys(d[:chave_nfe]) : d
      {
        chave_nfe: {
          inscricao_prestador: c[:inscricao_prestador].to_s,
          numero_nfe: c[:numero_nfe].to_s,
          codigo_verificacao: c[:codigo_verificacao],
          chave_nota_nacional: c[:chave_nota_nacional]
        }
      }.compact
    end

    r = create_client.sync_cancelamento_nfe(
      cnpj_remetente: ENV.fetch('NFSE_SP_CNPJ', ''), detalhes: detalhes
    )
    json_response(contrato_nfse(
      sucesso: r.success?,
      alertas: retorno_legivel(r.retorno)[:alerta] || [],
      erro: r.success? ? '' : (r.errors || 'Erro ao cancelar')
    ))
  rescue => e
    error_response("Erro interno no serviço de NFSe SP: #{e.message}", 500)
  end
end

get '/api/nfse-sp/status' do
  # No mesmo contrato, porque e este endpoint que o watchdog consulta. Se o
  # status falar outro idioma, o watchdog nao consegue dizer se esta
  # implementacao serve — e foi assim que a nota 29 escapou.
  json_response(contrato_nfse(sucesso: true).merge(
    implementacao: 'ruby-bridge',
    papel: 'fallback',
    cnpj: ENV.fetch('NFSE_SP_CNPJ', ''),
    leiaute_xsd: NfsePrefeituraSp.xsd_version
  ))
end

# ===========================================================================
# Contrato original do bridge, mantido para quem ja chama direto na 4569.
# Quem passa pelo failover usa as rotas /api/nfse-sp acima.
# ===========================================================================

# Endpoint para Consulta de CNPJ
post '/consulta_cnpj' do
  begin
    dados = payload_com_alias(
      payload,
      cnpj: :cnpj_remetente, cnpj_remetente: :cnpj_contribuinte
    )
    responder(create_client.sync_consulta_cnpj(dados), 'Erro ao consultar CNPJ')
  rescue => e
    error_response("Erro interno no serviço de NFSe SP: #{e.message}", 500)
  end
end

# Endpoint para Envio de RPS (Síncrono)
post '/emitir_rps' do
  begin
    dados = payload_com_alias(payload, cnpj: :cnpj_remetente, cnpj_prestador: :cnpj_remetente)
    response = create_client.sync_envio_rps(dados)

    if response.success?
      r = retorno_legivel(response.retorno)
      # A prefeitura devolve a chave aninhada em
      # chave_nfe_rps > chave_nfe > chave_nota_nacional
      chave = dig_retorno(r, :chave_nfe_rps, :chave_nfe, :chave_nota_nacional) ||
              dig_retorno(r, :chave_nfe)
      json_response({ success: true, retorno: r, chave_nfe: chave })
    else
      error_response(response.errors || 'Erro ao emitir RPS', 422)
    end
  rescue => e
    error_response("Erro interno no serviço de NFSe SP: #{e.message}", 500)
  end
end

# Endpoint para Cancelamento de NF-e
post '/cancelar_nfse' do
  begin
    dados = payload_com_alias(payload, cnpj: :cnpj_remetente, cnpj_prestador: :cnpj_remetente)
    responder(create_client.sync_cancelamento_nfe(dados), 'Erro ao cancelar NFSe')
  rescue => e
    error_response("Erro interno no serviço de NFSe SP: #{e.message}", 500)
  end
end
