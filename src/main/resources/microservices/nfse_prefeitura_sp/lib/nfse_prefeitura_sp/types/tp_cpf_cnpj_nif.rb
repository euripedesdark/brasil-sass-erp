# tpCPFCNPJNIF
class NfsePrefeituraSp::Types::TpCpfCnpjNif < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:cpf]     = NfsePrefeituraSp::Types::Simple.new('CPF', @data[:cpf])
    @children[:cnpj]    = NfsePrefeituraSp::Types::Simple.new('CNPJ', @data[:cnpj])
    @children[:nif]     = NfsePrefeituraSp::Types::Simple.new('NIF', @data[:nif])
    @children[:nao_nif] = NfsePrefeituraSp::Types::Simple.new('NaoNIF', @data[:nao_nif])
  end

  def add_children_tags_to_xml(xml)
    if !cpf.value.nil?
      cpf.add_tag_to_xml(xml)
    elsif !cnpj.value.nil?
      cnpj.add_tag_to_xml(xml)
    elsif !nif.value.nil?
      nif.add_tag_to_xml(xml)
    elsif !nao_nif.value.nil?
      nao_nif.add_tag_to_xml(xml)
    end
  end
end
