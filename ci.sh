```bash
#!/usr/bin/env bash
set -Eeuo pipefail

RUNNER_DIR="/opt/actions-runner"
RUNNER_USER="ghrunner"

echo "=== Instalando serviço GitHub Actions Runner ==="

if [[ $EUID -ne 0 ]]; then
    echo "Execute como root: sudo bash instalar-runner-servico.sh"
    exit 1
fi

if [[ ! -x "$RUNNER_DIR/svc.sh" ]]; then
    echo "ERRO: $RUNNER_DIR/svc.sh não encontrado."
    echo "Configure o runner primeiro pelo config.sh."
    exit 1
fi

if ! id "$RUNNER_USER" >/dev/null 2>&1; then
    echo "ERRO: usuário $RUNNER_USER não existe."
    exit 1
fi

cd "$RUNNER_DIR"

echo "1/3 - Instalando serviço..."
./svc.sh install "$RUNNER_USER"

echo "2/3 - Habilitando e iniciando serviço..."
./svc.sh start

echo "3/3 - Verificando status..."
./svc.sh status

echo
echo "=== Verificação systemd ==="
systemctl list-units --type=service --all | grep -i actions.runner || true

echo
echo "Concluído. Confira em GitHub > Settings > Actions > Runners."
echo "O runner deverá aparecer como Idle quando estiver conectado e disponível."
```
