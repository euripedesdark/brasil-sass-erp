#!/usr/bin/env python3
"""
Gera um seed de desenvolvimento para o Brasil SaaS ERP.

POR QUE ISTO EXISTE, E O QUE ELE SUBSTITUI

Duas versoes de seed em SQL foram geradas e as duas nao carregavam. Os motivos
sao sempre os mesmos, e nenhum deles e culpa do banco:

  1. ORDEM DE CARGA. As tabelas vinham em ordem alfabetica, entao uma tabela
     era carregada antes do seu pai. O valor gravado e uma subquery
     -- (SELECT MIN(id) FROM bc_core_empresa) -- que devolve NULL quando o pai
     ainda esta vazio.

  2. NOT NULL NAO E TRIGGER. O gerador anterior desligava os triggers de FK
     durante a carga ("DISABLE TRIGGER USER") e achava que resolvia. Nao
     resolve: NOT NULL e validado pelo motor, nao por trigger. Resultado em
     bc_bi_dashboard, a primeira tabela:
        null value in column "empresa_id" ... violates not-null constraint

  3. CHECK DE ENUM. Quatro tabelas tem CHECK do tipo
        status = ANY (ARRAY['EMITIDO','PAGO',...])
     e o seed gravava 'TESTE-bc_fin_boleto-status-7'. Nao passa.

  4. TABELA DE REFERENCIA POLUIDA. bc_fis_issqn tem 1.759.790 linhas reais e o
     seed ia acrescentar 61 registros falsos. Trocar dado de tabela de
     referencia por dado sintetico e pior que nao ter dado.

Este script resolve os quatro, e o primeiro por construcao: ele le as FKs do
banco, monta o grafo e sobe na ordem topologica. Pai antes de filho, sempre.

O QUE ELE FAZ

  python3 scripts/gerar_seed_desenvolvimento.py --total 10000
  python3 scripts/gerar_seed_desenvolvimento.py --total 10000 --dry-run
  python3 scripts/gerar_seed_desenvolvimento.py --total 10000 --saida x.sql

O QUE ELE NAO FAZ, DE PROPOSITO

  * Nao toca em tabela de referencia. Permissao, modulo, perfil, NCM, CFOP,
    CEST, ISSQN, municipio, servico LC116, CNAE e historico do Flyway ficam de
    fora. Tabela de referencia com dado falso e pior que tabela vazia, porque
    o ERP passa a responder com dado errado em vez de nao responder.

  * Nao emite nota fiscal. Nada de NFe, NFS-e, MDF-e ou CT-e e criado. O
    emissor de NFS-e e o unico caminho de nota do ERP e ele nao e chamado.

  * Nao inventa valor que precisa ser verdade. Coluna que e data de
    vencimento, CNPJ ou chave de acesso recebe um valor coerente com o tipo,
    nao um valor que "passe".

DETERMINISMO

A mesma semente gera o mesmo arquivo. Isso importa porque um seed que muda a
cada execucao transforma "funciona" em "funcionou uma vez".

  --semente 42   (padrao 42)
"""
import argparse
import random
import re
import subprocess
import sys
from collections import defaultdict

# --------------------------------------------------------------------------
# Tabelas que NUNCA sao semeadas, em nenhuma hipotese. O motivo de cada uma
# esta junto, porque "pular tabela" sem motivo e o jeito de um dia pular a
# tabela errada.
# --------------------------------------------------------------------------
NUNCA_SEMEAR = {
    'flyway_schema_history':  'historico de migration; seed aqui faz o Flyway achar que rodou',
    'bc_core_permissao':      'ACL real; dado falso muda a autorizacao do sistema',
    'bc_core_modulo':         'modulos do menu; dado falso muda o que aparece',
    'bc_core_perfil':         'perfis de acesso; idem',
    'bc_core_perfil_permissao': 'vinculo perfil-permissao; idem',
    'bc_core_usuario':        'usuarios reais, com login e senha; idem',
    'bc_core_usuario_perfil': 'vinculo usuario-perfil; idem',
    'bc_core_usuario_modulo': 'vinculo usuario-modulo; e ACL. Semear com id de '
                              'modulo inventado estoura a FK, e com id real '
                              'troca o que o usuario enxerga',
    'bc_core_empresa_vinculo': 'relacao entre empresas; em banco novo nao ha '
                               'empresa para vincular',
    'bc_fis_nfe':             'nota fiscal — emissao e por outro caminho',
    'bc_fis_nfe_item':        'nota fiscal',
    'bc_fis_nfe_evento':      'nota fiscal',
    'bc_fis_manifestacao':    'nota fiscal',
    'bc_fis_nfse':            'nota fiscal',
    'bc_fis_nfse_item':       'nota fiscal',
    'bc_fis_nfse_retorno':    'nota fiscal',
    'bc_fis_mdfe':            'documento fiscal',
    'bc_fis_cte':             'documento fiscal',
    'bc_fis_sped':            'documento fiscal',
}

# --------------------------------------------------------------------------
# Tabelas de REFERENCIA: semeadas so quando estao vazias.
#
# A distincao com NUNCA_SEMEAR e o que faz o seed funcionar em banco novo.
# Estas tabelas vem das migrations com dado real (NCM 10.515, CFOP 619, CEST
# 1.043, ISSQN 1.759.790, municipio 5.343) e nao devem ser sobrepostas. Mas
# em um banco recem-criado a partir do dump de esquema elas estao VAZIAS, e
# qualquer tabela que tenha FK para elas quebra:
#
#   ERROR: bc_cad_base_cep violates foreign key constraint
#          "bc_cad_base_cep_municipio_id"
#
# porque nao existe municipio para apontar. Entao: se tem dado, nao mexe; se
# esta vazia, semeia o minimo para as FK resolverem.
# --------------------------------------------------------------------------
REFERENCIA = {
    'bc_fis_ncm':           'classificacao de mercadoria',
    'bc_fis_cfop':          'operacoes fiscais',
    'bc_fis_cest':          'substituicao tributaria',
    'bc_fis_issqn':         'codigos de servico municipal',
    'bc_fis_nbs':           'nomenclatura brasileira de servicos',
    'bc_fis_servico_lc116': 'servicos da LC 116',
    'bc_fis_cnae_servico':  'CNAE de servico',
    'bc_cad_municipio':     'municipios do IBGE',
    'bc_cad_base_cep':      'base de CEP',
    'bc_core_banco':        'bancos do Brasil',
}


def psql(sql, banco, timeout=120):
    r = subprocess.run(
        ['psql', '-h', 'localhost', '-U', 'postgres', '-d', banco,
         '-tA', '-F', '\x1f', '-v', 'ON_ERROR_STOP=1', '-c', sql],
        capture_output=True, text=True,
        env={'PGPASSWORD': 'ALTERE_ME', 'PATH': '/usr/bin:/bin'},
        timeout=timeout)
    if r.returncode != 0:
        raise RuntimeError('psql falhou: %s' % r.stderr.strip()[:400])
    return r.stdout


# --------------------------------------------------------------------------
# 1. Introspeccao
# --------------------------------------------------------------------------

