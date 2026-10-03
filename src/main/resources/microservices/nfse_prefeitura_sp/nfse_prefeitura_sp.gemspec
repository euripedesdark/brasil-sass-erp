# frozen_string_literal: true

require_relative "lib/nfse_prefeitura_sp/version"

Gem::Specification.new do |spec|
  spec.name = "nfse_prefeitura_sp"
  spec.version = NfsePrefeituraSp::VERSION
  spec.authors = ["https://www.infosimples.com.br"]
  spec.email = ["contato@infosimples.com.br"]

  spec.summary = "Gem para integração com a Nota Fiscal de Serviços Eletrônica (NFS-e) da Prefeitura de São Paulo."
  spec.homepage = "https://github.com/infosimples/nfse_prefeitura_sp"
  spec.license = "MIT"
  spec.required_ruby_version = ">= 3.1.0"

  # Specify which files should be added to the gem when it is released.
  # The `git ls-files -z` loads the files in the RubyGem that have been added into git.
  gemspec = File.basename(__FILE__)
  spec.files = IO.popen(%w[git ls-files -z], chdir: __dir__, err: IO::NULL) do |ls|
    ls.readlines("\x0", chomp: true).reject do |f|
      (f == gemspec) ||
        f.start_with?(*%w[bin/ test/ spec/ features/ .git appveyor Gemfile])
    end
  end
  spec.require_paths = ["lib"]

  # Uncomment to register a new dependency of your gem
  spec.add_dependency "openssl", "~> 3.3.0"
  spec.add_dependency "nokogiri", "~> 1.18.0"
  spec.add_dependency "savon", "~> 3.0.0.rc2"
end
