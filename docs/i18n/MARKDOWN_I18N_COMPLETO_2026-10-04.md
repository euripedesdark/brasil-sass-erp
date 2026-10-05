# Internacionalização da documentação Markdown

Data: 2026-10-04

## Escopo

O repositório contém 177 arquivos `.md`. Eles foram classificados em:

- documentação própria do Brasil SaaS ERP;
- documentação histórica/auditorias;
- documentação de microsserviços próprios;
- documentação de terceiros/upstream;
- arquivos legais/licenças.

A internacionalização deve abranger a documentação própria do produto, sem reescrever licenças ou documentação upstream.

## Idiomas

- `pt-BR` — fonte canônica;
- `en-US`;
- `es-ES`;
- `fr-FR`.

## Regras

1. Código, comandos, endpoints, nomes de classes, tabelas, variáveis e identificadores permanecem inalterados.
2. Siglas fiscais brasileiras permanecem inalteradas: NF-e, NFS-e, CT-e, MDF-e, SPED, SEFAZ, ISSQN, CFOP, NCM etc.
3. Datas, hashes, versões e resultados históricos não são reinterpretados.
4. Links relativos devem apontar para o documento correspondente no idioma quando houver versão traduzida.
5. Documentação de terceiros e arquivos de licença não devem ser artificialmente traduzidos.
6. Tradução deve ser idiomática e técnica, não palavra por palavra.
7. Documentos históricos devem continuar marcados como históricos; tradução não transforma um snapshot antigo em estado atual.

## Inventário atual

A raiz e os diretórios próprios devem convergir para uma estrutura previsível de documentação multilíngue. Os documentos já existentes em `docs/i18n/` são a referência inicial para nomenclatura dos idiomas.

## Verificação

A conclusão da internacionalização será considerada válida somente quando:

- todos os Markdown próprios estiverem classificados;
- não houver mistura acidental de português nos documentos EN/ES/FR;
- os índices e links internos forem consistentes;
- conteúdo técnico permanecer semanticamente equivalente ao PT-BR;
- documentação de terceiros/licenças permanecer intacta.

Este arquivo registra o escopo da operação e evita tratar os 177 Markdown como se fossem todos propriedade editorial do ERP.
