> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

# Fase 8 — Módulo Fiscal

## Objetivo
Implementar o módulo fiscal completo do Brasil SaaS ERP, cobrindo todos os documentos fiscais eletrônicos brasileiros (NFe, NFCe, NFSe, CTe, MDFe), apurações de impostos, obrigações acessórias (SPED, REINF, eSocial) e o motor de regras tributárias.

## Tabelas já existentes (V4__fiscal.sql)
As tabelas já foram criadas na Fase 4. Esta fase implementa as entidades JPA, repositórios, serviços e controllers.

### Tabelas do schema `brasil_saas`:
- `bc_fis_certificado_digital` — Certificados A1/A3 por empresa
- `bc_fis_imposto` — Cadastro de impostos (ICMS, IPI, PIS, COFINS, ISS, etc.)
- `bc_fis_regra_tributaria` — Regras de tributação por NCM/CFOP/UF
- `bc_fis_ncm` — Nomenclatura Comum do Mercosul (seed V16)
- `bc_fis_cest` — Código Especificador da Substituição Tributária
- `bc_fis_cfop` — Código Fiscal de Operações e Prestações
- `bc_fis_nbs` — Nomenclatura Brasileira de Serviços
- `bc_fis_servico_lc116` — Serviços da LC 116/2003
- `bc_fis_issqn` — Alíquotas ISS por município (seed V16)
- `bc_fis_cnae_servico` — CNAE vinculado a serviços
- `bc_fis_nfe` — Notas Fiscais Eletrônicas (cabeçalho)
- `bc_fis_nfe_item` — Itens da NFe
- `bc_fis_nfe_evento` — Eventos da NFe (cancelamento, carta de correção)
- `bc_fis_nfce` — Notas Fiscais de Consumidor Eletrônicas
- `bc_fis_nfce_item` — Itens da NFCe
- `bc_fis_nfse` — Notas Fiscais de Serviço Eletrônicas
- `bc_fis_nfse_item` — Itens da NFSe
- `bc_fis_cte` — Conhecimentos de Transporte Eletrônicos
- `bc_fis_cte_item` — Itens do CTe
- `bc_fis_mdfe` — Manifestos Eletrônicos de Documentos Fiscais
- `bc_fis_manifestacao` — Manifestação do destinatário
- `bc_fis_apuracao` — Apurações de impostos por período
- `bc_fis_sped_fiscal` — SPED Fiscal (ICMS/IPI)
- `bc_fis_sped_contribuicoes` — SPED Contribuições (PIS/COFINS)
- `bc_fis_ecd` — Escrituração Contábil Digital
- `bc_fis_ecf` — Escrituração Contábil Fiscal
- `bc_fis_esocial` — eSocial
- `bc_fis_reinf` — EFD-Reinf

## Entidades JPA a criar

### 1. CertificadoDigital
```java
package br.com.brasil_saas.fiscal.model;

@Entity
@Table(name = "bc_fis_certificado_digital", schema = "brasil_saas")
public class CertificadoDigital extends TenantEntity {
    private String tipo;           // A1, A3
    private String cnpj;
    private String razaoSocial;
    private String arquivoPath;    // caminho ou MinIO key
    private String senha;          // encryptada
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean ativo;
}
