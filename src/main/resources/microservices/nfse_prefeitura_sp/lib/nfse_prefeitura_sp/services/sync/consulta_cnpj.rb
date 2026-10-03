# PedidoConsultaCNPJ
class NfsePrefeituraSp::Services::Sync::ConsultaCnpj
  OPERATION_REQUEST  = :consulta_cnpj
  OPERATION_RESPONSE = :consulta_cnpj_response

  def initialize(data, signer)
    @data   = data
    @signer = signer
  end

  def request_xml
    builder = Nokogiri::XML::Builder.new(encoding: 'UTF-8') do |xml|
      xml.PedidoConsultaCNPJ("xmlns" => "http://www.prefeitura.sp.gov.br/nfe") do
        xml.Cabecalho('Versao' => NfsePrefeituraSp.xsd_version.to_s, 'xmlns' => '') do
          xml.CPFCNPJRemetente do
            xml.CNPJ @data[:cnpj_remetente]
          end
        end

        xml.CNPJContribuinte('xmlns' => '') do
          xml.CNPJ @data[:cnpj_contribuinte]
        end
      end
    end

    signed_xml = @signer.sign_xml(builder.to_xml)
    ("<VersaoSchema>#{NfsePrefeituraSp::XSD_VERSION.to_s}</VersaoSchema><MensagemXML>" + "<![CDATA[#{signed_xml}]]>" + "</MensagemXML>").gsub(/\n/,'')
  end
end
