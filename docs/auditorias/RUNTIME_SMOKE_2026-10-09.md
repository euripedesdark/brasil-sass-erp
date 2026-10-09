# Inicialização da imagem ERP — 9 de outubro de 2026

A construção da imagem prova que o pacote pode ser gerado, mas não confirma que a aplicação inicia com os serviços de infraestrutura. O job Docker passa a iniciar a imagem construída e verificar essa conexão.

## Verificação adicionada

- PostgreSQL 18 com banco descartável `erp_runtime_smoke`, MongoDB 7, Redis 7 e RabbitMQ 3.
- Inicialização do ERP na porta 18080, com Flyway e validação Hibernate da configuração normal.
- Espera limitada pela aplicação pronta, acompanhando se o processo continua em execução.
- Health check exige UP em PostgreSQL, MongoDB, Redis, RabbitMQ e readiness da aplicação.
- HTTP na raiz precisa retornar o documento HTML do frontend incluído na imagem.
- Logs, health JSON e HTML são publicados como artefatos de teste, e o container da aplicação é removido ao final.

O script `scripts/ci/runtime-smoke.sh` usa as imagens de infraestrutura já adotadas pelo projeto e configurações próprias de CI. SMTP não participa dessa verificação. Autenticação externa, credenciais de integrações fiscais e emissão de documentos ainda exigem testes próprios. Nenhum parâmetro de produção foi alterado.

Validação inicial: YAML do workflow, sintaxe bash dos passos/script e diff check aprovados. A execução completa do novo teste será verificada no GitHub antes de considerar essa evidência concluída.