def introspecionar(banco):
    """Le do banco tudo que o gerador precisa: colunas, FKs, unicidade, CHECK."""
    colunas = defaultdict(list)
    # O ultimo campo usa sentinela, e nao string vazia, porque o psql em modo
    # unaligned NAO imprime o separador final de um campo vazio: a linha sai
    # com 7 campos em vez de 8 e o split estoura em ValueError. Ocorreu em
    # flyway_schema_history.success, que tem column_default nulo.
    sql = """
    select table_name, column_name, data_type, is_nullable,
           coalesce(character_maximum_length, -1),
           coalesce(numeric_precision, -1), coalesce(numeric_scale, -1),
           coalesce(column_default, '<sem-default>')
      from information_schema.columns
     where table_schema = 'brasil_saas'
     order by table_name, ordinal_position
    """
    for l in psql(sql, banco).strip().split('\n'):
        if not l.strip():
            continue
        partes = l.split('\x1f')
        if len(partes) != 8:
            continue          # linha degenerada: melhor pular que inventar
        t, c, dt, nul, tam, prec, esc, default = partes
        colunas[t].append({
            'nome': c, 'tipo': dt, 'nulo': nul == 'YES',
            'tamanho': int(tam), 'precisao': int(prec), 'escala': int(esc),
            'default': default,
        })

    # FK: coluna -> tabela referenciada.
    #
    # A versao anterior tinha um `join pg_constraint fg on fg.conrelid =
    # con.conrelid and fg.conkey[1] = con.conkey[1]`, que eu nao consigo
    # justificar: ele elimina linhas em silencio. O resultado foi 326 FKs em vez
    # de 354 — e, pior, as que sumiram eram exatamente as que importam.
    # bc_prod_estrutura.produto_pai_id e produto_filho_id NAO apareciam, entao o
    # gerador sortava dois inteiros qualquer como se fossem produto, e
    # ck_prod_estrutura_distinto (pai <> filho) pegava a coincidencia.
    #
    # A contagem e conferida contra pg_constraint no fim da funcao, e o numero
    # fora do esperado e aviso — nao apenas um valor silenciosamente errado.
    fks = defaultdict(dict)
    sql = """
    select rc.relname, kcu.column_name, pc.relname
      from pg_constraint con
      join pg_class rc on rc.oid = con.conrelid
      join pg_class pc on pc.oid = con.confrelid
      join pg_namespace n on n.oid = rc.relnamespace
      join information_schema.key_column_usage kcu
        on kcu.constraint_name = con.conname
       and kcu.constraint_schema = 'brasil_saas'
     where con.contype = 'f'
       and n.nspname = 'brasil_saas'
    """
    for l in psql(sql, banco).strip().split('\n'):
        if not l.strip():
            continue
        filho, col, pai = l.split('\x1f')
        fks[filho][col] = pai
    # FOREIGN KEY de coluna unica, que o information_schema em Sometimes nao traz
    sql = """
    select rc.relname, a_child.attname, pc.relname
      from pg_constraint con
      join pg_class rc on rc.oid = con.conrelid
      join pg_class pc on pc.oid = con.confrelid
      join pg_namespace n on n.oid = rc.relnamespace
      join lateral unnest(con.conkey) with ordinality as k(attnum, ord)
        on true
      join pg_attribute a_child
        on a_child.attrelid = rc.oid and a_child.attnum = k.attnum
     where con.contype = 'f'
       and n.nspname = 'brasil_saas'
       and array_length(con.conkey, 1) = 1
    """
    for l in psql(sql, banco).strip().split('\n'):
        if not l.strip():
            continue
        filho, col, pai = l.split('\x1f')
        fks[filho].setdefault(col, pai)

    # Colunas que precisam variar dentro da tabela, por causa de UNIQUE.
    #
    # Tres situacoes, e as tres ja custaram uma iteracao cada:
    #
    #   UNIQUE de 1 coluna   (uuid, sigla)  -> a coluna nao pode repetir
    #
    #   UNIQUE composta      (empresa_id, pessoa_id) -> a COMBINACAO nao pode
    #     repetir. Como empresa_id e constante no seed, o efeito pratico e que
    #     pessoa_id tem que variar. Tratar empresa_id como "unica" — o que a
    #     primeira versao fazia, olhando coluna por coluna — faz o gerador criar
    #     169 empresas para 56 produtos.
    #
    #   UNIQUE onde sobra mais de uma coluna variavel  -> nao se sabe o que
    #     precisa variar, e nao se tenta adivinhar. O dado pode colidir; se
    #     colidir, o erro diz o indice e o gerador pode ser corrigido.
    #
    # Constraint e indice sao lidos juntos e o resultado e montado do zero: a
    # primeira versao somava as colunas de um lado e so desviava do outro, e
    #Sobravam colunas que nunca deveriam estar na lista.
    #
    # E o agrupamento e POR INDICE, nao por tabela. Agrupar por tabela junta
    # todos os indices unique de uma tabela numa linha so —
    # "id,codigo_tributacao_municipal,empresa_id" com count 3 — e a regra da
    # composta passa a nunca casar. Foi assim que codigo_tributacao_municipal
    # ficou de fora e o seed morreu em ux_cad_servico_codigo_municipal.
    CONSTANTES = {'empresa_id'}
    grupos = []   # (tabela, [colunas])

    sql = """
    select tc.table_name,
           string_agg(kcu.column_name, ',' order by kcu.ordinal_position)
      from information_schema.table_constraints tc
      join information_schema.key_column_usage kcu
        on kcu.constraint_name = tc.constraint_name
       and kcu.table_schema = tc.table_schema
     where tc.table_schema = 'brasil_saas'
       and tc.constraint_type = 'UNIQUE'
     group by tc.constraint_name, tc.table_name
    """
    for l in psql(sql, banco).strip().split('\n'):
        if l.strip():
            t, cols = l.split('\x1f')
            grupos.append((t, cols.split(',')))

    # Indice UNIQUE que NAO e constraint: um "CREATE UNIQUE INDEX" nao aparece
    # em information_schema.table_constraints.
    #
    # A segunda consulta pega o que a primeira perde: coluna de EXPRESSAO. Um
    # indice como
    #     CREATE UNIQUE INDEX uk_bc_fin_caixa_empresa_nome
    #       ON bc_fin_caixa (empresa_id, lower((nome)::text))
    # tem indkey = "3 0": o 0 e a posicao da expressao, que nao corresponde a
    # nenhuma linha de pg_attribute. O join por attnum descarta a coluna e o
    # gerador passa a achar que o indice e so por empresa_id — que e
    # exatamente o que aconteceu, e a carga morreu em "duplicate key value
    # violates unique constraint uk_bc_fin_caixa_empresa_nome".
    #
    # Aqui as colunas sao lidas do texto do CREATE INDEX, cruzadas com as
    # colunas que a tabela realmente tem.from de expression como
    # "lower(nome)" vira "nome", que e o que importa para a regra.
    sql = """
    select r.relname, ix.relname, pg_get_indexdef(i.indexrelid)
      from pg_index i
      join pg_class r on r.oid = i.indrelid
      join pg_class ix on ix.oid = i.indexrelid
      join pg_namespace n on n.oid = r.relnamespace
     where i.indisunique
       and n.nspname = 'brasil_saas'
       and not exists (select 1 from pg_constraint c
                        where c.conindid = i.indexrelid and c.contype = 'u')
     group by r.relname, ix.relname, i.indexrelid
    """
    for l in psql(sql, banco).strip().split('\n'):
        if not l.strip():
            continue
        t, _ix, definicao = l.split('\x1f')
        if '(' not in definicao:
            continue
        dentro = definicao[definicao.index('(') + 1:]
        # fecha a parenthesis do btree, ignorando as internas
        nivel, corte = 1, len(dentro)
        for i, ch in enumerate(dentro):
            if ch == '(':
                nivel += 1
            elif ch == ')':
                nivel -= 1
                if nivel == 0:
                    corte = i
                    break
        dentro = dentro[:corte]
        achados = [x for x in colunas.get(t, [])
                   if re.search(r'\b%s\b' % re.escape(x['nome']), dentro)]
        if achados:
            grupos.append((t, [x['nome'] for x in achados]))

    unicos = defaultdict(set)
    compostas = defaultdict(list)
    for t, lista in grupos:
        if len(lista) == 1:
            unicos[t].add(lista[0])
            continue
        variaveis = [c for c in lista if c not in CONSTANTES]
        if len(variaveis) == 1:
            unicos[t].add(variaveis[0])
        elif len(variaveis) > 1:
            # Mais de uma coluna variavel: a COMBINACAO tem que ser unica.
            # Exemplo real: uk_usuario_modulo e (usuario_id, modulo_id), e os
            # dois sao FK que variam. Deixar de fora e o que fez o seed morrer
            # nele. A geracao trata essas colunas com divisao de indice, o que
            # garante que o par nao se repete ate o limite do produto.
            compostas[t].append(lista)

    # CHECK: guarda a definicao para casar padroes
    checks = defaultdict(list)
    sql = """
    select c.conrelid::regclass::text, c.conname, pg_get_constraintdef(c.oid)
      from pg_constraint c
      join pg_class r on r.oid = c.conrelid
      join pg_namespace n on n.oid = r.relnamespace
     where c.contype = 'c' and n.nspname = 'brasil_saas'
    """
    for l in psql(sql, banco).strip().split('\n'):
        if not l.strip():
            continue
        t, nome, definicao = l.split('\x1f')
        checks[t.split('.')[-1]].append((nome, definicao))

    existentes = {}
    for t in colunas:
        try:
            existentes[t] = int(psql(
                'select count(*) from brasil_saas.%s' % t, banco, 60).strip() or 0)
        except Exception:
            existentes[t] = -1

    return colunas, fks, unicos, compostas, checks, existentes


