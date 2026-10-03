#!/usr/bin/env python3
"""Gera o manifesto de integridade das pastas de microservices.

POR QUE ISTO EXISTE

O pedido e "baixar os repositorios do jeito que estao". So 3 das 23 pastas
mantem o .git com remote e, dessas 3, sabemos o commit exato. Das outras 20 o
historico foi perdido, e nao ha commit nenhum registrado.

Isso significa que um `git clone` NAO reproduz o que esta em disco: ele traz o
HEAD de hoje, que pode ser uma versao diferente da que o ERP foi testado com.
Uma biblioteca de imposto que mudou de comportamento entre a data em que foi
copiada e a data em que foi restaurada e um bug que aparece como "a API antes
funcionava".

Entao o manifesto nao guarda o conteudo: guarda o SHA-256 de cada arquivo. Com
ele, a restauracao e verificavel. Se o que baixou bate com o hash, e o mesmo
material. Se nao bate, o script diz que NAO bate, em vez de o material errado
entrar em producao silenciosamente.

Nao guarda o conteudo porque sao 554 MB e o manifesto precisa caber no git.
Guarda 14 mil hashes, que cabem.

USO
    python3 gerar_manifesto.py [RAIZ_MICROSERVICES] [--saida MANIFESTO]
"""
import hashlib
import json
import os
import subprocess
import sys

RAIZ = os.path.abspath(
    sys.argv[1] if len(sys.argv) > 1 and not sys.argv[1].startswith("--")
    else "src/main/resources/microservices")

IGNORAR_DIR = {".git", "node_modules", "target", "build", ".idea",
               "__pycache__", ".venv", ".mvn", ".settings"}

# Pastas que nao tem de onde baixar: sao pacotes oficiais da SEFAZ ou material
# oficial da prefeitura. Nao existe repositorio git para elas.
SEM_REPO = {
    "PL_MDFe_300b_NT012025_1.05": {
        "motivo": "Pacote de Liberacao oficial da SEFAZ (schemas + MOC + NT). Nao e repositorio.",
        "origem": "https://portal.fazenda.sp.gov.br/servicos/mdfe",
        "alternativa": "https://dfe-portal.svrs.rs.gov.br/mdfe/Documentos",
    },
    "NFSe-SaoPaulo-SP": {
        "motivo": "Documentos oficiais da Prefeitura de Sao Paulo (manual, XSD, planilhas de codigo). Nao e repositorio.",
        "origem": "manual NFe_Web_Service-v3.3.8.pdf + esquema nota servico sp/*.xsd",
        "alternativa": "https://nfews.prefeitura.sp.gov.br/lotenfe.asmx",
    },
}

# Codigo do proprio Brasil SaaS: vive no git do ERP, nao tem repositorio externo.
CODIGO_NOSSO = {
    "nfse-sp-api": "commits e2070939, 746e1f15, 20179364",
    "nfse-sp-bridge": "commits 746e1f15, 20179364",
    "nfse-watchdog": "commits 746e1f15",
    "nfse-failover": "commits 746e1f15",
}

# Origem das 23, com a confianca. Recuperada de <scm> do pom, composer.json,
# package.json, BRASIL-SAAS.md, git remote, ou busca online.
# "fraca" = veio de badge de README e pode apontar para fork.
ORIGENS = {
    "BancosBrasileiros":         ("https://github.com/guibranco/BancosBrasileiros.git", "remoto"),
    "EchoAvatar":                 ("https://github.com/PantoMatrix/PantoMatrix.git", "fraca"),
    "Java-Efd-Contribuicoes":     ("https://github.com/Samuel-Oliveira/Java-Efd-Contribuicoes.git", "pom-scm"),
    "Java-Efd-Icms":              ("https://github.com/Samuel-Oliveira/Java-Efd-Icms.git", "pom-scm"),
    "Java_CTe":                   ("https://github.com/Samuel-Oliveira/Java_CTe.git", "pom-scm"),
    "Java_Certificado":           ("https://github.com/Samuel-Oliveira/Java_Certificado.git", "pom-scm"),
    "Java_MDFe":                  ("https://github.com/Samuel-Oliveira/Java_MDFe.git", "pom-m2"),
    "Java_NFe":                   ("https://github.com/Samuel-Oliveira/Java_NFe.git", "pom-scm"),
    "Java_Pdf_Signature":         ("https://github.com/Samuel-Oliveira/Java_Pdf_Signature.git", "pom-scm"),
    "boleto-cnab-api":            ("https://github.com/akretion/boleto_cnab_api.git", "bradilcloud-md"),
    "esocial":                    ("https://github.com/tst-labs/esocial.git", "readme"),
    "l10n-brazil":                ("https://github.com/OCA/l10n-brazil.git", "remoto"),
    "nfe":                        ("https://github.com/wmixvideo/nfe.git", "pom-scm"),
    "nfse":                       ("https://github.com/EduardoKuhn89/nfse.git", "pom-scm"),
    "nfse_prefeitura_sp":         ("https://github.com/infosimples/nfse_prefeitura_sp.git", "busca-online"),
    "sped-mdfe":                  ("https://github.com/nfephp-org/sped-mdfe.git", "remoto"),
    "spring-ai":                  ("https://github.com/spring-projects/spring-ai.git", "pom-scm"),
}


