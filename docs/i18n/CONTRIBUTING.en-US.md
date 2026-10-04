# Contributing to Brasil SaaS ERP

Thank you for your interest in contributing to Brasil SaaS ERP! This document
provides guidelines for contributing to the project.

## Code of Conduct

This project and all participants are governed by the
[Code of Conduct](../../CODE_OF_CONDUCT.md). By participating, you are expected to
uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report, please check the existing issues as you might
find out that you don't need to create one. When you are creating a bug report,
please include as many details as possible:

* A clear and descriptive title
* The exact steps which reproduce the problem
* The behavior you observed after following the steps
* The behavior you expected to see instead
* Screenshots, if applicable
* Your environment (OS, Java version, browser, etc.)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an
enhancement suggestion, please include:

* A clear and descriptive title
* A detailed description of the proposed enhancement
* Any relevant examples or mockups
* The motivation for the enhancement

### Pull Requests

1. Fork the repository and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code follows the existing code style.
6. Create a pull request with a clear title and description.

## Development Setup

### Prerequisites

* Java 21 (Oracle JDK or OpenJDK)
* Maven 3.9+
* Node.js 20+
* PostgreSQL 18
* MongoDB 7+
* Redis 7+
* RabbitMQ 3.13+

### Building

```bash
mvn clean package -DskipTests
```

### Running Tests

```bash
mvn test
```

### Running Locally

```bash
# Start infrastructure (PostgreSQL, MongoDB, Redis, RabbitMQ)
docker-compose up -d

# Run the application
mvn spring-boot:run
```

## Project Structure

```
src/main/java/br/com/brasil_saas/
├── core/           # Authentication, authorization, multi-tenancy
├── cadastro/       # Master data (customers, suppliers, products, services)
├── financeiro/     # Financial management
├── fiscal/         # Tax documents (NFS-e, NF-e, MDF-e, CT-e, SPED)
├── estoque/        # Inventory management
├── vendas/         # Sales
├── compras/        # Purchasing
├── producao/      # Production planning and control
├── rh/             # Human resources
├── crm/            # Customer relationship management
├── bi/             # Business intelligence
├── contabilidade/  # Accounting
├── ativos/         # Fixed assets
├── dms/            # Document management
├── qualidade/      # Quality management
├── projetos/       # Project management
├── wms/            # Warehouse management
├── workflow/       # Workflow engine
├── portais/        # Customer/supplier portals
├── ia/             # AI assistant
├── servicos/       # Service orders
└── relatorios/     # Reports
```

## Coding Standards

* Follow the existing code style (Spring Boot, Lombok, MapStruct).
* Use meaningful variable and method names.
* Write clear commit messages.
* Add Javadoc for public methods.
* Keep methods small and focused on a single responsibility.

## License

By contributing, you agree that your contributions will be licensed under the
GNU Affero General Public License v3.0 (AGPLv3).