# --------------------------------------------------------------------------
# 2. Planos: o que fazer com cada CHECK
# --------------------------------------------------------------------------

# As definicoes do Postgres sao cheias de redundancia: "((" a mais, "::text"
# no meio, "(0)::numeric" num CHECK de inteiro. Cada regex abaixo tolera essa
# redundancia. As primeiras versoes eram literais e casavam em 2 dos 18 CHECKs
# do banco — o suficiente para o gerador parecer funcionar e nao funcionar.
#
#   enum        status::text = ANY (ARRAY['A','B']::text[])
#   nao_igual   (a <> b)
#   positivo    (x > (0)::numeric)   ou   (nivel > 0)
#   nao_neg     (x >= (0)::numeric)
#   intervalo   (x >= 0 AND x <= 100)
# O cast do Postgres no meio do nome e o que quebra o casamento:
#   ((status)::text = ANY ((ARRAY['A'::character varying, ...])::text[]))
# A coluna e "status", mas o padrao que aceitava "::text" sem parenteses
# pegava "text" — e a regra era registrada na coluna "text", que nao existe.
# Resultado: status recebia 'SEED STATUS 7' e morria em ck_fin_remessa_status.
# O grupo de permissao abaixo aceita o ")" e o "::" em qualquer ordem.
RE_ENUM = re.compile(
    r'([a-z_][a-z0-9_]*)\s*(?:\)|::\s*[a-z]+\s*\)*\s*)+=\s*ANY\s*\(\s*\(?\s*ARRAY\s*\[(.*?)\]\s*\)',
    re.S)
RE_NOT_SELF = re.compile(r'(\w+)\s*<>\s*(\w+)')
RE_POSITIVO = re.compile(r'(\w+)\s*>\s*0\b')
RE_NAO_NEGATIVO = re.compile(r'(\w+)\s*>=\s*0\b')
def normalizar_check(defi):
    r"""Tira o ruido de SQL de uma definicao de CHECK e deixa so a comparacao.

    O Postgres escreve assim:
        CHECK (((perda_percentual >= (0)::numeric) AND (perda_percentual < (100)::numeric)))
    Parentese aninhado, cast depois do parentese fechado, e parentese em volta
    de numero. Cada um disso ja quebrou uma regex: empilhar `\(?` nao resolve
    tres levels, e o `::numeric` vem DEPOIS do ")", nao antes.

    Depois de normalizar fica:
        CHECK (perda_percentual >= 0 AND perda_percentual < 100)
    que casa com regex simples. Preserva o ARRAY[...] do CHECK de enum, que e a
    unica parte com conteudo que importa.
    """
    d = re.sub(r'::\s*[a-z]+(\[\])?', ' ', defi)      # tira cast
    d = re.sub(r'\(\s*(-?[\d.]+)\s*\)', r' \1 ', d)  # tira parenhes de numero
    d = re.sub(r'\(\s*([\w.]+)\s*\)', r' \1 ', d)   # e de identificador simples
    d = re.sub(r'\(\s*\(+', ' (', d)                # achata (( -> (
    d = re.sub(r'\s+', ' ', d)
    return d


# O limite superior pode ser "<" ou "<=". Depois da normalizacao sobra um ")"
# orfao entre o numero e o AND, porque o "(0)" virou " 0 " e o parentese que o
# fechava continua la. Entao a regex tolera ")" e "(" opcionais.
RE_INTERVALO = re.compile(
    r'(\w+)\s*>=\s*(-?[\d.]+)\s*\)?\s*AND\s*\(?\s*(\w+)\s*<=\s*(-?[\d.]+)')
RE_INTERVALO_ABERTO = re.compile(
    r'(\w+)\s*>=\s*(-?[\d.]+)\s*\)?\s*AND\s*\(?\s*(\w+)\s*<\s*(-?[\d.]+)')


def classificar_checks(checks_da_tabela):
    """Traduz os CHECK de uma tabela em regras que o gerador sabe aplicar.

    Reconhecidos:
      enum        col = ANY(ARRAY['A','B'])   -> escolhe um literal do array
      nao_igual   a <> b                      -> desloca b em +1
      positivo    x > 0                       -> valor em 1..999.99
      nao_neg     x >= 0                      -> valor em 0..
      intervalo   lo <= x <= hi               -> valor dentro da faixa
    """
    regras = {'enum': {}, 'nao_igual': [], 'positivo': set(),
              'nao_negativo': set(), 'intervalo': {}, 'desconhecido': []}
    for nome, defi in checks_da_tabela:
        # O enum precisa da definicao crua: normalizar destruiria o ARRAY.
        m = RE_ENUM.search(defi)
        if m:
            valores = re.findall(r"'([^']*)'", m.group(2))
            if valores:
                regras['enum'][m.group(1)] = valores
                continue

        norm = normalizar_check(defi)

        # A ORDEM IMPORTA. O intervalo e testado antes do "> 0" e do ">= 0"
        # porque um intervalo tambem tem limite inferior: "perda_percentual >=
        # 0 AND perda_percentual < 100" casa com RE_NAO_NEGATIVO, que fazia
        # continue e deixava a coluna sem teto — o gerador tirava 100.0 de um
        # uniform() e o CHECK recusava. Regra mais especifica antes da geral.
        m = RE_INTERVALO.search(norm) or RE_INTERVALO_ABERTO.search(norm)
        if m:
            # exclusive: o teto entra 0.001 abaixo, para nunca cair no valor
            # que o CHECK recusa.
            regras['intervalo'][m.group(1)] = (float(m.group(2)),
                                              float(m.group(4)) - 0.001)
            continue

        m = RE_NOT_SELF.search(norm)
        if m:
            regras['nao_igual'].append((m.group(1), m.group(2)))
            continue

        m = RE_POSITIVO.search(norm)
        if m:
            regras['positivo'].add(m.group(1))
            continue

        m = RE_NAO_NEGATIVO.search(norm)
        if m:
            regras['nao_negativo'].add(m.group(1))
            continue

        regras['desconhecido'].append((nome, defi))
    return regras


