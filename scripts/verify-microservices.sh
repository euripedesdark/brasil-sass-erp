#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="$ROOT/src/main/resources/microservices"

required=(
  "Java_NFe"
  "Java_CTe"
  "Java_MDFe"
  "Java-Efd-Icms"
  "Java-Efd-Contribuicoes"
  "Java_Certificado"
  "Java_Pdf_Signature"
  "nfe"
  "nfse"
  "nfse-sp-bridge"
  "nfse_prefeitura_sp"
  "NFSe-SaoPaulo-SP"
  "esocial"
  "spring-ai"
)

for item in "${required[@]}"; do
  test -e "$BASE/$item" || { echo "ERROR: missing microservice: $item"; exit 1; }
done

for pom in   "$BASE/Java_NFe/pom.xml"   "$BASE/Java-Efd-Icms/pom.xml"   "$BASE/Java-Efd-Contribuicoes/pom.xml"   "$BASE/Java_Certificado/pom.xml"   "$BASE/Java_Pdf_Signature/pom.xml"   "$BASE/nfe/pom.xml"   "$BASE/nfse/pom.xml"; do
  test -f "$pom" || { echo "ERROR: missing POM: $pom"; exit 1; }
done

test -d "$BASE/esocial/src/esocial-jt-service/src/main/java" || {
  echo "ERROR: eSocial service source tree is missing"
  exit 1
}

test -f "$BASE/nfse-sp-bridge/app.rb" || {
  echo "ERROR: NFSe São Paulo bridge source is missing"
  exit 1
}

echo "Brasil SaaS ERP microservices verification: OK"
echo "Required fiscal/eSocial components are present."
