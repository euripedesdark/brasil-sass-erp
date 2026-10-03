# tpChaveRPS
class NfsePrefeituraSp::Types::TpChaveRps < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:inscricao_prestador] = NfsePrefeituraSp::Types::Simple.new('InscricaoPrestador', @data[:inscricao_prestador])
    @children[:serie_rps]           = NfsePrefeituraSp::Types::Simple.new('SerieRPS', @data[:serie_rps])
    @children[:numero_rps]          = NfsePrefeituraSp::Types::Simple.new('NumeroRPS', @data[:numero_rps])
  end

  def add_children_tags_to_xml(xml)
    inscricao_prestador.add_tag_to_xml(xml)
    serie_rps.add_tag_to_xml(xml)
    numero_rps.add_tag_to_xml(xml)
  end
end
