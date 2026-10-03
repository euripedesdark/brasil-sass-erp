# tpChaveNFe
class NfsePrefeituraSp::Types::TpChaveNfe < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:inscricao_prestador] = NfsePrefeituraSp::Types::Simple.new('InscricaoPrestador', @data[:inscricao_prestador])
    @children[:numero_nfe]          = NfsePrefeituraSp::Types::Simple.new('NumeroNFe', @data[:numero_nfe])
    @children[:codigo_verificacao]  = NfsePrefeituraSp::Types::Simple.new('CodigoVerificacao', @data[:codigo_verificacao]) if @data[:codigo_verificacao]
    @children[:chave_nota_nacional] = NfsePrefeituraSp::Types::Simple.new('ChaveNotaNacional', @data[:chave_nota_nacional]) if @data[:chave_nota_nacional]
  end

  def add_children_tags_to_xml(xml)
    inscricao_prestador.add_tag_to_xml(xml)
    numero_nfe.add_tag_to_xml(xml)
    codigo_verificacao.add_tag_to_xml(xml) if @data[:codigo_verificacao]
    chave_nota_nacional.add_tag_to_xml(xml) if @data[:chave_nota_nacional]
  end
end
