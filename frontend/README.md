# Pasta `frontend/` na raiz — DEPRECIADA

Esta pasta **não é usada** pelo build nem pelo runtime.

O frontend funcional do Brasil SaaS ERP está em:

```
src/main/resources/static/react/
```

- `package.json` / Vite: `src/main/resources/static/react/`
- Bundle servido: `src/main/resources/static/dist/`
- Dockerfile e `cobertura_frontend.py` apontam apenas para `static/react`

Os arquivos aqui eram apenas `.gitkeep` (esqueleto vazio).  
Não adicione código nesta pasta. Ela será removida completamente.
