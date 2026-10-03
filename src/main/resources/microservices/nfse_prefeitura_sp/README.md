# NfsePrefeituraSp

>WORK IN PROGRESS

Gem para integração com a Nota Fiscal de Serviços Eletrônica (NFS-e) da Prefeitura de São Paulo.

**Esta gem se integra exclusivamente com a "Versão 2" dos serviços da Prefeitura, contemplando as mudanças introduzidas
pela Reforma Triburtária de 2026 (IBS/CBS).**

## Manuais da Prefeitura de São Paulo

Os manuais técnicos disponibilizados pela Prefeitura de São Paulo estão disponíveis [aqui](https://notadomilhao.sf.prefeitura.sp.gov.br/manuais/).

## Módulos disponíveis

Abaixo estão indicados os serviços já disponíveis (`[X]`) e os pendentes de implementação (`[ ]`).

### Serviços síncronos

  - `[X] ENVIO DE RPS`
  - `[ ] ENVIO DE LOTE DE RPS`
  - `[X] TESTE DE ENVIO DE LOTE DE RPS`
  - `[ ] CONSULTA DE NF-E`
  - `[ ] CONSULTA DE NF-E RECEBIDAS`
  - `[ ] CONSULTA DE NF-E EMITIDAS`
  - `[ ] CONSULTA DE LOTE`
  - `[ ] CONSULTA INFORMAÇÕES DO LOTE`
  - `[X] CANCELAMENTO DE NF-E`
  - `[X] CONSULTA DE CNPJ`

### Serviços assíncronos

  - `[ ] ENVIO DE LOTE DE RPS - ASSÍNCRONO`
  - `[ ] CONSULTA SITUAÇÃO LOTE ASSÍNCRONO`
  - `[ ] TESTE ENVIO DE LOTE RPS - ASSÍNCRONO`
  - `[ ] EMISSÃO DE GUIA - ASSÍNCRONO`
  - `[ ] CONSULTA SITUAÇÃO GUIA`
  - `[ ] CONSULTA GUIA`

## Instalação

Criando um cliente:

```ruby
require 'nfse_prefeitura_sp'
client = NfsePrefeituraSp::Client.new(
  cert_path:    '/path/to/cert.p12',
  cert_password: 'passwd'
)
```

### Exemplos de serviços síncronos

ENVIO DE RPS:

```ruby
data = {
  cnpj_remetente: '00000000000000',
  rps: [
    {
      chave_rps: {
        inscricao_prestador: '00000000',
        numero_rps:          '000000',
      },

      tipo_rps:                       'RPS',
      codigo_servico:                 '00000',
      aliquota_servicos:              '0.029'
      tributacao_rps:                 'T',
      status_rps:                     'N',
      data_emissao:                   'YYYY-MM-DD',
      discriminacao:                  'Serviço prestado...',
      percentual_carga_tributaria:    '0.XXXX',
      valor_carga_tributaria:         'XX.XX',
      valor_final_cobrado:            'XXX.XX',
      valor_ipi:                      'XX.XX',
      valor_pis:                      'XX.XX',
      valor_cofins:                   'XX.XX',
      valor_inss:                     'XX.XX',
      valor_ir:                       'XX.XX',
      valor_csll:                     'XX.XX',
      retencao_pis_cofins:            '0',
      exigibilidade_suspensa:         '0',
      pagamento_parcelado_antecipado: '0',
      nbs:                            '000000000',
      c_loc_prestacao:                '0000000', # Código IBGE
      iss_retido:                     false,
      valor_deducoes:                 '0.00',
      endereco_tomador: {
        tipo_logradouro:      'Rua',
        logradouro:           'Logradouro',
        numero_endereco:      '000',
        complemento_endereco: 'Complemento',
        bairro:               'Bairro',
        cidade:               '0000000', # Código IBGE
        uf:                   'SP',
        cep:                  '00000000',
      },
      cpf_cnpj_tomador: {
        cnpj: '00000000000000',
      },
      razao_social_tomador: 'Razão Social',
      ibs_cbs: {
        fin_nfse:  '0',
        ind_final: '0',
        c_ind_op:  '000000',
        ind_dest:  '0',
        valores: {
          trib: {
            g_ibs_cbs: {
              c_class_trib: '000000',
            },
          },
        },
      },
    },
  ],
}

response = client.sync_envio_rps(data)

response.success?                                                    # true / false
response.retorno[:chave_n_fe_rps][:chave_n_fe][:inscricao_prestador] # Inscrição do prestador de serviços
response.retorno[:chave_n_fe_rps][:chave_n_fe][:numero_n_fe]         # Número da nota fiscal
response.retorno[:chave_n_fe_rps][:chave_n_fe][:codigo_verificacao]  # Código de verificação
response.retorno[:alerta]                                            # Alertas
response.retorno[:erro]                                              # Erros
```

TESTE DE ENVIO DE LOTE DE RPS:

```ruby
data = {
  cnpj_remetente: '00000000000000',
  dt_inicio:'YYYY-MM-DD',
  dt_fim:   'YYYY-MM-DD',
  rps: [
    {
      chave_rps: {
        inscricao_prestador: '00000000',
        numero_rps:          '000000',
      },

      tipo_rps:                       'RPS',
      codigo_servico:                 '00000',
      aliquota_servicos:              '0.029'
      tributacao_rps:                 'T',
      status_rps:                     'N',
      data_emissao:                   'YYYY-MM-DD',
      discriminacao:                  'Serviço prestado...',
      percentual_carga_tributaria:    '0.XXXX',
      valor_carga_tributaria:         'XX.XX',
      valor_final_cobrado:            'XXX.XX',
      valor_ipi:                      'XX.XX',
      valor_pis:                      'XX.XX',
      valor_cofins:                   'XX.XX',
      valor_inss:                     'XX.XX',
      valor_ir:                       'XX.XX',
      valor_csll:                     'XX.XX',
      exigibilidade_suspensa:         '0',
      pagamento_parcelado_antecipado: '0',
      nbs:                            '000000000',
      c_loc_prestacao:                '0000000', # Código IBGE
      iss_retido:                     false,
      valor_deducoes:                 '0.00',
      endereco_tomador: {
        tipo_logradouro:      'Rua',
        logradouro:           'Logradouro',
        numero_endereco:      '000',
        complemento_endereco: 'Complemento',
        bairro:               'Bairro',
        cidade:               '0000000', # Código IBGE
        uf:                   'SP',
        cep:                  '00000000',
      },
      cpf_cnpj_tomador: {
        cnpj: '00000000000000',
      },
      inscricao_municipal_tomador: '000000000000',
      razao_social_tomador: 'Razão Social',
      ibs_cbs: {
        fin_nfse:  '0',
        ind_final: '0',
        c_ind_op:  '000000',
        ind_dest:  '0',
        valores: {
          trib: {
            g_ibs_cbs: {
              c_class_trib: '000000',
            },
          },
        },
      },
    },
  ],
}

response = client.sync_teste_envio_lote_rps(data)
# TODO: exemplo de resposta
```

CANCELAMENTO DE NF-E:

```ruby
data = {
  cnpj_remetente: '00000000000000',
  transacao: false,
  detalhes: [
    {
      chave_nfe: {
        inscricao_prestador: '00000000',
        numero_nfe:          '000000000000',
        # codigo_verificacao:  '000000',
        # chave_nota_nacional: '000000',
      }
    }
  ],
}

response = client.sync_cancelamento_nfe(data)

response.success?         # true / false
response.retorno          # Hash
response.retorno[:alerta] # Alertas
response.retorno[:erro]   # Erros
```

CONSULTA DE CNPJ:

```ruby
data = {
  cnpj_remetente:    '00000000000000',
  cnpj_contribuinte: '00000000000000',
}

response = client.sync_consulta_cnpj(data)

response.success?                                # true / false
response.retorno[:detalhe][:inscricao_municipal] # Inscrição municipal
response.retorno[:detalhe][:emite_n_fe]          # Emite NF-e (true/false)
response.retorno[:alerta]                        # Alertas
response.retorno[:erro]                          # Erros
```

### Exemplos de serviços assíncronos

TODO

## Licença

Esta gem está disponível como código aberto sob os termos da [Licença MIT](https://opensource.org/licenses/MIT).
