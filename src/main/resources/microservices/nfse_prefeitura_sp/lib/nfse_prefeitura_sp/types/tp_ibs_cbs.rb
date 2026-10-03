# tpIBSCBS
class NfsePrefeituraSp::Types::TpIbsCbs < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:fin_nfse]  = NfsePrefeituraSp::Types::Simple.new('finNFSe', @data[:fin_nfse])
    @children[:ind_final] = NfsePrefeituraSp::Types::Simple.new('indFinal', @data[:ind_final])
    @children[:c_ind_op]  = NfsePrefeituraSp::Types::Simple.new('cIndOp', @data[:c_ind_op])
    @children[:ind_dest]  = NfsePrefeituraSp::Types::Simple.new('indDest', @data[:ind_dest])
    @children[:valores]   = NfsePrefeituraSp::Types::TpValores.new('valores', @data[:valores])
  end

  def add_children_tags_to_xml(xml)
    fin_nfse.add_tag_to_xml(xml)
    ind_final.add_tag_to_xml(xml)
    c_ind_op.add_tag_to_xml(xml)
    ind_dest.add_tag_to_xml(xml)
    valores.add_tag_to_xml(xml)
  end
end
