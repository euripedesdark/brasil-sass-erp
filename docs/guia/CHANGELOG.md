# Notas de versão

## v4.2.0 - 2026-09-21
### Implementações
- **Módulo Produção**: Implementação completa com controller, service, repository e models
- **Novos Cadastros**: Categorias, Marcas, Unidades de Medida, Transportadoras (com status)
- **Entrada de Notas**: Módulo de entrada de notas fiscais implementado
- **Hierarquia de Perfis**: Adicionado campo hierarquia_nivel em Perfil (V45)
- **Seed Diretoria/Gerente**: Dados iniciais para hierarquia corporativa (V46)
- **Logotipos e Imagens**: Campos de logo/foto em múltiplas entidades (V39, V44)
- **Migrations Flyway**: 33 migrations implementadas (V1-V46)

### Estrutura do Projeto
- **Monólito Modular**: 17 módulos organizados por domínio de negócio
- **Total de Classes Java**: 626+ classes (347 no monólito + 279 nos módulos)
- **Frontend React**: Interface moderna com design translúcido

## v4.1.3
- Correcao de Base de calculo Cclasstrib icms 70
- Correcao com reuso de cclasstrib 
- Correcao proc Eventos sem signature