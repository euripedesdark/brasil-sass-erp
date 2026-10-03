# tpValores
class NfsePrefeituraSp::Types::TpValores < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:trib] = NfsePrefeituraSp::Types::TpTrib.new('trib', @data[:trib])
  end

  def add_children_tags_to_xml(xml)
    trib.add_tag_to_xml(xml)
  end
end
