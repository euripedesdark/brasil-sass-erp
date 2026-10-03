#!/usr/bin/env python3
"""Monta o truststore JKS que a SVRS de homologacao do MDF-e exige.

POR QUE ISSO EXISTE

O `homologacao.cacerts` que acompanha a fincatto tem 68 entradas, e todas sao
de NFC-e (os alias comecam com `hnfe.`, `hnfe.fazenda.mg.gov.br`). Nao tem a CA
do MDF-e. Com ele, o ERP sobe e o primeiro HTTPS morre com:

    (certificate_unknown) PKIX path building failed:
    SunCertPathBuilderException: unable to find valid certification path
    to requested target

O que o servidor de homologacao da SVRS apresenta e certificado ICP-Brasil:
`CN=*.svrs.rs.gov.br`, emitido pela `Autoridade Certificadora do SERPRO SSLv1`,
que vem da Raiz Brasileira v10. Essas CAs publicas estao no truststore do
sistema, em /etc/ssl/certs/ca-certificates.crt.

A cadeia correta e a uniao das duas coisas: as CAs publicas que o SO ja confia,
mais as 68 da lib que servem para os webservices de NFC-e. Nenhuma sozinha
serve.

O QUE ISTO NAO FAZ

Nao desliga a validacao de TLS. Desligar e o jeito rapido de fazer o teste
passar e de emitir MDF-e com um certificado que nao e seu, num ambiente que nao
e de producao. Aqui a validacao continua ligada: se a SEFAZ apresentar
certificado fora dessa cadeia, o ERP falha, que e o comportamento correto.

Nao ha atalho com `TrustAllCerts` em nenhum lugar deste codigo, e se alguem
adicionar, o build e para avisar.
"""
import os
import re
import subprocess
import sys
import tempfile

DESTINO = (sys.argv[1] if len(sys.argv) > 1
           else "/tmp/opencode/certs-homologacao/cadeia-mdfe.jks")
SENHA = "mdfe-homologacao"
CACERTS_SO = "/etc/ssl/certs/ca-certificates.crt"
CACERTS_LIB = "/tmp/opencode/certs-homologacao/homologacao.cacerts"


def importar(jks, arq_pem, alias):
    r = subprocess.run(
        ["keytool", "-importcert", "-noprompt", "-trustcacerts",
         "-alias", alias, "-file", arq_pem,
         "-keystore", jks, "-storetype", "JKS", "-storepass", SENHA],
        capture_output=True, text=True)
    return r.returncode == 0


def fatia_pem(texto):
    """Separa um bundle PEM em certificados individuais."""
    partes = re.findall(
        r"-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----",
        texto, re.S)
    return partes


def main():
    if not os.path.exists(CACERTS_SO):
        print("  ERRO: %s nao existe" % CACERTS_SO)
        return 1

    tmp = tempfile.mkdtemp(prefix="cadeia-")
    jks = os.path.join(tmp, "c.jks")
    total = 0
    falhas = 0

    # 1. CAs publicas do sistema, uma a uma. O keytool -importcert em bundle
    #    so importa o primeiro certificado, entao fatiar e obrigatorio.
    with open(CACERTS_SO, encoding="utf-8") as f:
        publicas = fatia_pem(f.read())
    for i, p in enumerate(publicas):
        arq = os.path.join(tmp, "so-%d.pem" % i)
        with open(arq, "w", encoding="utf-8") as f:
            f.write(p + "\n")
        if importar(jks, arq, "so-%d" % i):
            total += 1
        else:
            falhas += 1
    print("  CAs publicas do SO: %d importadas, %d recusadas"
          % (total, falhas))

    # 2. as 68 da lib, alias por alias
    n_lib = 0
    if os.path.exists(CACERTS_LIB):
        listagem = subprocess.run(
            ["keytool", "-list", "-keystore", CACERTS_LIB, "-storetype", "JKS",
             "-storepass", SENHA], capture_output=True, text=True).stdout
        for linha in listagem.splitlines():
            if ", trustedCertEntry" not in linha:
                continue
            alias = linha.split(",")[0].strip()
            pem = subprocess.run(
                ["keytool", "-exportcert", "-rfc", "-keystore", CACERTS_LIB,
                 "-storetype", "JKS", "-storepass", SENHA, "-alias", alias],
                capture_output=True, text=True).stdout
            if not pem.strip():
                continue
            arq = os.path.join(tmp, "lib-%d.pem" % n_lib)
            with open(arq, "w", encoding="utf-8") as f:
                f.write(pem)
            if importar(jks, arq, "fincatto-%d" % n_lib):
                n_lib += 1
    print("  CAs da fincatto (NFC-e): %d" % n_lib)

    if total == 0:
        print("  ERRO: nenhuma CA importada")
        return 1

    os.makedirs(os.path.dirname(DESTINO), exist_ok=True)
    with open(jks, "rb") as f, open(DESTINO, "wb") as g:
        g.write(f.read())

    print("  truststore: %s" % DESTINO)
    print("  total: %d CAs" % (total + n_lib))
    return 0


if __name__ == "__main__":
    sys.exit(main())
