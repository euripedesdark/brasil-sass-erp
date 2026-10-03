# tpTrib
class NfsePrefeituraSp::Types::TpTrib < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:g_ibs_cbs] = NfsePrefeituraSp::Types::TpGIbsCbs.new('gIBSCBS', @data[:g_ibs_cbs])
  end

  def add_children_tags_to_xml(xml)
    g_ibs_cbs.add_tag_to_xml(xml)
  end
end
