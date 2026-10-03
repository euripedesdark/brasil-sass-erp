# tpEndereco
class NfsePrefeituraSp::Types::TpEndereco < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:tipo_logradouro]      = NfsePrefeituraSp::Types::Simple.new('TipoLogradouro', @data[:tipo_logradouro])
    @children[:logradouro]           = NfsePrefeituraSp::Types::Simple.new('Logradouro', @data[:logradouro])
    @children[:numero_endereco]      = NfsePrefeituraSp::Types::Simple.new('NumeroEndereco', @data[:numero_endereco])
    @children[:complemento_endereco] = NfsePrefeituraSp::Types::Simple.new('ComplementoEndereco', @data[:complemento_endereco])
    @children[:bairro]               = NfsePrefeituraSp::Types::Simple.new('Bairro', @data[:bairro])
    @children[:cidade]               = NfsePrefeituraSp::Types::Simple.new('Cidade', @data[:cidade])
    @children[:uf]                   = NfsePrefeituraSp::Types::Simple.new('UF', @data[:uf])
    @children[:cep]                  = NfsePrefeituraSp::Types::Simple.new('CEP', @data[:cep])
  end

  def add_children_tags_to_xml(xml)
    tipo_logradouro.add_tag_to_xml(xml)
    logradouro.add_tag_to_xml(xml)
    numero_endereco.add_tag_to_xml(xml)
    complemento_endereco.add_tag_to_xml(xml)
    bairro.add_tag_to_xml(xml)
    cidade.add_tag_to_xml(xml)
    uf.add_tag_to_xml(xml)
    cep.add_tag_to_xml(xml)
  end
end
