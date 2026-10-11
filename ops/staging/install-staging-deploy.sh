#!/usr/bin/env bash
set -Eeuo pipefail

# Instala o deploy isolado de homologação. Não altera a unit de produção.
[[ $EUID -eq 0 ]] || { echo "Execute com sudo/root." >&2; exit 1; }

DEPLOY_USER="erp-deploy"
STAGING_ROOT="/opt/brasil-saas-erp/staging"
RELEASE_ROOT="/opt/brasil-saas-erp-deploy"
ENV_FILE="/etc/brasil-saas/staging.env"
PROD_ENV="/etc/brasil-saas/erp.env"
SCRIPT="/usr/local/sbin/deploy-brasil-saas-erp-staging"

id "$DEPLOY_USER" >/dev/null 2>&1 || { echo "Usuário $DEPLOY_USER não existe." >&2; exit 1; }
[[ -s "$ENV_FILE" ]] || {
  echo "Falta $ENV_FILE. Crie-o com credenciais e DB_URL exclusivos de homologação; não copie o banco de produção." >&2
  exit 2
}
[[ -x /usr/bin/java ]] || { echo "Java não encontrado em /usr/bin/java." >&2; exit 1; }
[[ -x /usr/bin/curl ]] || { echo "curl não encontrado em /usr/bin/curl." >&2; exit 1; }
[[ -s "$PROD_ENV" ]] || { echo "Não encontrei $PROD_ENV; não consigo validar a separação do ambiente." >&2; exit 1; }

PROD_DB_URL="$(sed -n 's/^DB_URL=//p' "$PROD_ENV" | tail -n1)"
STAGING_DB_URL="$(sed -n 's/^DB_URL=//p' "$ENV_FILE" | tail -n1)"
[[ -n "$STAGING_DB_URL" ]] || { echo "DB_URL ausente em $ENV_FILE." >&2; exit 1; }
if [[ -n "$PROD_DB_URL" && "$PROD_DB_URL" == "$STAGING_DB_URL" ]]; then
  echo "Recusado: DB_URL de homologação é igual ao de produção." >&2
  exit 1
fi

install -d -o root -g "$DEPLOY_USER" -m 0750 "$STAGING_ROOT"
install -d -o "$DEPLOY_USER" -g "$DEPLOY_USER" -m 0750 "$RELEASE_ROOT"
chown root:"$DEPLOY_USER" "$ENV_FILE"
chmod 0640 "$ENV_FILE"

cat > "$SCRIPT" <<'DEPLOY_SCRIPT'
#!/usr/bin/env bash
set -Eeuo pipefail
[[ $EUID -eq 0 ]] || { echo "Este script precisa ser chamado pelo sudo permitido." >&2; exit 1; }
SOURCE="/opt/brasil-saas-erp-deploy/staging.release.jar"
TARGET="/opt/brasil-saas-erp/staging/brasil-saas-erp.jar"
SERVICE="brasil-saas-erp-staging.service"
HEALTH_URL="http://127.0.0.1:8090/actuator/health"
[[ -s "$SOURCE" ]] || { echo "JAR de homologação ausente: $SOURCE" >&2; exit 1; }
jar tf "$SOURCE" | grep -q '^BOOT-INF/' || { echo "JAR inválido." >&2; exit 1; }
systemctl cat "$SERVICE" >/dev/null
BACKUP=""
if [[ -s "$TARGET" ]]; then BACKUP="$(mktemp /var/tmp/erp-staging-backup.XXXXXX.jar)"; cp -p "$TARGET" "$BACKUP"; fi
TMP="$(mktemp /opt/brasil-saas-erp/staging/.release.XXXXXX)"
cleanup() { rm -f "$TMP"; [[ -z "$BACKUP" ]] || rm -f "$BACKUP"; }
rollback() {
  echo "Health check falhou; revertendo homologação." >&2
  if [[ -n "$BACKUP" && -s "$BACKUP" ]]; then install -o root -g erp-deploy -m 0640 "$BACKUP" "$TARGET"; fi
  systemctl restart "$SERVICE" || true
  exit 1
}
trap cleanup EXIT
install -o root -g erp-deploy -m 0640 "$SOURCE" "$TMP"
mv -f "$TMP" "$TARGET"
systemctl restart "$SERVICE"
for _ in $(seq 1 60); do
  if curl --fail --silent --show-error --max-time 3 "$HEALTH_URL" -o /tmp/erp-staging-health.json 2>/dev/null &&
     python3 -c 'import json; assert json.load(open("/tmp/erp-staging-health.json")).get("status") == "UP"' 2>/dev/null; then
    cat /opt/brasil-saas-erp-deploy/staging.trigger > /opt/brasil-saas-erp-deploy/staging.status
    chmod 0644 /opt/brasil-saas-erp-deploy/staging.status
    echo "Deploy de homologação saudável."
    exit 0
  fi
  sleep 5
