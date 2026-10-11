# Backup PostgreSQL a cada 2 horas

O serviço gera um dump PostgreSQL em formato custom/comprimido fora do repositório e do diretório da aplicação:

- Destino: `/var/backups/brasil-saas-erp/`
- Frequência: a cada 2 horas, com execução recuperada após indisponibilidade do servidor
- Retenção padrão: 14 dias
- Permissões: diretório `0700`, arquivos `0600`
- Validação: `pg_restore --list` antes de publicar o arquivo final
- Logs: journal do systemd

## Instalação no servidor

Execute como administrador na máquina do ERP:

```bash
sudo install -o root -g root -m 0750 ops/db-backup/brasil-saas-db-backup /usr/local/sbin/brasil-saas-db-backup
sudo install -o root -g root -m 0644 ops/db-backup/brasil-saas-db-backup.service /etc/systemd/system/brasil-saas-db-backup.service
sudo install -o root -g root -m 0644 ops/db-backup/brasil-saas-db-backup.timer /etc/systemd/system/brasil-saas-db-backup.timer
sudo install -d -o root -g root -m 0700 /var/backups/brasil-saas-erp
sudo systemctl daemon-reload
sudo systemctl enable --now brasil-saas-db-backup.timer
sudo systemctl start brasil-saas-db-backup.service
```

## Verificação

```bash
systemctl list-timers brasil-saas-db-backup.timer
systemctl status brasil-saas-db-backup.timer --no-pager
journalctl -u brasil-saas-db-backup.service -n 80 --no-pager
sudo ls -lh /var/backups/brasil-saas-erp/
```

O serviço lê as variáveis `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` de `/etc/brasil-saas/erp.env`. Confirme que esses nomes existem no arquivo antes de habilitar. O backup não altera nem reinicia o serviço do ERP.
