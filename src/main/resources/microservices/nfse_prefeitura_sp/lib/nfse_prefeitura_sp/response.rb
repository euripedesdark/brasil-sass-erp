# frozen_string_literal: true
# Based on https://github.com/iugu/nfe-paulistana in order to keep as consistent as possible.
class NfsePrefeituraSp::Response
  RETURN_ROOT = {
    teste_envio_lote_rps:      :envio_lote_rps,
    consulta_n_fe:             :consulta,
    consulta_n_fe_emitidas:    :consulta,
    consulta_n_fe_recebidas:   :consulta,
    consulta_lote:             :consulta,
    consulta_informacoes_lote: :informacoes_lote,
  }.freeze

  def initialize(options = {})
    @options = options
  end

  def xml
    @options[:xml]
  end

  def nfe_method
    @options[:method]
  end

  # O nome do no de retorno deriva do metodo chamado, como a gem ja fazia:
  #   :consulta_cnpj         => <RetornoConsultaCNPJ>
  #   :consulta_n_fe         => <RetornoConsulta>
  #   :teste_envio_lote_rps  => <RetornoEnvioLoteRPS>
  #
  # A prefeitura nomeia o no com caixa ALTA e o sufixo especifico da
  # operacao, nao `retorno_consulta`. Por isso a busca ignora caixa e tenta
  # varios padroes antes de cair na raiz.
  def retorno
    @retorno ||= begin
      doc = Nokogiri::XML(xml) { |c| c.noblanks }
      sufixo = (RETURN_ROOT[@options[:method]] || @options[:method]).to_s
      raiz = nil
      [
        "translate(local-name(),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='RETORNO_#{sufixo.upcase}'",
        "translate(local-name(),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='RETORNO'",
        "starts-with(translate(local-name(),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'RETORNO')"
      ].each do |cond|
        raiz = doc.at_xpath("//*[#{cond}]")
        break if raiz
      end
      raiz ||= doc.root
      # chaves como simbolo: e o que a gem entregava antes
      # (Hash.from_xml(...).deep_transform_keys(&:underscore.to_sym)) e o que
      # todo o resto da gem e o chamador assumem.
      self.class.symbolize(self.class.parse_xml(raiz))
    end
  end

  def success?
    v = dig_retorno(:cabecalho, :sucesso)
    s = v.to_s.strip.downcase
    s == 'true' || s == '1'
  end

  def errors
    return if success?
    r = retorno
    return 'Resposta da prefeitura nao reconhecida' unless r.is_a?(Hash)

    # A prefeitura costuma devolver o motivo em campos diferentes por
    # operacao (Alerta, Erro, Mensagem) e as vezes aninhado em Detalhe.
    achado = dig_retorno(:erro) ||
             dig_retorno(:alerta) ||
             dig_retorno(:mensagem) ||
             dig_retorno(:detalhe, :erro) ||
             dig_retorno(:detalhe, :alerta)
    achado || 'Erro nao informado pela prefeitura'
  end

  private

  def dig_retorno(*chaves)
    atual = retorno
    chaves.each do |c|
      return nil unless atual.is_a?(Hash)
      atual = atual[c]
    end
    atual
  end

  # Converte um no XML em Hash, aplicando as mesmas transformacoes que
  # `Hash.from_xml(...).deep_transform_keys(&:underscore)` fazia.
  #
  # Por que nao usar Hash.from_xml: metodo foi removido no ActiveSupport 8
  # (ainda presente na 6/7). A gem declara `activesupport ~> 8.0` mas
  # usava um metodo que nao existe nessa versao, quebrando a leitura da
  # resposta com "undefined method 'from_xml' for class Hash".
  # Converte um no XML em Hash, reproduzindo o que
  # `Hash.from_xml(...).deep_transform_keys(&:underscore)` fazia.
  #
  # Por que nao usar Hash.from_xml: o metodo foi removido no ActiveSupport 8
  # (ainda presente na 6/7). A gem declara `activesupport ~> 8.0` mas usava
  # um metodo que nao existe nessa versao, quebrando a leitura da resposta
  # com "undefined method 'from_xml' for class Hash".
  #
  # So vira array quando a MESMA chave se repete, exatamente como o
  # ActiveSupport fazia (force_array = false). Nao se usa o atalho
  # "todos os filhos tem o mesmo nome => lista", porque contentor legitimo
  # com um unico filho e o caso comum da prefeitura:
  #   <Cabecalho><Sucesso>true</Sucesso></Cabecalho>
  # virava ["true"] e quebrava success?.
  def self.parse_xml(node)
    return nil if node.nil?

    # elemento sem filhos => valor escalar
    if node.element_children.empty?
      return nil if node.text.nil? || node.text.strip.empty?
      return node.text.strip
    end

    out = {}
    node.element_children.each do |f|
      chave = underscore(f.name)
      valor = parse_xml(f)
      if out.key?(chave)
        out[chave] = Array(out[chave])
        out[chave] << valor
      else
        out[chave] = valor
      end
    end
    out
  end

  # ActiveSupport foi deixado de fora de proposito: so precisa-se de
  # underscored, e trinta linhas locales evitam depender da versao dele.
  def self.underscore(str)
    str.to_s
       .gsub(/::/, '/')
       .gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2')
       .gsub(/([a-z\d])([A-Z])/, '\1_\2')
       .tr('-', '_')
       .downcase
  end

  # Converte as chaves de string para simbolo, preservando arrays.
  def self.symbolize(obj)
    case obj
    when Hash
      obj.each_with_object({}) { |(k, v), acc| acc[k.to_sym] = symbolize(v) }
    when Array
      obj.map { |v| symbolize(v) }
    else
      obj
    end
  end
end
