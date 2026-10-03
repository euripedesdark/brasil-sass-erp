# Por que a NFS-e de São Paulo parou de funcionar

Diagnóstico do crash-loop de 26/09/2026, 01:10 → 09:47.

Este é o registro do que **realmente** aconteceu, para não se contar de novo a
história errada.

## Resposta curta

A unit do `nfse-sp-api` apontava para um JDK que não existe nesta máquina
(`/opt/oracle-jdk-21`). As outras duas unidades morriam antes de rodar. Os três
serviços crash-looparam 8,6 horas, e ninguém percebeu porque nada alertava.

Nada disso tem relação com o CNPJ alfanumérico, com o retorno da prefeitura,
com o proxy chunked, nem com o dump do banco. São coisas separadas.

## A linha do tempo

| Quando | O quê |
|---|---|
| 25/09 21:34 | construção da API de NFS-e de São Paulo |
| 25/09 22:41 | código municipal do serviço e guarda da NFS-e no Mongo |
| **25/09 22:59** | **nota 14 emitida — funcionou** |
| 25/09 23:22 | ruby como fallback, com proxy de failover na 4567 |
| 25/09 23:49 | MinIO fixado e `subir-dev.sh` com subida em ordem |
| **26/09 01:10** | **começa o crash-loop** |
| 26/09 09:47 | units corrigidas (caminho com espaço citado, porta 4568) |

Entre a nota 14 funcionar e o crash-loop começar, os commits que existem são
`fe5c1b9b`, `20179364` e `b4dac5e1`. O último mexeu em `.gitignore`,
`installbase.sh`, um teste e `subir-dev.sh` — **nada de NFS-e**. A unit do
`nfse-sp-api` foi criada/configurada fora do git, em `/etc/systemd/system`, e é
por isso que o commit não aparece em nenhum `git log`.

## As três causas, uma por serviço

### 1. `nfse-sp-api` — JDK inexistente

```
nfse-sp-api.service: Unable to locate executable '/opt/oracle-jdk-21/bin/java': No such file or directory
nfse-sp-api.service: Failed at step EXEC spawning /opt/oracle-jdk-21/bin/java
```

A unit apontava para um JDK em `/opt` que não está instalado. O passo `EXEC` do
systemd falha antes do processo existir, então o log do Spring nunca aparecia e
ninguém via "a aplicação não sobe" — via "não achou o executável".

Corrigido para o JDK do sdkman, que é o que está na máquina.

### 2. Caminho com espaço não citado no `ExecStart`

```
/etc/systemd/system/nfse-sp-api.service:3: Invalid URL, ignoring: Repos/BRASIL-SAAS-ERP/src/main/resources/microservices/nfse-sp-api/README.md
```

O repositório está em `.../GIT Repos/BRASIL-SAAS-ERP/...`. Com o espaço não citado,
o systemd corta o caminho em dois, e o `java` recebia
`/home/euripedes/OneDrive/python/projetos-leno/GIT` como se fosse o jar:

```
Error: Unable to access jarfile /home/euripedes/OneDrive/python/projetos-leno/GIT
```

A segunda linha é consequência da primeira, não uma causa separada.

O aviso `Invalid URL` é o `Documentation=` da unit, não o `ExecStart` — mas aponta
para o mesmo problema de quoting.

### 3. `nfse-failover` e `nfse-sp-bridge` — `status=2/INVALIDARGUMENT`

```
nfse-failover.service: Main process exited, code=exited, status=2/INVALIDARGUMENT
```

`INVALIDARGUMENT` é o systemd recusando a **unit**, não o programa falhando. O
processo nunca chega a ser executado. Diferente do `nfse-sp-api`, aqui o passo
`EXEC` nem aparece no log.

Causa provável: as duas unidades dependem do `ruby` do mise
(`~/.local/share/mise/installs/ruby/3/bin`), e o `PATH` da unit não o tinha. Um
shell com `PATH` incompleto e um executável que o systemd resolve pelo `PATH`
produzem exatamente isto.

O log do proxy confirma: ao subir, ele imprimia
`bash: linha 1: cd: número excessivo de argumentos` — o mesmo problema de quoting
do caminho com espaço, agora dentro do health check.

## Por que ninguém percebeu

O health check do proxy é um GET em `/api/nfse-sp/status`. GET **não tem corpo**.

Isso importa para um defeito que existe em paralelo (ver
`docs/pesquisa/pesquisa-failover-chunked.md`): um health check que não exercita o caminho
principal deixa o serviço passar semanas quebrado sem nenhum sinal. O mesmo
raciocínio vale aqui — três serviços em crash-loop por 8,6 horas sem alarme,
porque nada verificava se estavam **de pé de fato**, e sim se o endpoint de
status respondia, o que só faz sentido se o processo estivesse vivo.

`Restart=always` com `RestartSec=10` fez o pior: 2.024 reinícios acumulados em
quase 9 horas, sem que nada disparasse alerta. O contador chegou a 2.024 e
continuou subindo.

## O que não explicar

- **A nota 14 funcionou às 22:59 e o crash-loop começou 01:10.** A unit do
  `nfse-sp-api` já apontava para o JDK inexistente durante as duas janelas, ou
  o caminho com quebra já estava presente. O journal não cobre antes de 01:10, e
  o log da API foi recriado às 09:47, então **não há como dizer qual das
  alterações de unit entrou em que momento**. A ordem exata é desconhecida.
- **O proxy chunked.** É um defeito real, provado por teste, e independente.
  Não tem relação com o crash-loop: são coisas que se descobriram em separado.

## Como conferir de novo

```bash
# os três de pé, sem reinício acumulado
systemctl is-active nfse-sp-api nfse-failover nfse-sp-bridge
systemctl show nfse-sp-api -p NRestarts          # tem que ser 0

# as três portas respondem
for p in 4567 4568 4569; do curl -s -m 5 -o /dev/null -w "$p %{http_code}\n" \
  http://127.0.0.1:$p/api/nfse-sp/status; done
```

`NRestarts` growing é o sinal. Verificar `is-active` sozinho não basta: um
serviço em `Restart=always` fica `active` mesmo falhando o tempo todo, porque o
systemd só marca `failed` quando as tentativas são esgotadas — e com
`RestartSec=10` e sempre ativo, isso praticamente não acontece.

## O que deveria ter existido

Um alerta que olhasse `NRestarts`, não só `is-active`. Os três serviços
respondem e o health check passa, então qualquer verificação que use só o endpoint
diz que está tudo bem com um serviço que não sobe há 8 horas.
