# tpRPS
class NfsePrefeituraSp::Types::TpRps < NfsePrefeituraSp::Types::Complex
  def initialize_children
    @children[:chave_rps]                         = NfsePrefeituraSp::Types::TpChaveRps.new('ChaveRPS', @data[:chave_rps])
    @children[:tipo_rps]                          = NfsePrefeituraSp::Types::Simple.new('TipoRPS', @data[:tipo_rps])
    @children[:data_emissao]                      = NfsePrefeituraSp::Types::Simple.new('DataEmissao', @data[:data_emissao])
    @children[:status_rps]                        = NfsePrefeituraSp::Types::Simple.new('StatusRPS', @data[:status_rps])
    @children[:tributacao_rps]                    = NfsePrefeituraSp::Types::Simple.new('TributacaoRPS', @data[:tributacao_rps])
    # Obrigatorio no XSD v01 (minOccurs=1). No leiaute 2 nao existe:
    # emitir la gera "The element 'RPS' has invalid child element 'ValorServicos'"
    @children[:valor_servicos]                    = NfsePrefeituraSp::Types::Simple.new('ValorServicos', @data[:valor_servicos])
    # ValorServicos e obrigatório no XSD (minOccurs=1) e precisa vir logo
    # depois de TributacaoRPS. A gem nao emitia o campo, entao TODA nota era
    # rejeitada com:
    #   "The element 'RPS' is not expected" / "ValorServicos is missing"
    @children[:valor_deducoes]                    = NfsePrefeituraSp::Types::Simple.new('ValorDeducoes', @data[:valor_deducoes])
    @children[:valor_pis]                         = NfsePrefeituraSp::Types::Simple.new('ValorPIS', @data[:valor_pis])
    @children[:valor_cofins]                      = NfsePrefeituraSp::Types::Simple.new('ValorCOFINS', @data[:valor_cofins])
    @children[:valor_inss]                        = NfsePrefeituraSp::Types::Simple.new('ValorINSS', @data[:valor_inss])
    @children[:valor_ir]                          = NfsePrefeituraSp::Types::Simple.new('ValorIR', @data[:valor_ir])
    @children[:valor_csll]                        = NfsePrefeituraSp::Types::Simple.new('ValorCSLL', @data[:valor_csll])
    @children[:codigo_servico]                    = NfsePrefeituraSp::Types::Simple.new('CodigoServico', @data[:codigo_servico])
    @children[:aliquota_servicos]                 = NfsePrefeituraSp::Types::Simple.new('AliquotaServicos', @data[:aliquota_servicos])
    @children[:iss_retido]                        = NfsePrefeituraSp::Types::Simple.new('ISSRetido', @data[:iss_retido])
    @children[:cpf_cnpj_tomador]                  = NfsePrefeituraSp::Types::TpCpfCnpjNif.new('CPFCNPJTomador', @data[:cpf_cnpj_tomador])
    @children[:inscricao_municipal_tomador]       = NfsePrefeituraSp::Types::Simple.new('InscricaoMunicipalTomador', @data[:inscricao_municipal_tomador])
    @children[:inscricao_estadual_tomador]        = NfsePrefeituraSp::Types::Simple.new('InscricaoEstadualTomador', @data[:inscricao_estadual_tomador])
    @children[:razao_social_tomador]              = NfsePrefeituraSp::Types::Simple.new('RazaoSocialTomador', @data[:razao_social_tomador])
    @children[:endereco_tomador]                  = NfsePrefeituraSp::Types::TpEndereco.new('EnderecoTomador', @data[:endereco_tomador])
    @children[:email_tomador]                     = NfsePrefeituraSp::Types::Simple.new('EmailTomador', @data[:email_tomador])
    @children[:cpf_cnpj_intermediario]            = NfsePrefeituraSp::Types::TpCpfCnpj.new('CPFCNPJIntermediario', @data[:cpf_cnpj_intermediario])
    @children[:inscricao_municipal_intermediario] = NfsePrefeituraSp::Types::Simple.new('InscricaoMunicipalIntermediario', @data[:inscricao_municipal_intermediario])
    @children[:iss_retido_intermediario]          = NfsePrefeituraSp::Types::Simple.new('ISSRetidoIntermediario', @data[:iss_retido_intermediario])
    @children[:email_intermediario]               = NfsePrefeituraSp::Types::Simple.new('EmailIntermediario', @data[:email_intermediario])
    @children[:discriminacao]                     = NfsePrefeituraSp::Types::Simple.new('Discriminacao', @data[:discriminacao])
    @children[:valor_carga_tributaria]            = NfsePrefeituraSp::Types::Simple.new('ValorCargaTributaria', @data[:valor_carga_tributaria])
    @children[:percentual_carga_tributaria]       = NfsePrefeituraSp::Types::Simple.new('PercentualCargaTributaria', @data[:percentual_carga_tributaria])
    @children[:fonte_carga_tributaria]            = NfsePrefeituraSp::Types::Simple.new('FonteCargaTributaria', @data[:fonte_carga_tributaria])
    @children[:codigo_cei]                        = NfsePrefeituraSp::Types::Simple.new('CodigoCEI', @data[:codigo_cei])
    @children[:matricula_obra]                    = NfsePrefeituraSp::Types::Simple.new('MatriculaObra', @data[:matricula_obra])
    @children[:municipio_prestacao]               = NfsePrefeituraSp::Types::Simple.new('MunicipioPrestacao', @data[:municipio_prestacao])
    # ORDEM IMPORTA: o XSD da prefeitura e um <xs:sequence>, nao <xs:all>.
    # NumeroEncapsulamento tem de vir ANTES de ValorTotalRecebido e
    # RetencaoPisCofins. Emitido depois, a prefeitura responde:
    #   "The element 'RPS' has invalid child element 'NumeroEncapsulamento'.
    #    List of possible elements expected: 'RetencaoPisCofins, ...'"
    @children[:numero_encapsulamento]             = NfsePrefeituraSp::Types::Simple.new('NumeroEncapsulamento', @data[:numero_encapsulamento])
    @children[:valor_total_recebido]              = NfsePrefeituraSp::Types::Simple.new('ValorTotalRecebido', @data[:valor_total_recebido])
    @children[:retencao_pis_cofins]               = NfsePrefeituraSp::Types::Simple.new('RetencaoPisCofins', @data[:retencao_pis_cofins])
    @children[:valor_inicial_cobrado]             = NfsePrefeituraSp::Types::Simple.new('ValorInicialCobrado', @data[:valor_inicial_cobrado])
    @children[:valor_final_cobrado]               = NfsePrefeituraSp::Types::Simple.new('ValorFinalCobrado', @data[:valor_final_cobrado])
    @children[:valor_multa]                       = NfsePrefeituraSp::Types::Simple.new('ValorMulta', @data[:valor_multa])
    @children[:valor_juros]                       = NfsePrefeituraSp::Types::Simple.new('ValorJuros', @data[:valor_juros])
    @children[:valor_ipi]                         = NfsePrefeituraSp::Types::Simple.new('ValorIPI', @data[:valor_ipi])
    @children[:exigibilidade_suspensa]            = NfsePrefeituraSp::Types::Simple.new('ExigibilidadeSuspensa', @data[:exigibilidade_suspensa])
    @children[:ncm]                               = NfsePrefeituraSp::Types::Simple.new('NCM', @data[:ncm])
    @children[:nbs]                               = NfsePrefeituraSp::Types::Simple.new('NBS', @data[:nbs])
    @children[:c_loc_prestacao]                   = NfsePrefeituraSp::Types::Simple.new('cLocPrestacao', @data[:c_loc_prestacao])
    @children[:c_pais_prestacao]                  = NfsePrefeituraSp::Types::Simple.new('cPaisPrestacao', @data[:c_pais_prestacao])
    @children[:ibs_cbs]                           = NfsePrefeituraSp::Types::TpIbsCbs.new('IBSCBS', @data[:ibs_cbs])
    @children[:assinatura]                        = NfsePrefeituraSp::Types::Simple.new('Assinatura', generate_signature)

    # Versão 3.3.6 - 14/05/2026
    # O campo PagamentoParceladoAntecipado não será mais utilizado e os valores enviados serão desconsiderados.
    # @children[:pagamento_parcelado_antecipado]    = NfsePrefeituraSp::Types::Simple.new('PagamentoParceladoAntecipado', @data[:pagamento_parcelado_antecipado])
  end

  # Tags do leiaute 2.x (IBS/CBS, LC 214/2025). Nao existem no XSD v01, e
  # a prefeitura rejeita com "The element 'RPS' has invalid child element".
  # So sao emitidas quando NFSE_SP_XSD_VERSION=2.
  def add_tags_layout2(xml)
    valor_multa.add_tag_to_xml(xml)
    valor_juros.add_tag_to_xml(xml)
    valor_ipi.add_tag_to_xml(xml)
    exigibilidade_suspensa.add_tag_to_xml(xml)
    # pagamento_parcelado_antecipado.add_tag_to_xml(xml) # Ver nota acima (Versão 3.3.6 - 14/05/2026).
    ncm.add_tag_to_xml(xml)
    nbs.add_tag_to_xml(xml)

    if !c_loc_prestacao.value.nil?
      c_loc_prestacao.add_tag_to_xml(xml)
    else
      c_pais_prestacao.add_tag_to_xml(xml)
    end

    ibs_cbs.add_tag_to_xml(xml)
  end

  def add_children_tags_to_xml(xml)
    assinatura.add_tag_to_xml(xml)
    chave_rps.add_tag_to_xml(xml)
    tipo_rps.add_tag_to_xml(xml)
    data_emissao.add_tag_to_xml(xml)
    status_rps.add_tag_to_xml(xml)
    tributacao_rps.add_tag_to_xml(xml)
    valor_servicos.add_tag_to_xml(xml)
    valor_deducoes.add_tag_to_xml(xml)
    valor_pis.add_tag_to_xml(xml)
    valor_cofins.add_tag_to_xml(xml)
    valor_inss.add_tag_to_xml(xml)
    valor_ir.add_tag_to_xml(xml)
    valor_csll.add_tag_to_xml(xml)
    codigo_servico.add_tag_to_xml(xml)
    aliquota_servicos.add_tag_to_xml(xml)
    iss_retido.add_tag_to_xml(xml)
    cpf_cnpj_tomador.add_tag_to_xml(xml)
    inscricao_municipal_tomador.add_tag_to_xml(xml)
    inscricao_estadual_tomador.add_tag_to_xml(xml)
    razao_social_tomador.add_tag_to_xml(xml)
    endereco_tomador.add_tag_to_xml(xml)
    email_tomador.add_tag_to_xml(xml)
    cpf_cnpj_intermediario.add_tag_to_xml(xml)
    inscricao_municipal_intermediario.add_tag_to_xml(xml)
    iss_retido_intermediario.add_tag_to_xml(xml)
    email_intermediario.add_tag_to_xml(xml)
    discriminacao.add_tag_to_xml(xml)
    valor_carga_tributaria.add_tag_to_xml(xml)
    percentual_carga_tributaria.add_tag_to_xml(xml)
    fonte_carga_tributaria.add_tag_to_xml(xml)
    codigo_cei.add_tag_to_xml(xml)
    matricula_obra.add_tag_to_xml(xml)
    municipio_prestacao.add_tag_to_xml(xml)
    numero_encapsulamento.add_tag_to_xml(xml)
    valor_total_recebido.add_tag_to_xml(xml)
    retencao_pis_cofins.add_tag_to_xml(xml)

    if NfsePrefeituraSp.xsd_version >= 2
      if !valor_inicial_cobrado.value.nil?
        valor_inicial_cobrado.add_tag_to_xml(xml)
      else
        valor_final_cobrado.add_tag_to_xml(xml)
      end
      add_tags_layout2(xml)
    end
  end

  # O XSD tipa ISSRetido como xs:boolean, entao o valor chega como `true`
  # (quando montado em Ruby) ou "true"/"false" (quando vem de JSON).
  def iss_retido_efetivo?
    v = @children[:iss_retido].value
    return false if v.nil?
    return v if [true, false].include?(v)
    v.to_s.strip.casecmp('true').zero? || v.to_s.strip == '1'
  end

  def generate_signature
    # Build the base string.
    str = ''

    # Leiaute 1: "Inscrição Municipal do Prestador com 8 posições (dígitos)".
    # Leiaute 2: 12 posições.
    #
    # Usar a largura errada invalida a assinatura inteira. A prefeitura
    # responde com o erro 1206 e devolve a string que ela verificou:
    #   "Assinatura Digital do RPS incorreta - String verificada (02130033...)"
    posicoes_im = NfsePrefeituraSp.xsd_version >= 2 ? 12 : 8
    str << @children[:chave_rps].inscricao_prestador.value.to_s.rjust(posicoes_im, '0')

    # 2. Série do RPS com 5 posições (caracteres). Completar com espaços em branco à direita caso seja necessário.
    # Atenção: Não utilize espaços à esquerda. O conteúdo deverá estar alinhado à esquerda.
    str << @children[:chave_rps].serie_rps.value.to_s.ljust(5, ' ')

    # 3. Número do RPS com 12 posições (dígitos). Completar com zeros à esquerda caso seja necessário.
    str << @children[:chave_rps].numero_rps.value.to_s.rjust(12, '0')

    # 4. Data de emissão do RPS no formato AAAAMMDD (caracteres).
    str << @children[:data_emissao].value.to_s.gsub(/\D/, '')

    # 5. Tipo de Tributação do RPS com 1 posição (caractere).
    str << @children[:tributacao_rps].value.to_s

    # 6. Status do RPS com 1 posição (caractere).
    str << @children[:status_rps].value.to_s

    # 7. Valor 'S' (SIM) para ISS Retido.
    #    Valor 'N' (NÃO) para Nota Fiscal sem ISS Retido.
    #
    # O XSD tipa ISSRetido como xs:boolean, entao chega aqui tanto `true`
    # (Ruby) quanto "true"/"false" (string, se o chamador enviar JSON).
    # A versao anterior comparava `valor.to_s === true`, que e sempre falso:
    # uma nota COM ISS retido era assinada como se nao tivesse.
    str << (iss_retido_efetivo? ? 'S' : 'N')

    # 8. Leiaute 1: "Valor dos Serviços do RPS" com 15 posições.
    #    Leiaute 2: "Valor Inicial ou Final Cobrado dos Serviços".
    if NfsePrefeituraSp.xsd_version >= 2
      position_8 = (!@children[:valor_inicial_cobrado].value.nil? ? @children[:valor_inicial_cobrado].value : @children[:valor_final_cobrado].value).to_s
    else
      position_8 = @children[:valor_servicos].value.to_s
    end
    str << position_8.delete(',').delete('.').rjust(15, '0')

    # 9. Valor das Deduções do RPS, incluindo os centavos (sem ponto decimal e sem R$), com 15 posições (dígitos).
    str << @children[:valor_deducoes].value.to_s.delete(',').delete('.').rjust(15, '0')

    # 10. Código do Serviço do RPS com 5 posições (dígitos). Completar com zeros à esquerda caso seja necessário.
    str << @children[:codigo_servico].value.to_s.rjust(5, '0')

    # Positions 11 and 12.
    if !@children[:cpf_cnpj_tomador].cpf.value.nil?
      position_11 = '1'
      position_12 = @children[:cpf_cnpj_tomador].cpf.value.to_s
    elsif !@children[:cpf_cnpj_tomador].cnpj.value.nil?
      position_11 = '2'
      position_12 = @children[:cpf_cnpj_tomador].cnpj.value.to_s
    elsif !@children[:cpf_cnpj_tomador].nif.value.nil?
      position_11 = '4'
      position_12 = @children[:cpf_cnpj_tomador].nif.value.to_s
    else
      position_11 = '3'
      position_12 = ''
    end

    # 11. Indicador de CPF/CNPJ do Tomador com 1 posição (dígito).
    #     Valor 1 para CPF.
    #     Valor 2 para CNPJ.
    #     Valor 3 para Não informado.
    #     Valor 4 para NIF (obrigatório preencher campo 16).
    str << position_11

    # 12. CPF/CNPJ do tomador com 14 posições (dígitos). Sem formatação (ponto, traço, barra, ....).
    # Completar com zeros à esquerda caso seja necessário. Se o Indicador do CPF/CNPJ for 3 (não informado) ou 4 (NIF),
    # preencher com 14 zeros.
    str << position_12.rjust(14, '0')

    # Observação: não é necessário informar os dados de intermediário na assinatura se não houver intermediário.
    if !@children[:cpf_cnpj_intermediario].cpf.value.nil? || !@children[:cpf_cnpj_intermediario].cnpj.value.nil?
      # Positions 13 and 14.
      if !@children[:cpf_cnpj_intermediario].cpf.nil?
        position_13 = '1'
        position_14 = @children[:cpf_cnpj_intermediario].cpf.to_s
      elsif !@children[:cpf_cnpj_intermediario].cnpj.nil?
        position_13 = '2'
        position_14 = @children[:cpf_cnpj_intermediario].cnpj.to_s
      end

      # 13. Indicador de CPF/CNPJ do Intermediário com 1 posição (dígito).
      # Valor 1 para CPF.
      # Valor 2 para CNPJ.
      # Valor 3 para Não informado o CPF/CNPJ do Intermediário.
      str << position_13

      # 14. CPF/CNPJ do intermediário com 14 posições (dígitos). Sem formatação (ponto, traço, barra,....).
      # Completar com zeros à esquerda caso seja necessário. Se o Indicador do CPF/CNPJ for 3 (não informado),
      # preencher com 14 zeros.
      str << position_14.rjust(14, '0')

      # 15. ISS Retido Intermediário
      #     Valor 'S' (SIM) para ISS Retido pelo Intermediário
      #     Valor 'N' (NÃO) para ISS não retido pelo Intermediário
      str << @children[:iss_retido_intermediario].value.to_s
    end

    # 16. NIF ou NãoNIF
    #     Caso for preenchido NaoNif, informar apenas 1 posição com o valor:
    #       0 - Não informado na nota de origem
    #       1 - Dispensado do NIF
    #       2 - Não exigência do NIF
    #     Caso tenha o NIF, preencher com o NIF, sem espaços no começo ou final do NIF.
    #     O tamanho total do NIF não pode ultrapassar 40 caracteres.
    # str << ... TODO

    @signer.sign(str)
  end
end
