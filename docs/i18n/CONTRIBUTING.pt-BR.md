# Contribuindo para o Brasil SaaS ERP

Obrigado pelo interesse em contribuir com o Brasil SaaS ERP! Este documento
fornece diretrizes para contribuir com o projeto.

## Código de Conduta

Este projeto e todos os participantes são regidos pelo
[Código de Conduta](CODE_OF_CONDUCT.md). Ao participar, você é esperado
para respeitar este código.

## Como Posso Contribuir?

### Reportando Bugs

Antes de criar um relatório de bug, verifique os issues existentes para ver
se o problema já foi reportado. Ao criar um relatório de bug, inclua o
máximo de detalhes possível:

* Um título claro e descritivo
* Os passos exatos para reproduzir o problema
* O comportamento observado após seguir os passos
* O comportamento esperado
* Capturas de tela, se aplicável
* Seu ambiente (SO, versão do Java, navegador, etc.)

### Sugerindo Melhorias

Sugestões de melhorias são rastreadas como issues do GitHub. Ao criar uma
sugestão, inclua:

* Um título claro e descritivo
* Uma descrição detalhada da melhoria proposta
* Quaisquer exemplos ou mockups relevantes
* A motivação para a melhoria

### Pull Requests

1. Faça um fork do repositório e crie sua branch a partir de `main`.
2. Se você adicionou código que deve ser testado, adicione testes.
3. Se você alterou APIs, atualize a documentação.
4. Garanta que a suíte de testes passa.
5. Certifique-se de que seu código segue o estilo de código existente.
6. Crie um pull request com título e descrição claros.

## Configuração de Desenvolvimento

### Pré-requisitos

* Java 21 (Oracle JDK ou OpenJDK)
* Maven 3.9+
* Node.js 20+
* PostgreSQL 18
* MongoDB 7+
* Redis 7+
* RabbitMQ 3.13+

### Build

```bash
mvn clean package -DskipTests
```

### Executando Testes

```bash
mvn test
```

### Executando Localmente

```bash
# Inicie a infraestrutura (PostgreSQL, MongoDB, Redis, RabbitMQ)
docker-compose up -d

# Execute a aplicação
mvn spring-boot:run
```

## Estrutura do Projeto

```
src/main/java/br/com/brasil_saas/
├── core/           # Autenticação, autorização, multi-tenant
├── cadastro/       # Dados mestres (clientes, fornecedores, produtos, serviços)
├── financeiro/     # Gestão financeira
├── fiscal/         # Documentos fiscais (NFS-e, NF-e, MDF-e, CT-e, SPED)
├── estoque/        # Gestão de estoque
├── vendas/         # Vendas
├── compras/        # Compras
├── producao/      # Planejamento e controle da produção
├── rh/             # Recursos humanos
├── crm/            # Gestão de relacionamento com o cliente
├── bi/             # Inteligência de negócios
├── contabilidade/  # Contabilidade
├── ativos/         # Ativos fixos
├── dms/            # Gestão de documentos
├── qualidade/      # Gestão da qualidade
├── projetos/       # Gestão de projetos
├── wms/            # Gestão de armazém
├── workflow/       # Motor de workflow
├── portais/        # Portais de cliente/fornecedor
├── ia/             # Assistente de IA
├── servicos/       # Ordens de serviço
└── relatorios/     # Relatórios
```

## Padrões de Código

* Siga o estilo de código existente (Spring Boot, Lombok, MapStruct).
* Use nomes de variáveis e métodos significativos.
* Escreva mensagens de commit claras.
* Adicione Javadoc para métodos públicos.
* Mantenha métodos pequenos e focados em uma única responsabilidade.

## Licença

Ao contribuir, você concorda que suas contribuições serão licenciadas sob a
GNU Affero General Public License v3.0 (AGPLv3).