# --------------------------------------------------------------------------
# 3. Geracao de valor por coluna
# --------------------------------------------------------------------------

class Gerador:
    def __init__(self, rnd, fks, unicos, regras_por_tabela, ids_por_tabela,
                 max_existente, empresa_id, semente):
        self.rnd = rnd
        self.fks = fks
        self.unicos = unicos
        self.regras = regras_por_tabela
        self.ids = ids_por_tabela
        self.max_existente = max_existente
        self.empresa_id = empresa_id
        self.semente = semente
        # contador por (tabela, coluna) para as FK que tambem sao UNIQUE
        self._consumidos = {}

    # -- tipos ---------------------------------------------------------- #
    def texto(self, tabela, coluna, n, tamanho):
        # 'coluna' chega como dict de coluna, nao como string. Passar a string
        # e o natural, e foi o que aconteceu na primeira versao.
        nome = coluna['nome'] if isinstance(coluna, dict) else str(coluna)
        base = 'SEED %s %s' % (nome.upper()[:14], n)
        if len(base) > tamanho > 0:
            base = base[:tamanho]
        return "'" + base.replace("'", "") + "'"

    @staticmethod
    def cabe(valor, col):
        """Ultima rede: nenhum texto passa do limite da coluna.

        Cada ramo de `valor()` ja respeita o tamanho, e mesmo assim o seed
        passou um ciclo inteiro morrendo em "value too long for type character
        varying(11)" por causa do cpf, que tem caminho proprio. Com isto, um
        caminho novo que esqueça do tamanho nao vira um erro de carga: vira um
        texto cortado, que e o que o usuario esperaria.
        """
        if not valor.startswith("'") or not valor.endswith("'"):
            return valor
        limite = col.get('tamanho', -1)
        if limite <= 0:
            return valor
        dentro = valor[1:-1]
        if len(dentro) > limite:
            return "'" + dentro[:limite] + "'"
        return valor

    def valor(self, tabela, col, n, nome_id=None):
        c = col['nome']
        t = col['tipo']
        regras = self.regras.get(tabela, {})
        fk_pai = self.fks.get(tabela, {}).get(c)

        # --- FK: tem de apontar para linha que existe
        if fk_pai:
            candidatos = self.ids.get(fk_pai)
            if not candidatos:
                # O pai nao foi semeado. Inventar um id (1..5) parece funcionar
                # e so quebra depois: a FK e checada na hora se o trigger estiver
                # ligado, e nao esta. Referencia quebrada e pior que referencia
                # nula, porque o ERP le e nao avisa.
                return 'NULL' if col['nulo'] else '1'
            # FK que tambem e UNIQUE (cliente.pessoa_id, por exemplo) nao pode
            # repetir o valor: duas linhas apontando para a mesma pessoa
            # batem em uk_cliente_pessoa na segunda. Aqui o pai e consumido em
            # ordem, um por linha, em vez de sorteado.
            if c in self.unicos.get(tabela, ()):
                usados = self._consumidos.setdefault((tabela, c), 0)
                if usados < len(candidatos):
                    self._consumidos[(tabela, c)] = usados + 1
                    return str(candidatos[usados])
                # esgotou a lista de pais: repete o ultimo, que a constraint
                # de unicidade vai recusar. Deixa o erro aparecer.
                # Acabaram os pais e a coluna e NOT NULL: nao ha saida. Deixa
                # o erro aparecer, que e mais honesto que escolher um id repetido
                # e deixar o banco recusar duas linhas depois.
                return 'NULL' if col['nulo'] else str(candidatos[usados % len(candidatos)])
            return str(self.rnd.choice(candidatos))

        # --- coluna de empresa
        if c == 'empresa_id':
            return str(self.empresa_id)

        # --- regra de CHECK tem prioridade sobre o tipo
        if c in regras.get('enum', {}):
            return "'" + self.rnd.choice(regras['enum'][c]) + "'"
        if c in regras.get('intervalo', {}):
            lo, hi = regras['intervalo'][c]
            return str(round(self.rnd.uniform(lo, hi), 2))
        if c in regras.get('positivo', set()):
            t = 'numeric' if 'numeric' in t or 'decimal' in t else t
            if 'numeric' in t or 'decimal' in t:
                return str(round(self.rnd.uniform(1, 999.99), 2))
            return str(self.rnd.randint(1, 1000))
        if c in regras.get('nao_negativo', set()):
            if 'numeric' in t or 'decimal' in t:
                return str(round(self.rnd.uniform(0, 999.99), 2))
            return str(self.rnd.randint(0, 1000))

        # --- booleanos
        if t == 'boolean':
            return 'true' if n % 2 else 'false'

        # --- data e hora
        if t == 'date':
            return "'%s'" % self.rnd.choice(
                ['2026-01-15', '2026-03-20', '2026-06-10', '2026-09-25'])
        if t in ('timestamp without time zone', 'timestamp with time zone'):
            return "'%s 10:%02d:00'" % (self.rnd.choice(
                ['2026-01-15', '2026-05-20', '2026-09-25']), self.rnd.randint(0, 23))
        if t == 'time without time zone':
            return "'%02d:%02d:00'" % (self.rnd.randint(8, 18), self.rnd.randint(0, 59))

        # --- json
        if t in ('json', 'jsonb'):
            return "'{}'" if col['nulo'] else "'{\"seed\": %d}'" % n

        # --- numeros
        if t in ('smallint', 'integer', 'bigint'):
            if c in ('id',):
                return '0'   # preenchido fora
            if 'percentual' in c or 'percent' in c:
                return str(self.rnd.randint(1, 99))
            if 'quantidade' in c or 'qtd' in c:
                return str(self.rnd.randint(1, 50))
            if c in ('nivel', 'numero_item'):
                return str(self.rnd.randint(1, 5))
            if 'valor' in c or 'preco' in c or 'saldo' in c or 'total' in c:
                return str(self.rnd.randint(1000, 99999))   # centavos
            return str(self.rnd.randint(1, 1000))
        if t in ('numeric', 'decimal'):
            # Respeita numeric(precision, scale). A primeira versao gerava
            # uniform(1, 9999.99) com 2 casas para qualquer coluna, e uma
            # numeric(5,2) estoura: 9999.99 sao 6 digitos. O erro aparecia
            # como "numeric field overflow", que nao diz qual coluna.
            esc = max(col['escala'], 0)
            prec = col['precisao']
            if prec > 0:
                inteiros = max(prec - esc, 1)
                teto = 10 ** inteiros - 1
            else:
                teto = 9999
            teto = min(teto, 999999)
            base = min(teto, 9999.99 if esc == 2 else teto)
            v = round(self.rnd.uniform(0.01, base), esc)
            return str(v)
        if t in ('real', 'double precision'):
            return str(round(self.rnd.uniform(0.01, 9999.99), 4))

        # --- texto
        if t in ('text', 'character varying', 'character', 'citext'):
            tam = col['tamanho'] if col['tamanho'] > 0 else 60
            if c in self.unicos.get(tabela, ()):
                # O numero da linha tem que SOBREVIVER ao corte. A primeira
                # versao fazia 'SEED-SIGLA-1' e truncava em 10 caracteres: as
                # 56 linhas viravam todas 'SEED-SIGL' e a segunda batia em
                # uk_unidade_medida. Entao o prefixo encolhe e o numero nao.
                # O numero e o ID da linha, e nao a posicao dela na carga.
                # Com a posicao, uma segunda execucao comeca em 1 de novo e
                # colide com o que a primeira gravou: uk_papel e UNIQUE
                # (empresa_id, nome) e o seed diede "NOME-1" para as 58 linhas
                # da rodada anterior. Com o id, o valor nunca se repete porque
                # o id so cresce.
                larg = min(tam, 60)
                numero = str(nome_id if nome_id is not None else n)
                if len(numero) >= larg:
                    v = numero[-larg:]
                else:
                    prefixo = ('%s-' % c[:6].upper())[:max(larg - len(numero) - 1, 0)]
                    v = (prefixo + numero)[:larg]
                return "'%s'" % v
            if c in ('cnpj', 'cpf'):
                # o tamanho da coluna manda: cpf e varchar(11) e cnpj e
                # varchar(14). Gerar 14 digitos para o cpf estoura a coluna, e
                # a mensagem do Postgres ("value too long for type character
                # varying(11)") nao diz qual coluna.
                larg = col['tamanho'] if col['tamanho'] > 0 else 14
                return "'%s'" % str(n).zfill(larg)[-larg:]
            if t == 'character varying' and col['tamanho'] <= 20 and 'status' in c:
                return "'SEED'"
            return self.texto(tabela, col, n, tam)

        # --- uuid
        if t == 'uuid':
            return 'gen_random_uuid()'

        # fallback seguro
        if col['nulo']:
            return 'NULL'
        return "'SEED'"


