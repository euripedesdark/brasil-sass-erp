# Status funcional do Brasil SaaS ERP — 2026-10-04

Este documento separa **código existente** de **operação comprovada**. Não considera CI/CD como gap funcional.

## Fechado nesta rodada

### NF-e
- emissão a partir de Pedido de Venda já está implementada no `NFeServiceImpl`;
- montagem, assinatura, transmissão e consulta de recibo usam `java-nfe`;
- cancelamento transmite evento à SEFAZ;
- consulta persistida e consulta direta à SEFAZ existem;
- a tela `/fiscal/nfe` permite emitir, consultar, cancelar e agora listar NF-e persistidas;
- a listagem não transporta o XML completo: usa resumo paginado;
- XML autorizado pode ser baixado por registro;
- acesso é filtrado por `X-Empresa-Id` e roles fiscais/financeiras.

**Importante:** emissão de NF-e só pode ser considerada operacional em uma instalação quando certificado, configuração SEFAZ, cadastro fiscal da empresa e homologação forem validados com uma NF-e real de teste.

### NFS-e
O fluxo já possui roteamento por município:
- Rondonópolis: código IBGE `5107602`;
- São Paulo: código IBGE `3550308`;
- demais municípios: provedor nacional.

O serviço persiste retorno, identificadores e arquivos. O ponto pendente é ampliar e homologar a matriz de municípios/provedores, não recriar a tela.

### CT-e / MDF-e
O ERP já possui:
- health/status da autorizadora;
- consulta de recibo do MDF-e;
- consulta/listagem de documentos persistidos;
- detalhe com XML;
- tela operacional de consulta.

**Ainda não está fechado:** montagem do documento, assinatura, recepção/autorização, persistência da chave/protocolo e eventos de CT-e/MDF-e. Não deve ser mascarado por uma tela que apenas chama `status`.

## Próximos blocos funcionais

### 1. Fiscal de transporte
Implementar, nesta ordem:
1. modelo de emissão de CT-e a partir da operação de transporte;
2. validação local do documento;
3. assinatura e envio;
4. processamento de recibo/protocolo;
5. persistência do XML autorizado;
6. eventos;
7. MDF-e: montagem → autorização → encerramento;
8. testes de homologação com certificado A1.

### 2. Compras / 3-way match
Fechar a cadeia:
**pedido de compra → recebimento → documento fiscal → conferência de quantidade/valor → título a pagar → contabilização**.

O workflow multinível já existe; o gap é a conciliação documental/financeira.

### 3. Vendas end-to-end
Fechar:
**pedido → reserva → separação → expedição/entrega → NF-e → financeiro → contabilidade**.

Não criar outra tela de pedido se o problema for integração entre os módulos.

### 4. WMS
A sequência funcional deve ser única:
**reserva → picking → packing → expedição**.

A infraestrutura de volumes/embalagem já foi exposta pela UI; falta validar a integração completa com a expedição e faturamento.

### 5. Produção/MRP
MRP, geração de OP, solicitação de compra, capacidade finita e custeio já existem.

Falta fechar a integração:
**planejamento → OP → apontamento → consumo → produção boa/refugo → estoque → custo → financeiro/contabilidade**.

### 6. Contabilidade / Financeiro
Já existem fechamentos, reabertura, geração a partir de título e integrações parciais.

Falta aprofundar:
- fechamento fiscal/contábil integrado;
- conciliação bancária;
- apropriações;
- integração automática de documentos fiscais;
- trilha de auditoria das contabilizações.

### 7. Ativos / Qualidade / Projetos
Os módulos e telas existem.

O critério daqui em diante é integração operacional:
- Ativos: aquisição → depreciação → manutenção → baixa → contabilidade;
- Qualidade: inspeção → não conformidade → ação corretiva → encerramento;
- Projetos: WBS → execução → apontamentos → custo → faturamento.

### 8. eSocial / RH
O módulo existe, mas processos legais devem ser tratados por evento e retorno:
**geração → assinatura/transmissão → protocolo → retorno → rejeição → correção → reenvio → fechamento**.

### 9. BI
Há duas famílias de API:
- `/api/bi/relatorios`;
- `/api/bi/reports`.

As duas estão mapeadas para o hub de BI. Antes de criar outra tela, é necessário decidir se são recursos distintos ou duplicados e eliminar a duplicidade arquitetural.

## Regra de conclusão

Um módulo só será marcado como **concluído** quando houver:

1. endpoint;
2. regra de negócio;
3. persistência;
4. tela operacional quando aplicável;
5. tratamento de erro;
6. autorização/tenant;
7. integração externa quando aplicável;
8. teste automatizado;
9. teste de homologação quando depender de SEFAZ/prefeitura/banco/órgão externo.

A existência de uma classe Java ou de uma rota HTTP isolada não conta como conclusão.
