# Módulo cadastro — CONCLUÍDO (Fase 7)

## Entidades (21)
- Globais: Municipio, BaseCep
- Agregado pessoa: Pessoa, PessoaFisica, PessoaJuridica, Papel,
  Cliente, Fornecedor, Transportadora, Endereco, Contato
- Produto/serviço: Categoria, Marca, UnidadeMedida, Produto,
  ProdutoVariacao, ProdutoKit, ProdutoImagem, ProdutoEcommerce,
  Servico, DocumentoFiscal

## Camadas
- Repositories: 21 (Spring Data JPA, filtro DeletedAtIsNull)
- DTOs: records com Bean Validation
- Mappers: MapStruct
- Services: interface + impl (multi-tenant por empresaId)
- Controllers: 9 REST em /api/cadastro/**

## Endpoints
/api/cadastro/pessoas
/api/cadastro/clientes
/api/cadastro/fornecedores
/api/cadastro/transportadoras
/api/cadastro/produtos
/api/cadastro/servicos
/api/cadastro/categorias
/api/cadastro/marcas
/api/cadastro/unidades-medida

## Permissões (V18)
cadastro:{pessoa|cliente|fornecedor|transportadora|produto|servico|categoria|marca|unidade}:{leitura|escrita}
