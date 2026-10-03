# TestePedidoEnvioLoteRPS
class NfsePrefeituraSp::Services::Sync::TesteEnvioLoteRPS
  OPERATION_REQUEST  = :teste_envio_lote_rps
  OPERATION_RESPONSE = :teste_envio_lote_rps_response

  def initialize(data, signer)
    @data   = data
    @signer = signer
    @rps    = NfsePrefeituraSp::Types::TpRps.new('RPS', @data, @signer)
  end

  def request_xml
    builder = Nokogiri::XML::Builder.new(encoding: 'UTF-8') do |xml|
      xml.PedidoEnvioLoteRPS("xmlns" => "http://www.prefeitura.sp.gov.br/nfe") do
        xml.Cabecalho('Versao' => NfsePrefeituraSp.xsd_version.to_s, 'xmlns' => '') do
          xml.CPFCNPJRemetente do
            xml.CNPJ @data[:cnpj_remetente]
          end
          xml.transacao false
          xml.dtInicio @data[:dt_inicio]
          xml.dtFim @data[:dt_fim]
          xml.QtdRPS 1
        end

        @rps.add_tag_to_xml(xml, 'xmlns' => '')
      end
    end

    signed_xml = @signer.sign_xml(builder.to_xml)
    ("<VersaoSchema>#{NfsePrefeituraSp::XSD_VERSION.to_s}</VersaoSchema><MensagemXML>" + "<![CDATA[#{signed_xml}]]>" + "</MensagemXML>").gsub(/\n/,'')
  end
end
