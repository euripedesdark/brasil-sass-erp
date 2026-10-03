# tpCPFCNPJ
class NfsePrefeituraSp::Types::TpCpfCnpj < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:cpf]  = NfsePrefeituraSp::Types::Simple.new('CPF', @data[:cpf])
    @children[:cnpj] = NfsePrefeituraSp::Types::Simple.new('CNPJ', @data[:cnpj])
  end

  def add_children_tags_to_xml(xml)
    if !cpf.nil?
      cpf.add_tag_to_xml(xml)
    elsif !cnpj.nil?
      cnpj.add_tag_to_xml(xml)
    end
  end
end
