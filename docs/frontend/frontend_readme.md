# Frontend Brasil SaaS ERP

Este é o frontend do sistema Brasil SaaS ERP, desenvolvido com React e PrimeReact.

## Estrutura do Projeto

```
src/
├── components/          # Componentes React
│   ├── admin/           # Componentes administrativos
│   ├── cadastro/        # Componentes de cadastro
│   ├── financeiro/      # Componentes financeiros
│   ├── rh/              # Componentes de RH
│   ├── vendas/          # Componentes de vendas
│   ├── compras/         # Componentes de compras
│   ├── estoque/         # Componentes de estoque
│   ├── servicos/        # Componentes de serviços
│   ├── producao/        # Componentes de produção
│   ├── Layout.jsx       # Componente de layout principal
│   ├── Login.jsx        # Componente de login
│   └── Dashboard.jsx    # Componente de dashboard
├── contexts/            # Contextos React
│   └── AuthContext.jsx  # Contexto de autenticação
├── services/            # Serviços e configuração de API
│   ├── ApiConfig.js     # Configuração do axios
│   └── AuthService.js   # Serviço de autenticação
└── App.jsx              # Componente raiz da aplicação
```

## Funcionalidades Implementadas

### 1. Autenticação e Autorização
- Sistema de login com proteção de rotas
- Contexto de autenticação para gerenciamento de sessão
- Interceptores para inclusão de token JWT

### 2. Interface Administrativa
- Gerenciamento de usuários (listagem e edição)
- Configurações do sistema (gerais e de imagem)
- Painel de controle para administradores

### 3. Módulos de Negócio
- Cadastro de pessoas (clientes, fornecedores, funcionários)
- Cadastro de produtos
- Módulos financeiros, RH, vendas, compras, estoque, serviços e produção
- Dashboard com métricas e atividades recentes

### 4. Design e UI
- Menu lateral expansível
- Design responsivo
- Componentes PrimeReact para consistência visual
- Fundo translúcido conforme solicitado
- Uso das imagens tela-inicial.png e tela-login.png

### 5. Recursos Específicos
- Sistema de comissões para vendedores
- Gestão de funcionários como serviço técnico, prestadores ou vendedores
- Módulo de produção para indústria/agricultura/pecuária
- Histórico de últimas alterações em cada módulo

## Hierarquia de Acesso

O sistema implementa uma hierarquia de permissões:
- **Administrador**: Acesso completo, incluindo gerenciamento de outros administradores
- **Diretor**: Acesso a todas as funcionalidades exceto gerenciamento de administradores
- **Gerente**: Acesso a todas as funcionalidades exceto gerenciamento de administradores e diretores
- Outros papéis: Acesso limitado às respectivas funcionalidades

## Tecnologias Utilizadas

- React 18
- PrimeReact (componentes UI)
- React Router DOM (navegação)
- Axios (requisições HTTP)
- PrimeIcons (ícones)

## Imagens do Sistema

O sistema utiliza duas imagens principais:
- `tela-inicial.png`: Imagem de fundo para o dashboard e outras telas internas
- `tela-login.png`: Imagem de fundo para a tela de login

Essas imagens estão localizadas em `src/main/resources/static/images/`.

## Integração com Backend

O frontend se integra com o backend Spring Boot através de uma API REST, com:
- Autenticação JWT
- Interceptores para tratamento de tokens
- Tratamento de erros e expiração de sessão