# --------------------------------------------------------------------------
# 4. Ordem topologica
# --------------------------------------------------------------------------

def ordenar(blocos, fks):
    """Sobe o grafo de FK: pai antes de filho. Empate vai na ordem original.

    A auto-referencia sai do conjunto de pendencias, e isso e o que faz a
    ordem inteira funcionar. bc_cad_categoria.categoria_pai_id aponta para
    bc_cad_categoria; com ela no proprio `pend`, a tabela nunca fica pronta,
    o desempate dispara, e o mesmo acontece com tudo que depende dela por
    baixo: fin_titulo depende de fin_plano_contas, com_pedido depende de
    fin_titulo, e assim por diante. Sao 14 tabelas saindo fora de ordem e
    quebrando FK na carga.

    A auto-referencia e tratada em outro lugar — a coluna entra com valor
    provisorio e um UPDATE aponta para o id real. Aqui so nao se espera por
    ela mesma.
    """
    presente = {t for t, _, _ in blocos}
    pend = {t: {p for p in fks.get(t, {}).values()
                if p in presente and p != t}
            for t, _, _ in blocos}
    original = [t for t, _, _ in blocos]
    saida, restantes, ciclos = [], set(original), []

    while restantes:
        escolhido = None
        for t in original:
            if t in restantes and not (pend[t] - set(saida)):
                escolhido = t
                break
        if escolhido is None:
            escolhido = next(t for t in original if t in restantes)
            ciclos.append(escolhido)
        saida.append(escolhido)
        restantes.discard(escolhido)
    return saida, ciclos


def colunas_ciclicas(ordem, fks):
    """Quais colunas FK nao podem ser preenchidas na hora do INSERT.

    Uma coluna FK de T para P e ciclica quando P alcança T de volta. Nao existe
    ordem de carga que satisfaca as duas: quem sobe primeiro aponta para uma
    tabela ainda vazia.

    Sao 14 tabelas neste banco.Os casos sao reais e nao artificiais:
      bc_com_pedido -> bc_com_pedido_item -> bc_com_pedido
      bc_cad_categoria -> bc_cad_categoria (pai_eh_pai)
      bc_est_expedicao -> bc_est_expedicao_item -> bc_est_expedicao

    Nesses casos a coluna entra com um valor provisorio e um UPDATE no fim
    aponta para o id real. O UPDATE e o que torna a carga util: sem ele, a
    linha fica apontando para um id que nao corresponde a nada, e o ERP le
    uma referencia quebrada sem avisar.

    O valor provisorio e 0 quando a coluna aceita nulo, e 1 quando e NOT NULL
    — porque NOT NULL e o que barra, e e checado na hora do INSERT.
    """
    # Fechamento transitivo de verdade: alc[origem] = conjunto de tabelas que
    # a origem alcanca, percorrendo o grafo inteiro.
    #
    # A versao anterior usava memorizacao dentro de um DFS com pilha de
    # deteccao de ciclo, e memorizava o resultado computado naquele contexto. Em
    # um grafo com ciclo, "a alcanca b" pode dar False porque o DFS deu de cara
    # com o ciclo, e esse False ficava guardado para sempre. Resultado: as
    # tabelas bc_com_pedido, bc_com_conferencia_fatura, bc_est_expedicao,
    # bc_est_reserva e bc_est_expedicao_item NAO eram marcadas como ciclicas,
    # recebiam 1 numa coluna NOT NULL e quebravam a FK na carga.
    #
    # Com 166 tabelas o custo e irrelevante e o resultado e correto.
    universo = [t for t in ordem if t in fks]
    alc = {}

    def calcula(origem):
        if origem in alc:
            return alc[origem]
        alc[origem] = set()          # marca em curso: ciclo nao entra
        visto = set()
        pilha = [p for p in fks.get(origem, {}).values() if p in fks]
        while pilha:
            atual = pilha.pop()
            if atual in visto:
                continue
            visto.add(atual)
            alc[origem].add(atual)
            for prox in fks.get(atual, {}).values():
                if prox in fks and prox not in visto:
                    pilha.append(prox)
        return alc[origem]

    for t in universo:
        calcula(t)

    ciclicas = defaultdict(set)
    for t in ordem:
        for c, pai in fks.get(t, {}).items():
            if pai in alc and t in alc[pai]:
                ciclicas[t].add(c)
    return ciclicas


# --------------------------------------------------------------------------
# 5. Plano
# --------------------------------------------------------------------------

