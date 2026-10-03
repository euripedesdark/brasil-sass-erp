#!/usr/bin/env bash
# Sobe o nfse-sp-bridge (NFS-e Sao Paulo) na porta 4569, que e a de fallback.
# A publica e a 4567, do nfse-failover. Antes a bridge ficava na 4567 porque era
# a implementacao unica.
#
# Regras da prefeitura de São Paulo JA VALIDADAS contra o WebService real
# (setembro/2026):
#   - certificado A1 é obrigatório (empresa LTDA)
#   - o certificado em uso é RC2-40-CBC: exige o provider legacy do OpenSSL
#   - empresa do Simples Nacional é OBRIGADA no leiaute 1 (NFSE_SP_XSD_VERSION=1)
#     — a prefeitura recusa com o erro 641 se mandar o leiaute 2
set -euo pipefail

export PATH="$HOME/.local/bin:$HOME/.local/share/mise/shims:$PATH"

cd "$(dirname "$(readlink -f "$0")")"

export NFSE_SP_CERT_PATH="${NFSE_SP_CERT_PATH:-/etc/brasil-saas/certs/sp_cert.p12}"
export NFSE_SP_CERT_PASS="${NFSE_SP_CERT_PASS:-}"

# 1 = leiaute clássico. 2 = leiaute IBS/CBS (LC 214/2025), só para
# empresas do regime normal. A prefeitura valida contra o cadastro dela
# e responde 641 se o Regime não bater com o leiaute enviado.
export NFSE_SP_XSD_VERSION="${NFSE_SP_XSD_VERSION:-1}"

# O .pfx emitido pela ICP-Brasil costuma vir cifrado com RC2-40-CBC, que o
# OpenSSL 3 removeu do provider default. Sem apontar OPENSSL_MODULES para o
# diretório do módulo, `OpenSSL::PKCS12.new` falha com
# "PKCS12_parse: unsupported (Algorithm (RC2-40-CBC : 0))".
export OPENSSL_MODULES="${OPENSSL_MODULES:-/usr/lib/x86_64-linux-gnu/ossl-modules}"

if [ ! -r "$NFSE_SP_CERT_PATH" ]; then
  echo "certificado não legível: $NFSE_SP_CERT_PATH" >&2
  exit 1
fi
if [ -z "$NFSE_SP_CERT_PASS" ]; then
  echo "NFSE_SP_CERT_PASS não definido" >&2
  exit 1
fi

echo "certificado : $NFSE_SP_CERT_PATH"
echo "leiaute XSD : $NFSE_SP_XSD_VERSION  (1 = clássico, 2 = IBS/CBS)"
echo "porta       : ${NFSE_SP_BRIDGE_PORTA:-4569}"

exec bundle exec rackup -p "${NFSE_SP_BRIDGE_PORTA:-4569}" -o 0.0.0.0 config.ru
