# frozen_string_literal: true

require 'base64'
require 'openssl'
require 'nokogiri'
require 'savon'

module NfsePrefeituraSp
  # Versao do leiaute (XSD) usado nas mensagens.
  #
  # 1 = leiaute classico, ate a entrada em vigor da Reforma Tribitaria.
  # 2 = leiaute 2.x, com os grupos IBS/CBS (LC 214/2025).
  #
  # **A escolha depende do regime tributario do prestador**, e a prefeitura
  # rejeita a mensagem errada com o erro 641:
  #
  #   "Contribuinte cadastrado como Simples Nacional na data informada.
  #    Devera ser utilizado o leiaute 1."
  #
  # Ou seja: empresa do Simples Nacional e OBRIGATORIO o leiaute 1. So o
  # regime normal usa o 2. A escolha nao e do integrador: a prefeitura
  # compara com o cadastro dela.
  #
  # Configuravel por NFSE_SP_XSD_VERSION, porque a gem é compartilhada por
  # emitentes dos dois regimes.
  XSD_VERSION = ENV.fetch('NFSE_SP_XSD_VERSION', '1') == '2' ? 2 : 1

  def self.xsd_version
    XSD_VERSION
  end

  # Carrega o provider "legacy" do OpenSSL.
  #
  # Certificado A1 da ICP-Brasil costuma vir cifrado com RC2-40-CBC ou
  # 3DES, cifras que o OpenSSL 3 removeu do provider default. Sem o legacy,
  # `OpenSSL::PKCS12.new` falha e o servico inteiro nao consegue assinar.
  #
  # Chamar `OpenSSL::Provider.load("legacy")` pelo NOME falha em varias
  # builds com "Failed to load legacy provider: (null)": o OpenSSL procura o
  # modulo em diretorios que ele nem sempre conhece. Por isso procuramos o
  # arquivo e carregamos pelo caminho absoluto.
  LEGACY_PATHS = [
    ENV.fetch('OPENSSL_LEGACY_PATH', nil),
    '/usr/lib/x86_64-linux-gnu/ossl-modules/legacy.so',
    '/usr/lib/aarch64-linux-gnu/ossl-modules/legacy.so',
    '/usr/lib64/ossl-modules/legacy.so',
    '/usr/lib/ossl-modules/legacy.so'
  ].compact.freeze

  def self.load_legacy_provider
    return false unless defined?(OpenSSL::Provider)
    return true if OpenSSL::Cipher.ciphers.any? { |c| c =~ /\Arc2-40/i }

    path = LEGACY_PATHS.find { |p| File.exist?(p) }
    if path
      # Precisa setar OPENSSL_MODULES ANTES do load: em build onde o modulo
      # nao esta no diretorio padrao do OpenSSL, so Provider.load nao basta e
      # o PKCS12 continua falhando com "RC2-40-CBC unsupported".
      ENV['OPENSSL_MODULES'] ||= File.dirname(path)
      OpenSSL::Provider.load(path)
    else
      begin
        OpenSSL::Provider.load('legacy')
      rescue StandardError
        return false
      end
    end

    legacy_cipher_available?
  rescue StandardError
    false
  end

  def self.legacy_cipher_available?
    OpenSSL::Cipher.ciphers.any? { |c| c =~ /\Arc2-40/i }
  rescue StandardError
    false
  end
end

require_relative 'nfse_prefeitura_sp/client'
require_relative 'nfse_prefeitura_sp/response'
require_relative 'nfse_prefeitura_sp/services'
require_relative 'nfse_prefeitura_sp/signer'
require_relative 'nfse_prefeitura_sp/types'
require_relative 'nfse_prefeitura_sp/version'
Dir.glob(File.join(File.expand_path('../', __FILE__), 'nfse_prefeitura_sp/types/**', '*.rb')).each { |file| require_relative(file) }
Dir.glob(File.join(File.expand_path('../', __FILE__), 'nfse_prefeitura_sp/services/**', '*.rb')).each { |file| require_relative(file) }
