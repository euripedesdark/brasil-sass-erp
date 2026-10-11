# Backup PostgreSQL nativo: completo diário + incremental a cada 2 horas

**Estado:** preparado no branch `ops/postgres-backup-systemd`. Não instalado nem executado em servidor.

## Comportamento

- A cada duas horas, em UTC, o timer chama um único serviço systemd. O disparo de 00:00 UTC é reservado para o backup completo diário.
- O script gera um backup completo de cluster se não houver uma base válida, no primeiro disparo do dia em 00:00 UTC, ou se a última base completa tiver 26 horas ou mais (recuperação após timer/servidor indisponível).
- Entre bases completas, gera incrementais nativos com pg_basebackup --incremental, usando o manifesto do backup anterior como referência. Isso cria uma cadeia de dependências.
- Destino: /var/backups/brasil-saas-erp/postgresql/, fora do repositório e da pasta da aplicação.
- Cada diretório de backup contém o backup_manifest nativo com checksums CRC32C (menos custoso em CPU; o manifesto mantém seu próprio SHA-256). Um catálogo separado em catalog/*.meta registra tipo, base completa, pai, horário e versão.
- Backup completo: validação por pg_verifybackup.
- Incremental: verifica a existência do manifesto atual e percorre os metadados PARENT até a base completa, recusando pais ausentes, bases divergentes e ciclos. Não executa pg_combinebackup em cada disparo, pois isso reconstruiria um backup sintético potencialmente grande e aumentaria I/O. A combinação e a restauração precisam ser testadas separadamente antes de produção.
- Retenção padrão de 14 dias por cadeia. O script preserva a base completa mais recente e remove metadados/diretórios dos backups ligados a bases completas mais antigas que ultrapassaram a retenção. Antes de produção, o comportamento deve ser validado com uma matriz de cenários de retenção; não considerar a política validada até esse teste.
- A taxa de transferência de dados é limitada por padrão a 20 MB/s (BACKUP_MAX_RATE=20M) para reduzir o impacto de I/O. Ajustável via ambiente.
- Diretórios e manifestos/metadados são restritos a root (0700/0600). Um lock impede duas execuções simultâneas.
- Não reinicia nem altera diretamente o serviço do ERP.

## Requisitos obrigatórios antes da instalação

1. PostgreSQL 17 ou superior no servidor. Incrementais nativos não funcionam em versões anteriores.
2. Binários de cliente pg_basebackup, pg_combinebackup e pg_verifybackup da mesma versão principal do servidor; também psql, python3 e flock.
3. Configuração do servidor summarize_wal = on. O script verifica essa configuração e aborta sem criar backup se estiver desligada. A alteração da configuração do PostgreSQL não é feita automaticamente.
4. Por padrão, o script tenta reutilizar DB_USER/DB_PASSWORD para replicação. Ele consulta os privilégios da conta; se ela não tiver REPLICATION/SUPERUSER, usa a role postgres pela regra local existente de replicação para 127.0.0.1. Isso evita criar usuário ou alterar pg_hba.conf, mas depende dessa regra local de trust continuar ativa. Se o ambiente mudar, configure BACKUP_PGUSER/BACKUP_PGPASSWORD explicitamente com uma conta já autorizada. O usuário de backup precisa de REPLICATION ou SUPERUSER e max_wal_senders deve ter capacidade disponível.
5. DB_URL, DB_USER e DB_PASSWORD (ou variáveis SPRING_DATASOURCE_*) para consultar a versão e a configuração. A URL JDBC deve conter os parâmetros TLS se o servidor os exigir; o script transporta sslmode, sslrootcert, sslcert e sslkey para libpq.
6. Espaço livre suficiente para pelo menos uma cópia completa adicional e para os incrementais. O backup fica no mesmo servidor/disco: protege de alguns incidentes lógicos, mas não de falha/perda do disco inteiro.

O backup nativo é de todo o cluster PostgreSQL, incluindo todos os bancos e tablespaces, e não somente o banco brasil-saas. É diferente de pg_dump, que faz um backup lógico de banco/objetos.

## Instalação — não executar até autorização explícita

Depois de revisar os requisitos e aprovar a instalação na máquina autorizada, a instalação será feita exclusivamente nessa máquina. Não executar em host não autorizado.

```bash
sudo install -o root -g root -m 0750 ops/db-backup/brasil-saas-postgres-backup /usr/local/sbin/brasil-saas-postgres-backup
sudo install -o root -g root -m 0644 ops/db-backup/brasil-saas-postgres-backup.service /etc/systemd/system/brasil-saas-postgres-backup.service
sudo install -o root -g root -m 0644 ops/db-backup/brasil-saas-postgres-backup.timer /etc/systemd/system/brasil-saas-postgres-backup.timer
sudo install -d -o root -g root -m 0700 /var/backups/brasil-saas-erp/postgresql
sudo systemctl daemon-reload
# Não habilitar o timer nesta etapa.
# Primeiro, executar uma cópia controlada após autorização explícita e validar o resultado/restauração.
# Só depois de aprovar o teste, habilitar o agendamento:
# sudo systemctl enable --now brasil-saas-postgres-backup.timer
```

O script reutiliza DB_URL, DB_USER e DB_PASSWORD de /etc/brasil-saas/erp.env. BACKUP_PGUSER/BACKUP_PGPASSWORD são opcionais e só precisam ser definidos para substituir a seleção automática. Não colocar segredos no repositório ou na linha de comando.

## Verificação operacional

```bash
systemctl list-timers brasil-saas-postgres-backup.timer
systemctl status brasil-saas-postgres-backup.timer --no-pager
journalctl -u brasil-saas-postgres-backup.service -n 100 --no-pager
find /var/backups/brasil-saas-erp/postgresql -maxdepth 2 -type f -printf '%p %s bytes\n' | sort
```

## Recuperação da cadeia incremental

Para reconstruir um backup, seguir os vínculos PARENT do ponto desejado até a base completa, inverter a sequência para ficar base-primeiro e executar pg_combinebackup com a base e cada incremental intermediário na ordem correta para produzir um diretório de backup completo sintético. Exemplo ilustrativo (substituir pelos nomes reais e incluir cada incremental intermediário):

```bash
sudo -u postgres pg_combinebackup \
  /var/backups/brasil-saas-erp/postgresql/full-AAAAMMDDTHHMMSSZ \
  /var/backups/brasil-saas-erp/postgresql/incremental-AAAAMMDDTHHMMSSZ \
  --output=/var/backups/brasil-saas-erp/restore-synthetic
sudo -u postgres pg_verifybackup /var/backups/brasil-saas-erp/restore-synthetic
```

A ordem e a compatibilidade da cadeia devem ser confirmadas pelos metadados antes da combinação. O diretório sintético deve ser recuperado em um procedimento controlado, com permissões, configuração, tablespaces e WAL tratados conforme a documentação da versão do PostgreSQL. Não copiar o diretório recuperado sobre o cluster de produção em execução. O procedimento de recuperação deve ser testado em ambiente isolado antes de ser considerado validado.

## Impactos e limites

- O backup completo lê o cluster inteiro e pode gerar I/O, tráfego local e uso de CPU; o incremental tende a transferir blocos alterados, mas também exige leitura e processamento de metadados/WAL summaries.
- --max-rate=20M limita a taxa de transferência de dados, mas não garante ausência de impacto. --checkpoint=spread evita solicitar checkpoint rápido, à custa de possível maior duração.
- A primeira execução será completa; não há backups incrementais até existir uma base válida.
- O script falha com mensagem clara se a versão for anterior a 17, se os binários não corresponderem à versão principal, se summarize_wal estiver desligado, ou se a autenticação/privilégios não permitirem a operação.
- Manifestos e verificações de ferramenta não substituem um teste real de restauração. Ainda não foi feito teste de restore nem teste em servidor.
