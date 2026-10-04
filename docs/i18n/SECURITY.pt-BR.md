# Política de Segurança

Levamos a segurança do Brasil SaaS ERP a sério. Se você acredita ter encontrado
uma vulnerabilidade de segurança, reporte-a de forma responsável.

## Como Reportar

**Não abra issues públicas para vulnerabilidades de segurança.**
Em vez disso, envie um e-mail para:

**euripedesdark@gmail.com**

Inclua as seguintes informações:

* Uma descrição da vulnerabilidade
* Passos para reproduzir o problema
* O impacto potencial
* Quaisquer sugestões de correção (se disponível)

## O Que Esperar

* Reconheceremos seu relatório em até 48 horas.
* Investigaremos o problema e forneceremos um cronograma para a correção.
* Creditaremos você nas notas de lançamento (a menos que prefira permanecer anônimo).
* Lançaremos uma correção o mais rápido possível e o notificaremos quando estiver disponível.

## Escopo

Esta política de segurança aplica-se a:

* O código da aplicação Brasil SaaS ERP
* Os endpoints da API
* O esquema do banco de dados e as migrations
* Os scripts de deploy e configurações

## Fora do Escopo

* Bibliotecas de terceiros (reporte vulnerabilidades aos respectivos projetos)
* Problemas na documentação
* Perguntas gerais sobre melhores práticas de segurança

## Medidas de Segurança

O projeto implementa as seguintes medidas de segurança:

* **Autenticação:** Autenticação baseada em JWT com refresh tokens
* **Autorização:** Controle de acesso baseado em papéis (RBAC) com permissões
* **Multi-tenant:** Isolamento de dados entre empresas
* **Criptografia:** TLS para todas as comunicações, mTLS para conexões com banco de dados
* **Validação de entrada:** Bean Validation em todos os endpoints da API
* **Prevenção de SQL injection:** Consultas parametrizadas via JPA
* **Prevenção de XSS:** Escape integrado do React
* **Proteção CSRF:** Design de API stateless
* **Auditoria:** Logs de acesso e trilhas de auditoria

## Política de Divulgação

Seguimos uma política de divulgação coordenada. Pedimos que você:

* Dê-nos um tempo razoável para corrigir o problema antes de divulgá-lo publicamente.
* Não explore a vulnerabilidade além do necessário para demonstrá-la.
* Não acesse ou modifique dados pertencentes a outros usuários.