done
journalctl -u "$SERVICE" -n 80 --no-pager >&2 || true
rollback
DEPLOY_SCRIPT
chown root:root "$SCRIPT"
chmod 0750 "$SCRIPT"

cat > /etc/systemd/system/brasil-saas-erp-staging.service <<'STAGING_UNIT'
[Unit]
Description=Brasil SaaS ERP - Homologação isolada
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=erp-deploy
Group=erp-deploy
WorkingDirectory=/opt/brasil-saas-erp/staging
EnvironmentFile=/etc/brasil-saas/staging.env
Environment=SPRING_PROFILES_ACTIVE=hom
Environment=SERVER_PORT=8090
ExecStart=/usr/bin/java -jar /opt/brasil-saas-erp/staging/brasil-saas-erp.jar --spring.profiles.active=hom --server.port=8090
Restart=on-failure
RestartSec=5
SuccessExitStatus=143
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadOnlyPaths=/opt/brasil-saas-erp/staging
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
STAGING_UNIT

cat > /etc/systemd/system/erp-staging-deploy-run.service <<'RUN_UNIT'
[Unit]
Description=Executar deploy isolado do ERP em homologação
After=network.target

[Service]
Type=oneshot
User=erp-deploy
Group=erp-deploy
WorkingDirectory=/opt/brasil-saas-erp-deploy
ExecStart=/usr/bin/sudo -n /usr/local/sbin/deploy-brasil-saas-erp-staging
RUN_UNIT

cat > /etc/systemd/system/erp-staging-deploy-watch.path <<'PATH_UNIT'
[Unit]
Description=Monitorar solicitações de deploy do ERP em homologação

[Path]
PathChanged=/opt/brasil-saas-erp-deploy/staging.trigger
Unit=erp-staging-deploy-run.service

[Install]
WantedBy=multi-user.target
PATH_UNIT

# Acrescenta a autorização específica de homologação, mantendo a autorização
# já existente para o script de produção e sem conceder sudo irrestrito.
SUDO_TMP="$(mktemp)"
if [[ -f /etc/sudoers.d/erp-deploy ]]; then cat /etc/sudoers.d/erp-deploy > "$SUDO_TMP"; fi
grep -qxF "$DEPLOY_USER ALL=(root) NOPASSWD: $SCRIPT" "$SUDO_TMP" || echo "$DEPLOY_USER ALL=(root) NOPASSWD: $SCRIPT" >> "$SUDO_TMP"
install -o root -g root -m 0440 "$SUDO_TMP" /etc/sudoers.d/erp-deploy
rm -f "$SUDO_TMP"

visudo -cf /etc/sudoers
systemd-analyze verify /etc/systemd/system/brasil-saas-erp-staging.service /etc/systemd/system/erp-staging-deploy-run.service /etc/systemd/system/erp-staging-deploy-watch.path
systemctl daemon-reload
systemctl enable --now erp-staging-deploy-watch.path

echo "Homologação configurada; serviço de aplicação não foi iniciado porque exige env e banco exclusivos válidos."
echo "Próximo passo: validar /etc/brasil-saas/staging.env e então executar: systemctl enable --now brasil-saas-erp-staging.service"
