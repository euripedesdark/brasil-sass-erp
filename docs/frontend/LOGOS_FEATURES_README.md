# Sistema BRASIL-SAAS-ERP - Recursos de Logos e Fotos

## Descrição

Este sistema permite o gerenciamento de logos e fotos para diferentes entidades do sistema ERP, incluindo:

- **Logos de Empresas**
- **Fotos de Usuários** (perfil)
- **Fotos de Funcionários**
- **Logos de Clientes**
- **Logos de Fornecedores**

Os logos e fotos podem ser utilizados em **notas fiscais**, **ordens de serviço (OS)** e **relatórios**.

## Estrutura de Dados

### Modelos Atualizados

#### Empresa (`br.com.brasil_saas.core.model.Empresa`)
- `logoUrl`: URL do logo da empresa
- `logoTipoConteudo`: Tipo MIME da imagem (ex: image/jpeg)
- `logoTamanho`: Tamanho da imagem em bytes

#### Usuario (`br.com.brasil_saas.core.model.Usuario`)
- `fotoUrl`: URL da foto do usuário
- `fotoTipoConteudo`: Tipo MIME da imagem
- `fotoTamanho`: Tamanho da imagem em bytes

#### Funcionario (`br.com.brasil_saas.rh.model.Funcionario`)
- `fotoUrl`: URL da foto do funcionário
- `fotoTipoConteudo`: Tipo MIME da imagem
- `fotoTamanho`: Tamanho da imagem em bytes

#### Cliente (`br.com.brasil_saas.cadastro.model.Cliente`)
- `logoUrl`: URL do logo do cliente
- `logoTipoConteudo`: Tipo MIME da imagem
- `logoTamanho`: Tamanho da imagem em bytes

#### Fornecedor (`br.com.brasil_saas.cadastro.model.Fornecedor`)
- `logoUrl`: URL do logo do fornecedor
- `logoTipoConteudo`: Tipo MIME da imagem
- `logoTamanho`: Tamanho da imagem em bytes

## Arquitetura de Armazenamento

### Armazenamento de Imagens
- As imagens são armazenadas no **MongoDB** em uma coleção chamada `imagens`
- Cada imagem é associada a uma entidade específica com tipo (empresa_logo, usuario_foto, etc.)
- Implementação utiliza o modelo existente de armazenamento de imagens (similar ao módulo de produtos)

### Serviços Disponíveis
- `GenericoImagemService`: Serviço centralizado para operações de imagem
- `LogoService`: Serviço para recuperação de logos/fotos para relatórios e documentos

## Endpoints Disponíveis

### Logos e Fotos Específicos

#### Logo da Empresa
- **POST** `/api/core/empresas/logo` - Atualizar logo da empresa
- **GET** `/api/core/empresas/{id}/logo` - Obter logo da empresa
- **DELETE** `/api/core/empresas/logo` - Remover logo da empresa

#### Foto do Usuário
- **POST** `/api/core/usuarios/foto` - Atualizar foto do usuário
- **GET** `/api/core/usuarios/{id}/foto` - Obter foto do usuário
- **DELETE** `/api/core/usuarios/foto` - Remover foto do usuário

#### Foto do Funcionário
- **POST** `/api/rh/funcionarios/foto` - Atualizar foto do funcionário
- **GET** `/api/rh/funcionarios/{id}/foto` - Obter foto do funcionário
- **DELETE** `/api/rh/funcionarios/foto` - Remover foto do funcionário

#### Logo do Cliente
- **POST** `/api/cadastro/clientes/logo` - Atualizar logo do cliente
- **GET** `/api/cadastro/clientes/{id}/logo` - Obter logo do cliente
- **DELETE** `/api/cadastro/clientes/logo` - Remover logo do cliente

#### Logo do Fornecedor
- **POST** `/api/cadastro/fornecedores/logo` - Atualizar logo do fornecedor
- **GET** `/api/cadastro/fornecedores/{id}/logo` - Obter logo do fornecedor
- **DELETE** `/api/cadastro/fornecedores/logo` - Remover logo do fornecedor

### Controller Centralizado
- **POST** `/api/core/logos/empresa` - Atualizar logo da empresa
- **GET** `/api/core/logos/empresa/{id}` - Obter logo da empresa
- **POST** `/api/core/logos/usuario` - Atualizar foto do usuário
- **GET** `/api/core/logos/usuario/{id}` - Obter foto do usuário

## Permissões de Acesso

### Permissões Requeridas
- `core:empresa:escrita` - Para atualizar logo da empresa
- `core:empresa:leitura` - Para visualizar logo da empresa
- `core:usuario:escrita` - Para atualizar foto do usuário
- `core:usuario:leitura` - Para visualizar foto do usuário
- `rh:funcionario:escrita` - Para atualizar foto do funcionário
- `rh:funcionario:leitura` - Para visualizar foto do funcionário
- `cadastro:cliente:escrita` - Para atualizar logo do cliente
- `cadastro:cliente:leitura` - Para visualizar logo do cliente
- `cadastro:fornecedor:escrita` - Para atualizar logo do fornecedor
- `cadastro:fornecedor:leitura` - Para visualizar logo do fornecedor