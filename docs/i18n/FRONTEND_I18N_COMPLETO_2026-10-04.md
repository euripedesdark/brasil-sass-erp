# Internacionalização completa do frontend — 2026-10-04

## Objetivo

Fechar em um único bloco a internacionalização do frontend para os quatro idiomas já oferecidos na tela de login:

- Português (Brasil) — `pt-BR`
- English — `en-US`
- Español — `es-ES`
- Français — `fr-FR`

## Cobertura

Os quatro catálogos possuem exatamente **1.096 chaves**, sem chaves ausentes entre idiomas.

A infraestrutura existente com i18next continua sendo a fonte principal. O bloco acrescenta uma ponte global para componentes legados que ainda possuem texto literal em português no JSX.

### Ponte global

`components/shared/I18nDomBridge.jsx`:

- observa conteúdo renderizado dinamicamente;
- traduz textos literais em português usando o próprio catálogo `pt-BR → idioma selecionado`;
- trata textos com interpolação `{{variavel}}`;
- traduz `placeholder`, `title`, `aria-label`, `aria-placeholder` e `alt`;
- acompanha mudanças de idioma sem recarregar a página;
- funciona também para componentes PrimeReact e telas legadas que ainda não migraram cada literal individual para `t()`.

Isso permite que a seleção de idioma da tela de login seja respeitada em todo o ERP, inclusive nas telas antigas.

## Persistência

O idioma continua armazenado em:

`brasilclouderp_language`

A detecção mantém:

1. idioma escolhido pelo usuário;
2. idioma do navegador;
3. fallback para português.

O atributo `lang` do documento também acompanha o idioma ativo.

## Critério

Não foram criadas quatro implementações independentes das telas. Todos os idiomas utilizam o mesmo frontend, os mesmos componentes, as mesmas rotas e o mesmo catálogo estruturado.

A única diferença de apresentação é o idioma ativo.

## Resultado

O frontend passa a tratar Português, Inglês, Espanhol e Francês como idiomas completos da aplicação, em vez de o seletor de idioma existir apenas na autenticação.

A manutenção futura deve adicionar qualquer nova string ao catálogo i18n; a ponte global existe como compatibilidade para o legado e não como substituição da prática de usar `useTranslation()` em componentes novos.
