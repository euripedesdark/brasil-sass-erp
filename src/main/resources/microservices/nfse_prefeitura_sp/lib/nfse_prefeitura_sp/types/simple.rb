module NfsePrefeituraSp::Types
  class Simple
    attr_accessor :key, :value

    def initialize(key, value)
      @key   = key
      @value = value
    end

    # String vazia conta como ausente. A prefeitura valida o conteudo
    # contra o XSD e um <InscricaoMunicipalTomador></...> vazio quebra:
    #   "The 'InscricaoMunicipalTomador' element is invalid - The value '' is
    #    invalid according to its datatype 'tipos:tpInscricaoMunicipal'"
    # Campo opcional que o chamador nao preencheu e exatamente o caso de
    # quem deve ser omitido, nao emitido vazio.
    def has_any_child_value?
      !blank?
    end

    def add_tag_to_xml(xml)
      xml.send(@key, @value) unless blank?
    end

    private

    def blank?
      @value.nil? || (@value.is_a?(String) && @value.strip.empty?)
    end
  end
end
