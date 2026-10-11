# Ambiente STAGING

Este ambiente e separado da producao e **nao aponta para dominio publico**.

- Aplicacao Spring Boot: porta `8081`, acesso direto por `http://localhost:8081` no proprio servidor.
- Diretorio de deploy: `/opt/brasil-saas-erp/staging`.
- Unit systemd: `brasil-saas-erp-staging.service`.
- Variaveis: `/etc/brasil-saas/staging.env`, criado a partir de `staging.env.example`.
- Banco PostgreSQL: database `brasil-saas-staging`; nao usar `brasil-saas` de producao.
- MongoDB: database `brasil-saas-staging`.
- Redis: database index `1`.
- RabbitMQ: virtual host `/staging`.
- MinIO: bucket `brasil-saas-staging`.
- IAM/AD compartilhado desabilitado no perfil para impedir autenticacao acidental contra o ambiente produtivo.
- Stripe success/cancel URLs ficam em `localhost:8081` ate que um hostname de staging seja definido deliberadamente.

## Seguranca e ativacao

1. Crie banco, usuario e credenciais exclusivos de staging. Nunca copie senhas, chaves JWT, credenciais de mensageria ou Stripe da producao.
2. Instale o arquivo de ambiente em `/etc/brasil-saas/staging.env`, dono `root:root`, permissao `0600`.
3. Confirme que a porta `8081` nao esta publicada no proxy de producao.
4. Instale a unit de `systemd_units/brasil-saas-erp-staging.service`, execute `systemctl daemon-reload` e habilite/inicie a unit quando o servidor estiver preparado.
5. Valide `curl -fsS http://127.0.0.1:8081/actuator/health`.

Nao configure DNS, Nginx ou URL publica para staging sem uma decisao explicita do operador. O workflow de build nao deve trocar nem reiniciar o servico de producao.
