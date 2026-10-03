# tpGIBSCBS
class NfsePrefeituraSp::Types::TpGIbsCbs < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:c_class_trib] = NfsePrefeituraSp::Types::Simple.new('cClassTrib', @data[:c_class_trib])
  end

  def add_children_tags_to_xml(xml)
    c_class_trib.add_tag_to_xml(xml)
  end
end
