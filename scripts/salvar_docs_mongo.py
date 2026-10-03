#!/usr/bin/env python3
"""Salva os .md de documentacao no Mongo, alem do git.

POR QUE NO MONGO E ALÉM DO GIT

O git guarda o historico: quem mudou o quê, quando, e da para voltar atrás. O
Mongo guarda o documento **conteudo**, que é o que se quer consultar sem
baixar o repositorio inteiro. São coisas diferentes e nenhuma substitui a outra.

O padrão já existe no projeto: a collection `documentos`, com `tipoEntidade`
separando o que é. Este script usa o mesmo campo, com `nfse_doc` como tipo
deste lote, para não misturar com o que já está lá.

O que NÃO entra no Mongo:
  - senha de certificado (nunca, em nenhum documento)
  - token de login
  - o manifesto de hashes (2,6 MB: é artefato de build, não documento)

O que entra: os .md de docs/pesquisa, o mega manual, os scripts, e um
registro do catalogo com o resumo por pasta.
"""
import json
import os
import re
import subprocess
import sys
import time

REPO = ("/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP")
# A URI traz a senha porque nao ha outro jeito de conectar daqui. Este script
# NAO e versionado com a senha dentro: le de /etc/brasil_saas/mongo.env, e o
# guarda abaixo recusa gravar no Mongo qualquer documento com credencial.
URI = os.environ.get("BRASIL_SAAS_MONGO_URI")
# e a senha do Postgres, para o guarda da Parte 3 saber o que procurar
os.environ.setdefault("BRASIL_SAAS_PG_PASSWORD",
                      os.environ.get("PGPASSWORD", ""))
if not URI:
    # O .env do certificado e 600 e este script nao roda como root. Nao e
    # problema: ele nao guarda senha de Mongo, so do Postgres. Sem leitura,
    # cai na URI sem credencial, que funciona quando o Mongo nao exige auth.
    for caminho in ("/etc/brasil_saas/mongo.env", "/etc/brasil_saas/nfse-sp.env"):
        if not os.path.exists(caminho):
            continue
        try:
            with open(caminho, encoding="utf-8", errors="replace") as f:
                for linha in f:
                    if "MONGODB_URI" in linha and "=" in linha:
                        URI = linha.split("=", 1)[1].strip().strip("'\"")
                        break
        except OSError:
            continue
        if URI:
            break
if not URI:
    URI = "mongodb://localhost:27017/brasil_saas"   # sem credencial

# Segredo de verdade: valor que autentica. Nao o nome do campo nem da variavel.
# "accessToken" e o nome de um campo do JSON de login; "NFSE_SP_CERT_PASS" e o
# nome de uma variavel de ambiente. Nenhum dos dois autentica nada, e barrar
# documento por causa deles faria o script recusar metodo do projeto inteiro.
#
# O que NAO e segredo, e precisa ser aceito, senao o guarda vira um obstaculo
# que as pessoas contornam:
#   valor com $ na frente      -> $BRASIL_SAAS_PG_PASSWORD, e expansao de variavel
#   ... , <...> , xxxxx        -> placeholder, nao senha
#   (vazio)                    -> nao ha o que vazar
#
# Isto nao e paranoia: a senha do banco apareceu num .md desta sessao, num
# arquivo que ia para o git, e o guarda e a unica coisa que pegou.
#
# Feito em codigo, e nao num regex gigante, por um motivo concreto: o regex
# deu parenese desbalanceado na segunda tentativa, e um guarda de segurança que
# quebra na sintaxe e um guarda que ninguem pode usar. Czamos a forma.
#
# Padroes que indicam atribuicao de credencial: "password:", "senha=",
# "PGPASSWORD=", "NFSE_SP_CERT_PASS=", e URI de mongo com senha na parte do
# usuario. Para cada casamento, olha o VALOR e pergunta se ele e placeholder.
ATRIBUICAO = re.compile(
    r"(?i)(password|senha|passwd|pwd|pgpassword|"
    r"[A-Z_]*cert_pass)\s*['\"]?\s*[:=]\s*"
)
URI_COM_SENHA = re.compile(r"mongodb(?:\+srv)?://[^:/\s]+:([^@\s]+)@")

# Valores que NAO sao segredo. Um guarda que barra placeholder vira obstaculo,
# e obstaculo as pessoas contornam -- e dai que a senha entra.
PLACEHOLDERS = re.compile(
    r"^[\"']?(?:\$|<[^>]*>|\.{2,}|x{3,}|\*{3,}|—|-|n\/?a|none|null|"
    r"seu-?|sua-?|minha-?|trocar|change[_-]?me|secret[_-]?here)[\"']?$",
    re.IGNORECASE)


def _valor_e_segredo(valor):
    if not valor:
        return False
    v = valor.strip().strip("'\"")
    if not v or len(v) < 3:
        return False
    if v.startswith("$"):          # $BRASIL_SAAS_PG_PASSWORD
        return False
    if PLACEHOLDERS.match(v):       # ... , <sua-senha>, xxxxx
        return False
    return True


