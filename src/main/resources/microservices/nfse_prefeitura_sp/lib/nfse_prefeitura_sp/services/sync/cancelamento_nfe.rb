# PedidoCancelamentoNFe
class NfsePrefeituraSp::Services::Sync::CancelamentoNfe
  OPERATION_REQUEST  = :cancelamento_n_fe
  OPERATION_RESPONSE = :cancelamento_n_fe_response

  def initialize(data, signer)
    @data   = data
    @signer = signer
  end

  def request_xml
    builder = Nokogiri::XML::Builder.new(encoding: 'UTF-8') do |xml|
      xml.PedidoCancelamentoNFe("xmlns" => "http://www.prefeitura.sp.gov.br/nfe") do
        xml.Cabecalho('Versao' => NfsePrefeituraSp.xsd_version.to_s, 'xmlns' => '') do
          xml.CPFCNPJRemetente do
            xml.CNPJ @data[:cnpj_remetente]
          end
          xml.transacao (@data[:transacao] == false ? false : true)
        end

        Array(@data[:detalhes]).each do |detalhe|
          xml.Detalhe('xmlns' => '') do
            NfsePrefeituraSp::Types::TpChaveNfe.new('ChaveNFe', detalhe[:chave_nfe]).add_tag_to_xml(xml)

            # Cadeia de caracteres (com informações do RPS emitido) assinada conforme descrito no item 4.3.10.
            inscricao_prestador = detalhe[:chave_nfe][:inscricao_prestador].to_s.rjust(8, '0')
            numero              = detalhe[:chave_nfe][:numero_nfe].to_s.rjust(12, '0')
            str_to_sign         = "#{inscricao_prestador}#{numero}"

            xml.AssinaturaCancelamento @signer.sign(str_to_sign)
          end
        end
      end
    end

    signed_xml = @signer.sign_xml(builder.to_xml)
    ("<VersaoSchema>#{NfsePrefeituraSp::XSD_VERSION.to_s}</VersaoSchema><MensagemXML>" + "<![CDATA[#{signed_xml}]]>" + "</MensagemXML>").gsub(/\n/,'')
  end
end
