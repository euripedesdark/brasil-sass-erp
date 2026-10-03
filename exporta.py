import subprocess
import time
import sys

# Configurações
DUMP_FILE = "dump.sql"
CONN_STR = "postgresql://neondb_owner:npg_FQZ49exLAPtO@ep-proud-river-acthxzvt-pooler.sa-east-1.aws.neon.tech/SYSFLUXO?sslmode=require"

# Função para rodar o comando com retry
def run_with_retry(max_retries=3, wait=10):
    attempt = 1
    while attempt <= max_retries:
        print(f"Tentativa {attempt}/{max_retries}...")
        try:
            # Executa psql
            process = subprocess.Popen(
                ["psql", CONN_STR, "-f", DUMP_FILE],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                universal_newlines=True
            )

            # Barra de progresso simples
            while True:
                line = process.stdout.readline()
                if not line and process.poll() is not None:
                    break
                if line:
                    sys.stdout.write("█")  # bloco de progresso
                    sys.stdout.flush()

            stdout, stderr = process.communicate()
            if process.returncode == 0:
                print("\n✅ Importação concluída com sucesso!")
                return True
            else:
                print("\n❌ Erro na importação:")
                print(stderr)
        except Exception as e:
            print(f"Erro inesperado: {e}")

        attempt += 1
        if attempt <= max_retries:
            print(f"Aguardando {wait} segundos antes do retry...")
            time.sleep(wait)

    print("❌ Todas as tentativas falharam.")
    return False

if __name__ == "__main__":
    run_with_retry()