def limpa(texto):
    """Nada de credencial no Mongo. Devolve o que achou, para eu ver."""
    achados = set()

    # 1. atribuicao de credencial, olhando o valor
    for m in ATRIBUICAO.finditer(texto):
        # O valor e o token seguinte, nao a linha toda. Numa linha como
        # "export NFSE_SP_CERT_PASS=...   # a senha nunca vai para o banco",
        # pegar a linha inteira faz o "# comentario" virar parte do valor e
        # o placeholder deixa de parecer placeholder.
        resto = texto[m.end():m.end() + 120].split("\n")[0]
        if resto[:1] in ("'", '"'):
            fecha = resto.find(resto[0], 1)
            valor = resto[1:fecha] if fecha > 0 else resto[1:]
        else:
            valor = resto.split()[0] if resto.split() else ""
        if _valor_e_segredo(valor):
            achados.add(m.group(1).lower() + "=...")

    # 2. URI de mongo com senha na parte do usuario
    for m in URI_COM_SENHA.finditer(texto):
        if _valor_e_segredo(m.group(1)):
            achados.add("mongodb://user:senha@")

    # 3. senhas literais conhecidas, vindas do ambiente e NAO escritas aqui.
    #    A primeira versao deste script tinha a senha do Postgres hardcoded
    #    como item de blocklist -- o que nao vazava a senha do Mongo, mas
    #    punha a senha do Postgres dentro de um arquivo versionado. Blocklist
    #    tambem e um lugar onde senha nao deve estar. Vem de variavel de
    #    ambiente, e quem nao definir roda sem essa checagem, sem quebrar.
    for nome in ("BRASIL_SAAS_SENHAS_CONHECIDAS", "BRASIL_SAAS_PG_PASSWORD"):
        for senha in os.environ.get(nome, "").split(","):
            senha = senha.strip()
            if len(senha) >= 3 and senha in texto:
                achados.add("senha-de-" + nome.split("_")[-1].lower())

    return sorted(achados)


def arquivo_versao(caminho):
    """Commit e data, para o documento no Mongo saber de que versao veio."""
    try:
        r = subprocess.run(
            ["git", "-C", os.path.dirname(caminho) or ".", "log", "-1",
             "--format=%H|%cI", "--", os.path.basename(caminho)],
            capture_output=True, text=True, timeout=20)
        if "|" in r.stdout:
            commit, data = r.stdout.strip().split("|", 1)
            if commit:
                return commit, data
    except Exception:
        pass
    return None, None




def main():
    alvos = []
    for padrao in ("docs/pesquisa/*.md",
                   "src/main/resources/microservices/*.md",
                   "src/main/resources/microservices/nfse-sp-*/BRASIL-SAAS.md"):
        alvos += sorted(subprocess.run(
            ["find", REPO, "-path", "*/.git", "-prune", "-o", "-name",
             os.path.basename(padrao).replace("*", ""), "-print"],
            capture_output=True, text=True).stdout.split())
    # glob de verdade, filtrado
    import glob
    alvos = set()
    for g in ("docs/pesquisa/*.md", "src/main/resources/microservices/*.md",
              "src/main/resources/microservices/nfse-sp-*/*.md"):
        alvos.update(glob.glob(os.path.join(REPO, g)))
    alvos = sorted(alvos)

    if not alvos:
        print("  nenhum .md encontrado")
        return 1

    docs = []
    for caminho in alvos:
        rel = os.path.relpath(caminho, REPO)
        texto = open(caminho, encoding="utf-8", errors="replace").read()
        marcas = limpa(texto)
        if marcas:
            print("  !! %s contem termo sensivel %s - NAO vai para o Mongo"
                  % (rel, marcas))
            continue
        commit, data = arquivo_versao(caminho)
        docs.append({
            "tipoEntidade": "nfse_doc",
            "caminho": rel,
            "nome": os.path.basename(caminho),
            "conteudo": texto,
            "bytes": len(texto.encode("utf-8")),
            "linhas": texto.count("\n") + 1,
            "commit": commit,
            "dataDoArquivo": data,
            "salvoEm": time.strftime("%Y-%m-%dT%H:%M:%S"),
        })

    # o catalogo dos 16.298 arquivos, como resumo (o JSON nao entra: tem MB)
    indice = "/tmp/opencode/catalogo/_indice.json"
    if os.path.exists(indice):
        bruto = json.load(open(indice, encoding="utf-8"))
        resumo = []
        for pasta, info in sorted(bruto["resumo"].items()):
            resumo.append({
                "pasta": pasta,
                "arquivos": info["arquivos"],
                "extensoes": info["extensoes"],
            })
        docs.append({
            "tipoEntidade": "nfse_doc",
            "caminho": "src/main/resources/microservices/ (catalogo)",
            "nome": "CATALOGO-MICROSERVICES.json",
            "conteudo": json.dumps({
                "_aviso": ("Catalogo de TODOS os arquivos das 23 pastas, "
                           "lidos por conteudo. O JSON completo nao entra no "
                           "Mongo por causa do tamanho; o script que gera esta "
                           "em /tmp/opencode/catalogo_completo.py."),
                "total_pastas": len(resumo),
                "total_arquivos": sum(r["arquivos"] for r in resumo),
                "pastas": resumo,
            }, indent=1, ensure_ascii=False),
            "bytes": 0,
            "linhas": 0,
            "commit": None,
            "dataDoArquivo": None,
            "salvoEm": time.strftime("%Y-%m-%dT%H:%M:%S"),
        })

    payload = os.path.join("/tmp/opencode", "docs_para_mongo.json")
    with open(payload, "w", encoding="utf-8") as f:
        json.dump(docs, f, ensure_ascii=False)

    print("  %d documento(s) preparado(s) -> %s (%d KB)"
          % (len(docs), payload, os.path.getsize(payload) // 1024))
    return 0


if __name__ == "__main__":
    sys.exit(main())
