# Fechamento funcional não-fiscal — 2026-10-04

Este lote é a rodada de fechamento antes do retorno ao Fiscal. O objetivo foi remover lacunas reais identificadas no código atual, sem recriar funcionalidades já existentes.

## BI — fechamento operacional
- KPI agora possui refresh real por empresa, eliminando a lista temporária vazia.
- Cálculo de KPI valida que o registro pertence ao tenant autenticado.
- Consultas de KPI precisam ser SELECT e devem restringir empresa por `:empresaId` ou `empresa_id`.
- CRUD de KPI passou a usar o tenant do principal, não `empresaId` enviado pelo cliente.
- Criação/alteração/exclusão/refresh de KPI exigem perfil administrativo.
- Relatórios gerenciais passaram a usar tenant autenticado.
- Geração de relatório passou a exigir autenticação.
- Agendamento/desagendamento exigem perfil administrativo.
- Relatórios agendados não aceitam mais tenant arbitrário.
- Agendamentos validam relatório, frequência, intervalo customizado, data futura, formato e destinatários.
- E-mail de relatório agora envia o arquivo como anexo real via MIME.

## O que não foi recriado
- Boletos já possuem tela e rota no frontend.
- Comissões e Caixa já possuem controllers e telas.
- Aprovação de títulos já possui tela.
- Produção/MRP já possui cadeia real de estoque e sugestões de compra/OP.
- Compras já gera título a pagar no recebimento.
- Fiscal foi deliberadamente deixado para o fechamento final, preservando NF-e/NFS-e existentes.

## Pendências finais
1. Fechar CT-e/MDF-e com emissão/autorização/eventos.
2. Completar matriz/homologações NFS-e.
3. Validar operação fiscal real com certificado A1.
4. Fazer a última varredura de endpoints/telas e corrigir apenas lacunas comprovadas.

CI/CD não faz parte deste fechamento funcional.