def sha256(caminho, bloco=1 << 20):
    h = hashlib.sha256()
    with open(caminho, "rb") as f:
        while True:
            pedaco = f.read(bloco)
            if not pedaco:
                break
            h.update(pedaco)
    return h.hexdigest()


def info_git(pasta):
    if not os.path.isdir(os.path.join(pasta, ".git")):
        return None, None
    def roda(*args):
        r = subprocess.run(["git", "-C", pasta] + list(args),
                           capture_output=True, text=True)
        return r.stdout.strip() or None
    return roda("rev-parse", "--short", "HEAD"), roda("rev-parse", "--abbrev-ref", "HEAD")


def arquivos(pasta):
    """Todos os arquivos relevantes, com hash. Ignora .git e build."""
    achados = {}
    for raiz, dirs, nomes in os.walk(pasta):
        dirs[:] = [d for d in dirs if d not in IGNORAR_DIR]
        for nome in nomes:
            caminho = os.path.join(raiz, nome)
            rel = os.path.relpath(caminho, pasta)
            try:
                achados[rel] = sha256(caminho)
            except OSError:
                continue
    return achados


def main():
    saida = None
    for i, a in enumerate(sys.argv):
        if a == "--saida" and i + 1 < len(sys.argv):
            saida = sys.argv[i + 1]
    if not saida:
        saida = os.path.join(RAIZ, "MANIFESTO-MICROSERVICES.json")

    manifesto = {
        "_gerado_por": "gerar_manifesto.py",
        "_aviso": (
            "Hashes de verificacao. Das 23 pastas, so 3 tem commit registrado; "
            "das outras 20 o clone traz o HEAD atual, que pode diferir do "
            "material testado. O hash diz se o que baixou e o mesmo."),
        "_pastas": {},
    }

    total_arquivos = 0
    for pasta in sorted(os.listdir(RAIZ)):
        caminho = os.path.join(RAIZ, pasta)
        if not os.path.isdir(caminho) or pasta in (".git",):
            continue

        commit, branch = info_git(caminho)
        arqs = arquivos(caminho)
        total_arquivos += len(arqs)

        if pasta in SEM_REPO:
            entrada = {
                "tipo": "oficial-sem-repo",
                "detalhe": SEM_REPO[pasta],
                "arquivos": len(arqs),
                "hashes": arqs,
            }
        elif pasta in CODIGO_NOSSO:
            entrada = {
                "tipo": "codigo-do-brasil_saas",
                "detalhe": {"commits_no_erp": CODIGO_NOSSO[pasta]},
                "arquivos": len(arqs),
                "hashes": arqs,
            }
        else:
            url, confianca = ORIGENS.get(pasta, (None, "desconhecida"))
            entrada = {
                "tipo": "repositorio-externo",
                "url": url,
                "confianca": confianca,
                "commit_conhecido": commit,
                "branch": branch,
                "arquivos": len(arqs),
                "hashes": arqs,
            }
        manifesto["_pastas"][pasta] = entrada

    manifesto["_total_pastas"] = len(manifesto["_pastas"])
    manifesto["_total_arquivos"] = total_arquivos

    os.makedirs(os.path.dirname(os.path.abspath(saida)), exist_ok=True)
    with open(saida, "w", encoding="utf-8") as f:
        json.dump(manifesto, f, indent=1, sort_keys=True, ensure_ascii=False)

    kb = os.path.getsize(saida) // 1024
    print("  manifesto: %s" % saida)
    print("  %d pastas, %d arquivos, %d KB"
          % (len(manifesto["_pastas"]), total_arquivos, kb))
    sem_commit = [p for p, e in manifesto["_pastas"].items()
                  if e["tipo"] == "repositorio-externo" and not e.get("commit_conhecido")]
    print("  sem commit conhecido: %d de %d repositorios externos"
          % (len(sem_commit), sum(1 for e in manifesto["_pastas"].values()
                                  if e["tipo"] == "repositorio-externo")))


if __name__ == "__main__":
    main()