def montar_plano(total, colunas, fks, unicos, compostas, checks, existentes, empresa_id):
    """Decide quantas linhas cada tabela semeada recebe, somando `total`."""
    semeaveis = []
    puladas = []
    plano_min = {}
    minimo_referencia = 8

    for t in sorted(colunas):
        if t in NUNCA_SEMEAR:
            puladas.append((t, 'nunca: ' + NUNCA_SEMEAR[t]))
            continue
        n = existentes.get(t, 0)
        if n < 0:
            puladas.append((t, 'nao consegui contar'))
            continue
        if t in REFERENCIA:
            if n > 0:
                puladas.append((t, 'referencia com %d linhas — preservada' % n))
            else:
                # Vazia: semeia o minimo so para as FK resolverem.
                semeaveis.append(t)
                plano_min[t] = minimo_referencia
            continue
        # Tabela transacional com dado e NORMAL: e o caso comum de um banco
        # de desenvolvimento. Nao existe mais um teto de linhas aqui.
        #
        # Havia, e ele pulava bc_cad_pessoa com 535 linhas — que era o seed
        # anterior, nao dado real. Referencia e ACL nao sao protegidos por
        # numero de linhas, e sim pelas listas NUNCA_SEMEAR e REFERENCIA, que
        # sao explicitas e dizem por que. Um teto generico protege o
        # errado: na hora que o seed roda duas vezes, e a tabela que ele
        # mesmo criou que some do plano.
        semeaveis.append(t)

    if not semeaveis:
        raise SystemExit('nenhuma tabela semeavel')

    #Peso: tabelas que sao o centro de um modulo recebem mais linhas, porque
    # e delas que o usuario olha quando abre a tela.
    PESO = {
        'bc_cad_produto': 3, 'bc_cad_pessoa': 3, 'bc_cad_cliente': 2,
        'bc_cad_fornecedor': 2, 'bc_cad_servico': 2, 'bc_cad_categoria': 1,
        'bc_cad_marca': 1, 'bc_cad_unidade_medida': 1, 'bc_cad_transportadora': 1,
        'bc_ven_pedido': 4, 'bc_ven_pedido_item': 6, 'bc_fin_titulo': 4,
        'bc_fin_titulo_parcela': 5, 'bc_com_pedido': 3, 'bc_com_pedido_item': 4,
        'bc_rh_funcionario': 3, 'bc_rh_cargo': 1, 'bc_est_deposito': 2,
        'bc_prod_ordem': 2, 'bc_srv_ordem_servico': 2,
    }
    pesos = [PESO.get(t, 1) for t in semeaveis]
    soma = float(sum(pesos))
    plano = {}
    for t, p in zip(semeaveis, pesos):
        if t in plano_min:
            plano[t] = plano_min[t]     # referencia vazia: o minimo so
            continue
        n = max(1, int(round(total * p / soma)))
        plano[t] = min(n, 500)          # teto: 500 por tabela evita arquivo gigante

    # Pai de FK que tambem e UNIQUE precisa ter linha para cada filho, senao a
    # segunda linha do filho bate no indice unico. bc_cad_cliente.pessoa_id e
    # UNIQUE: dois clientes nao podem apontar para a mesma pessoa. Com 30
    # pessoas e 40 clientes, 10 linhas nao tem para onde apontar.
    ajustes = []
    mudou = True
    while mudou:                      # pode propagar em cascata (A->B->C)
        mudou = False
        for filho in list(plano):
            for col, pai in fks.get(filho, {}).items():
                if col not in unicos.get(filho, ()):
                    continue
                if pai not in plano or plano[pai] >= plano[filho]:
                    continue
                antes = plano[pai]
                plano[pai] = plano[filho]
                mudou = True
                ajustes.append((pai, antes, plano[pai], filho, col))
    if ajustes:
        print('  ajuste por FK unica (%d):' % len(ajustes))
        for pai, antes, depois, filho, col in ajustes[:8]:
            print('      %-22s %3d -> %3d linhas  (exigido por %s.%s)'
                  % (pai, antes, depois, filho, col))

    # Tabela com FK NOT NULL para um pai que nao existe nao pode ser semeada.
    # A verificacao de "existe" e feita em gerar(), DEPOIS de ler os ids reais
    # dos pais que nao sao semeados: bc_core_usuario tem 3 linhas no banco de
    # trabalho e zero no banco novo, e a resposta muda. Aqui so se marca quais
    # colunas sao NOT NULL, que nao depende do banco.
    return plano, puladas


# --------------------------------------------------------------------------
# 6. Escrita
# --------------------------------------------------------------------------

