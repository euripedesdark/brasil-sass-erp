# Gera o mesmo XML do RPS e da consulta de CNPJ pela gem Ruby, para comparar
# com o que a API Java produz. Escrito com os MESMOS dados do teste Java
# NfseSpXmlAssinadoDumpTest, para o diff ser justo.
$LOAD_PATH.unshift File.expand_path(
  File.join(__dir__, '..', 'nfse_prefeitura_sp', 'lib')
)
ENV['OPENSSL_MODULES'] ||= '/usr/lib/x86_64-linux-gnu/ossl-modules'
require 'openssl'
OpenSSL::Provider.load('/usr/lib/x86_64-linux-gnu/ossl-modules/legacy.so')
require 'nfse_prefeitura_sp'
require 'fileutils'

CERT = ENV['NFSE_SP_CERT_PATH']
SENHA = ENV['NFSE_SP_CERT_PASS']
SAIDA = '/tmp/opencode/comparacao'
FileUtils.mkdir_p(SAIDA)

client = NfsePrefeituraSp::Client.new(cert_path: CERT, cert_password: SENHA)
signer = client.instance_variable_get(:@signer)

# ---- RPS ----
dados = {
  cnpj_remetente: '00000000000191',
  chave_rps: { inscricao_prestador: '2130033', serie_rps: 'BC', numero_rps: '1' },
  tipo_rps: 'RPS',
  data_emissao: '2026-09-25',
  status_rps: 'N',
  tributacao_rps: '1',
  valor_servicos: '1.00',
  valor_deducoes: '0.00',
  valor_pis: '0.00', valor_cofins: '0.00', valor_inss: '0.00',
  valor_ir: '0.00', valor_csll: '0.00',
  codigo_servico: '0101',
  aliquota_servicos: '0.029',
  iss_retido: 'false',
  cpf_cnpj_tomador: { cpf: '86946749120' },
  razao_social_tomador: 'EURIPEDES BATISTA DE PAIVA JUNIOR',
  email_tomador: 'euripededark@gmail.com',
  discriminacao: 'TESTE DE INTEGRACAO - NOTA DE R$ 1,00 A SER CANCELADA'
}

rps_xml = NfsePrefeituraSp::Services::Sync::EnvioRps.new(dados, signer).request_xml
File.write("#{SAIDA}/ruby-rps.xml", rps_xml)

# ---- Consulta de CNPJ ----
cnpj_xml = NfsePrefeituraSp::Services::Sync::ConsultaCnpj.new(
  { cnpj_remetente: '00000000000191', cnpj_contribuinte: '00000000000191' }, signer
).request_xml
File.write("#{SAIDA}/ruby-consulta-cnpj.xml", cnpj_xml)

puts "ruby-rps.xml           #{rps_xml.bytesize} bytes"
puts "ruby-consulta-cnpj.xml #{cnpj_xml.bytesize} bytes"
