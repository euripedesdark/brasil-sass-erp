module NfsePrefeituraSp::Types
  class Complex
    def initialize(key, data, signer = nil)
      @key      = key
      @data     = data || {}
      @data     = (@data.respond_to?('with_indifferent_access') ? @data.with_indifferent_access : @data)
      @signer   = signer
      @children = {}

      initialize_children
      @children.each { |key, child| define_singleton_method(key) { child } }
    end

    def initialize_children; end

    def has_any_child_value?
      @children.find { |_, child| child.has_any_child_value? }
    end

    def add_tag_to_xml(xml, attrs = {})
      if has_any_child_value?
        xml.send(@key, attrs) do
          add_children_tags_to_xml(xml)
        end
      end
    end

    def add_children_tags_to_xml
      raise "[NfsePrefeituraSp] Method not implemented error."
    end
  end
end