def gerar(banco, total, semente, saida, dry_run):
    colunas, fks, unicos, compostas, checks, existentes = introspecionar(banco)

    empresa_id = int(psql('select min(id) from brasil_saas.bc_core_empresa', banco).strip() or 1)
    print('  banco .............. %s' % banco)
    print('  tabelas ............ %d' % len(colunas))
    print('  empresa de destino . id %d' % empresa_id)
    print('  chaves estrangeiras  %d' % sum(len(v) for v in fks.values()))

    plano, puladas = montar_plano(total, colunas, fks, unicos, compostas, checks, existentes, empresa_id)
    print('  tabelas semeadas ... %d' % len(plano))
    print('  tabelas puladas .... %d' % len(puladas))
    for t, m in puladas:
        print('      %-28s %s' % (t, m))

    blocos = [(t, 0, 0) for t in sorted(plano)]
    ordem, ciclos = ordenar(blocos, fks)
    ciclicas = colunas_ciclicas(ordem, fks)
    if ciclos:
        n_cols = sum(len(v) for v in ciclicas.values())
        print('  ciclo de FK ..... %d tabela(s), %d coluna(s) — entram com '
              'provisorio e sao corrigidas por UPDATE no fim' % (len(ciclos), n_cols))
        for t in sorted(ciclicas)[:8]:
            print('      %-26s %s' % (t, ', '.join(sorted(ciclicas[t]))))

    # quem tem CHECK desconhecido
    desconhecidos = []
    for t in ordem:
        r = classificar_checks(checks.get(t, []))
        for nome, defi in r['desconhecido']:
            desconhecidos.append((t, nome, defi))
    if desconhecidos:
        print('  AVISO: %d CHECK(s) nao reconhecidos; a carga pode falhar neles'
              % len(desconhecidos))
        for t, nome, defi in desconhecidos[:6]:
            print('      %-26s %s' % (t, defi[:88]))

    if dry_run:
        print('\n  --dry-run: nada foi escrito.')
        print('  %d registros planejados em %d tabelas.' % (sum(plano.values()), len(plano)))
        return 0

    regras_por_tabela = {t: classificar_checks(checks.get(t, [])) for t in ordem}
    max_existente = {t: existentes.get(t, 0) for t in ordem}
    # id seguinte: comeca acima do maior id que ja existe na tabela.
    # Nem toda tabela tem coluna id — bc_core_banco e uma delas, e a query
    # "select max(id)" nela falha e derrubava a geracao inteira.
    tem_id = {t: any(c['nome'] == 'id' for c in colunas[t]) for t in ordem}
    sem_id = [t for t in ordem if not tem_id[t]]
    if sem_id:
        print('  tabelas sem coluna id (%d): %s' %
              (len(sem_id), ', '.join(sem_id[:6])))
    proximo = {}
    for t in ordem:
        if not tem_id[t]:
            proximo[t] = 1
            continue
        try:
            proximo[t] = int(psql(
                'select coalesce(max(id),0) from brasil_saas.%s' % t, banco, 60).strip() or 0) + 1
        except Exception:
            proximo[t] = 1

    ids_por_tabela = {}
    for t in ordem:
        if not tem_id.get(t, True):
            continue
        base = proximo[t]
        ids_por_tabela[t] = list(range(base, base + plano[t]))

    # A empresa e dado real, nao dado sintetico. Se ja existe, ela NAO e
    # semeada — apenas os ids dela ficam disponiveis para as FK apontarem.
    #
    # A primeira versao fazia o oposto: sobrescrevia
    # ids_por_tabela['bc_core_empresa'] = [empresa_id] para que os filhos
    # apontassem para ela, e o proprio INSERT de empresa saia com id 1, que ja
    # existia:
    #   ERROR: duplicate key value violates unique constraint
    #          "bc_core_empresa_pkey"
    if existentes.get('bc_core_empresa', 0) > 0:
        del plano['bc_core_empresa']
        ids_por_tabela['bc_core_empresa'] = [empresa_id]
        print('  empresa ............ preservada (id %d), nao semeada' % empresa_id)
    else:
        # Banco novo: a empresa precisa ser criada antes de qualquer coisa.
        base = proximo['bc_core_empresa']
        plano['bc_core_empresa'] = 1
        ids_por_tabela['bc_core_empresa'] = [base]
        empresa_id = base
        print('  empresa ............ criada (id %d)' % empresa_id)

    # Pai que NAO e semeado mas ja tem dado: le os ids reais para as FK
    # apontarem para algo que existe. Sem isso, bc_core_notificacao.usuario_id
    # receberia um id inventado — e a resposta muda conforme o banco: o de
    # trabalho tem 3 usuarios, o novo tem 0. Ler o id e a unica forma de o
    # gerador funcionar nos dois.
    lidos = 0
    for t in list(plano):
        for col, pai in fks.get(t, {}).items():
            if pai in ids_por_tabela:
                continue
            if existentes.get(pai, 0) <= 0:
                continue
            if not any(c['nome'] == 'id' for c in colunas.get(pai, [])):
                continue
            try:
                amostra = psql('select id from brasil_saas.%s limit 200' % pai,
                               banco, 30).strip()
            except Exception:
                continue
            if amostra:
                ids_por_tabela[pai] = [int(x) for x in amostra.split('\n') if x.strip()]
                lidos += 1

    # Agora sim: tabela com FK NOT NULL para pai que nao tem id nenhum sai.
    removidas = []
    mudou = True
    while mudou:
        mudou = False
        for t in list(plano):
            for col, pai in fks.get(t, {}).items():
                if ids_por_tabela.get(pai):
                    continue
                cc = next((c for c in colunas[t] if c['nome'] == col), None)
                if cc is not None and not cc['nulo']:
                    del plano[t]
                    removidas.append((t, col, pai))
                    mudou = True
                    break
    if removidas:
        print('  tabelas sem FK possivel (%d):' % len(removidas))
        for t, col, pai in removidas[:8]:
            print('      %-30s %s -> %s  (NOT NULL, pai sem dado)' % (t, col, pai))
    if lidos:
        print('  ids lidos de pais nao semeados: %d tabela(s)' % lidos)

    # o plano pode ter mudado; recalcula a ordem
    ordem = [t for t in ordem if t in plano]

    rnd = random.Random(semente)
    ger = Gerador(rnd, fks, unicos, regras_por_tabela, ids_por_tabela,
                  max_existente, empresa_id, semente)

    blocos_sql = []
    for t in ordem:
        r = classificar_checks(checks.get(t, []))
        cols = colunas[t]

        # Mapa coluna -> (posicao na composta, cardinalidades) para as
        # compostas com mais de uma coluna variavel.
        combo = {}
        # Composta em que as colunas que variam NAO sao FK: uk_cnae_codigo_lc116
        # e (codigo, lc116_cc116) e as duas sao texto. Nao ha lista de pai para
        # sortear. Aqui a primeira coluna que varia carrega o numero da linha, o
        # que garante a unicidade da combinacao, e as outras ficam constantes.
        for grupo in compostas.get(t, []):
            variaveis = [c for c in grupo if c != 'empresa_id']
            if len(variaveis) < 2:
                continue
            fk_cols = [c for c in variaveis if c in fks.get(t, {})]
            if len(fk_cols) == len(variaveis):
                card = [len(ids_por_tabela.get(fks[t][c], [])) or 1 for c in variaveis]
                for pos, c in enumerate(variaveis):
                    combo[c] = ('indice', pos, card)
                continue
            Leader = None
            for c in variaveis:
                if c in fks.get(t, {}):
                    continue
                cc = next((x for x in cols if x['nome'] == c), None)
                if cc is None:
                    continue
                Leader = c
                break
            if Leader:
                combo[Leader] = ('linha', 0, None)
                for c in variaveis:
                    if c != Leader and c not in combo:
                        combo[c] = ('constante', 0, None)

        n_linhas = len(ids_por_tabela.get(t, []))
        if n_linhas == 0:
            continue
        nomes = [c['nome'] for c in cols]
        # Se a contagem de colunas e de valores divergir, a carga vai morrer
        # em "INSERT has more target columns than expressions" — sem dizer
        # qual tabela. Checar aqui e uma linha e economiza o ciclo inteiro de
        # carga. Ja aconteceu: o append estava dentro do else e os outros
        # ramos da cadeia nao anexavam nada.
        if len(nomes) != len(set(nomes)):
            duplicadas = sorted({n for n in nomes if nomes.count(n) > 1})
            print('  AVISO: %s tem coluna repetida na introspeccao: %s'
                  % (t, ', '.join(duplicadas)))
        linhas = []
        for k, rid in enumerate(ids_por_tabela[t], start=1):
            valores = []
            na_da_linha = {}      # valor ja gerado nesta linha, por coluna
            for c in cols:
                nome = c['nome']
                # Cada ramo atribui `v` e o append acontece UMA vez, no fim do
                # corpo do laco. Antes o append estava dentro do ultimo elif,
                # entao os outros ramos definiam o valor e nao anexavam nada —
                # e o INSERT saia com menos colunas que valores. Fica assim
                # porque `v` e lido na linha seguinte para a regra de CHECK.
                if nome == 'id':
                    v = str(rid)
                elif nome == 'uuid':
                    v = 'gen_random_uuid()'
                elif nome in ('created_at', 'updated_at'):
                    v = 'now()'
                elif nome == 'deleted_at':
                    v = 'NULL'
                elif nome in ('created_by', 'updated_by', 'criado_por',
                              'atualizado_por', 'criado_por_id'):
                    v = '1' if not c['nulo'] else 'NULL'
                elif nome in ciclicas.get(t, ()):
                    # Coluna dentro de ciclo de FK: entra com provisorio.
                    # NULL quando aceita nulo, 1 quando e NOT NULL — NOT NULL e
                    # validado na hora do INSERT. O UPDATE do fim aponta para
                    # o id real.
                    v = 'NULL' if c['nulo'] else '1'
                elif combo.get(nome):
                    modo, pos, card = combo[nome]
                    if modo == 'constante':
                        v = "'1'" if c['tamanho'] > 1 else '1'
                    elif modo == 'linha':
                        # A coluna que carrega a unicidade da combinacao. Usa o
                        # id da linha pelo mesmo motivo do ramo UNIQUE.
                        larg = c['tamanho'] if c['tamanho'] > 0 else 30
                        larg = min(larg, 40)
                        if 'numeric' in c['tipo'] or 'int' in c['tipo']:
                            v = str(rid)
                        else:
                            num = str(rid)
                            pref = ('%s-' % nome[:6].upper())[:max(larg - len(num) - 1, 0)]
                            v = "'%s'" % ((pref + num)[:larg])
                    else:
                        # Divisao de indice sobre a lista de ids do pai.
                        pai = fks.get(t, {}).get(nome)
                        lista_pai = ids_por_tabela.get(pai) or []
                        if not lista_pai:
                            v = 'NULL' if c['nulo'] else '1'
                        else:
                            passo = 1
                            for antes in card[:pos]:
                                passo *= max(antes, 1)
                            # O indice calculado e uma POSICAO na lista do pai,
                            # e o valor gravado e o id que está nessa posicao.
                            # Gravar o indice direto quebra sempre que os ids
                            # do pai nao comecam em 1 — e nao comecam: no banco
                            # de trabalho bc_cad_produto ja vai ate 9012, por
                            # causa de um produto criado na importacao de NFe.
                            # Era o que fazia bc_cad_produto_kit.kit_id
                            # estourar a FK com o valor 1.
                            idx = (((k - 1) // passo) % max(card[pos], 1))
                            v = str(lista_pai[idx % len(lista_pai)])
                else:
                    v = ger.cabe(ger.valor(t, c, k, rid), c)

                # CHECK de "diferente de": o segundo lado nao pode ser igual ao
                # primeiro. Deslocar +1 e o truque obvio e quebra em dois
                # jeitos: se o valor for o maior da lista, +1 aponta para id
                # que nao existe; e se a lista for circular, +1 volta no
                # primeiro. Aqui o segundo lado e o PROXIMO da lista do pai,
                # dando a volta. ck_est_transferencia_depositos e exatamente
                # esse caso: deposito_origem_id <> deposito_destino_id.
                for a, b in r['nao_igual']:
                    if nome != b or a not in na_da_linha:
                        continue
                    proibido = na_da_linha[a]
                    lista = [str(x) for x in
                             (ids_por_tabela.get(fks.get(t, {}).get(b)) or [])]
                    if len(lista) >= 2 and proibido in lista:
                        v = lista[(lista.index(proibido) + 1) % len(lista)]
                    else:
                        # A coluna NAO e FK, entao nao ha lista de ids para
                        # percorrer. bc_prod_estrutura.produto_pai_id e
                        # produto_filho_id sao bigint soltos, sem FK no banco —
                        # so o CHECK (pai <> filho). Aqui somar 1 basta e sempre
                        # produz um valor diferente, que e o que o CHECK pede.
                        try:
                            v = str(int(proibido) + 1)
                        except (TypeError, ValueError):
                            v = ger.cabe("'%s'" % proibido[:max(
                                (c['tamanho'] if c['tamanho'] > 0 else 30) - 1, 1)], c)

                valores.append(v)
                na_da_linha[nome] = v
            linhas.append('(' + ', '.join(valores) + ')')
        blocos_sql.append(
            '-- %s: %d registro(s)\nINSERT INTO brasil_saas.%s (%s) VALUES\n%s;'
            % (t, n_linhas, t, ', '.join(nomes), ',\n'.join(linhas)))

    # --- Updates que fecham os ciclos de FK --------------------------- #
    updates = []
    for t in ordem:
        for c in sorted(ciclicas.get(t, ())):
            pai = fks[t][c]
            if pai not in ids_por_tabela or not ids_por_tabela[pai]:
                continue
            # apontar para o mesmo id da propria linha quando o pai for a
            # propria tabela (categoria_pai, por exemplo) e para o primeiro
            # id real do pai caso contrario
            if pai == t:
                # Auto-referencia: aponta para a propria linha. A coluna
                # entrava com NULL e agora tem que apontar para algo que
                # existe. 's.id' nao servia — nao ha alias 's' no UPDATE, e o
                # erro "missing FROM-clause entry for table s" so aparecia
                # depois de 9.212 linhas carregadas.
                expr = 'id'
            else:
                expr = str(ids_por_tabela[pai][0])
            updates.append(
                "-- %s.%s -> %s (ciclo de FK corrigido apos a carga)\n"
                "UPDATE brasil_saas.%s SET %s = %s WHERE id IN (%s);"
                % (t, c, pai, t, c, expr,
                   ', '.join(str(x) for x in ids_por_tabela[t])))

    cabecalho = """--
-- BRASIL-SAAS ERP — SEED DE DESENVOLVIMENTO
-- Gerado por scripts/gerar_seed_desenvolvimento.py
--
-- %d registros em %d tabelas, carga em ordem topologica de FK.
-- Semente %d: a mesma semente gera o mesmo arquivo.
--
-- NAO contem nota fiscal. As tabelas bc_fis_nfe, bc_fis_nfse, bc_fis_mdfe e
-- bc_fis_cte sao puladas de proposito — emissao de nota e por outro caminho e
-- nao se resolve com INSERT.
--
-- NAO contem tabela de referencia. Permissao, modulo, perfil, NCM, CFOP, CEST,
-- ISSQN, municipio e historico do Flyway ficam intactos: sao dados reais e
-- dado sintetico em tabela de referencia e pior que tabela vazia.
--
-- Para carregar:
--   psql -h localhost -U postgres -d brasil_saas -v ON_ERROR_STOP=1 \\
--        -f <este arquivo>
--
""" % (sum(len(ids_por_tabela.get(t, [])) for t in ordem), len(ordem), semente)

    # Sem plpgsql. Um "DO $$ ... $$;" com 160 linhas de setval falha de um jeito
    # dificil de ver: um unico setval cujo nome de sequence volte com caractere
    # inesperado abre uma string, e o parser reclama "syntax error at or near
    # PERFORM" na linha do $$, blameando o bloco inteiro em vez da linha.
    # SELECT solto nao tem string delimitada e cada setval e independente: um
    # que falhe nao derruba os outros.
    rodape = ['\n-- Sequencias: deixa a proxima insercao da aplicacao acima do seed.']
    for t in ordem:
        if not ids_por_tabela.get(t):
            continue
        try:
            seq = psql("select pg_get_serial_sequence('brasil_saas.%s','id')" % t,
                       banco, 30).strip()
        except Exception:
            continue
        if not seq.startswith('brasil_saas.'):
            continue          # tabela sem sequence: nao ha o que ajustar
        rodape.append(
            "SELECT setval('%s', GREATEST((SELECT COALESCE(MAX(id),0) FROM brasil_saas.%s), 1));"
            % (seq, t))

    # A ordem das partes importa: cabecalho, INSERT, UPDATE, setval. O UPDATE
    # vai DEPOIS do INSERT porque corrige linha que o INSERT acabou de criar —
    # na frente, o WHERE nao acha nada e o ciclo fica aberto sem erro nenhum.
    partes = [cabecalho, '\n\n'.join(blocos_sql)]
    if updates:
        partes.append(
            '-- Correcao das colunas em ciclo de FK.\n'
            '-- Entram com valor provisorio porque nao existe ordem de carga\n'
            '-- que satisfaca os dois lados do ciclo ao mesmo tempo.\n'
            + '\n\n'.join(updates))
    partes.append('\n'.join(rodape))
    corpo = '\n\n'.join(partes) + '\n'
    with open(saida, 'w', encoding='utf-8') as f:
        f.write(corpo)
    total_escrito = sum(len(ids_por_tabela.get(t, [])) for t in ordem)
    print('  gerado ............. %s (%d bytes)' % (saida, len(corpo.encode('utf-8'))))
    print('  %d registros em %d tabelas' % (total_escrito, len(ordem)))
    print('  %d UPDATE(s) de correcao de ciclo' % len(updates))
    if total_escrito != sum(plano.values()):
        # A diferenca vem das tabelas removidas DEPOIS do plano, por FK NOT
        # NULL sem pai. Anunciar o numero planejado faz o arquivo prometer mais
        # do que entrega: o cabecalho dizia 9.274 e o banco ficou com 9.212.
        print('  (plano de %d; %d registro(s) sairam com as tabelas removidas '
              'por FK NOT NULL sem pai)'
              % (sum(plano.values()), sum(plano.values()) - total_escrito))
    return 0


def main():
    ap = argparse.ArgumentParser(description='Gera seed de desenvolvimento do ERP')
    # O BANCO de conexao do psql. O SCHEMA e outro: o SQL gerado qualifica as
    # tabelas como brasil_saas.<tabela>, e sao coisas distintas desde o rename.
    ap.add_argument('--banco', default='brasil-saas')
    ap.add_argument('--total', type=int, default=10000)
    ap.add_argument('--semente', type=int, default=42)
    ap.add_argument('--saida', default='dump/seed_desenvolvimento.sql')
    ap.add_argument('--dry-run', action='store_true')
    a = ap.parse_args()
    try:
        return gerar(a.banco, a.total, a.semente, a.saida, a.dry_run)
    except RuntimeError as e:
        print('  ERRO: %s' % e, file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
