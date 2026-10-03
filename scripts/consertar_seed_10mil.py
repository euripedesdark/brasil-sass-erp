#!/usr/bin/env python3
"""Conserta os 9 INSERTs corrompidos do seed de 10.000 registros.

O QUE ESTA CORROMPIDO

Nove INSERTs tem um fragmento de DDL colado no fim da lista de colunas:

    INSERT INTO brasil_saas.bc_est_transferencia (id, empresa_id, ...,
                   deleted_at, CONSTRAINT)
                                                   ^^^^^^^^^^^^ nao e coluna

O "CONSTRAINT" e o inicio de um `ALTER TABLE ... ADD CONSTRAINT` que o
gerador emendou no texto. E nao e so cosmetico: o gerador contou esse token
como coluna, e por isso emitiu UM VALOR A MAIS no SELECT do que colunas
existem. Sem tirar os dois, o INSERT nao roda:

    ERROR: syntax error at or near "CONSTRAINT"

A diferenca entre o numero de colunas e o numero de valores varia de 1 a 4,
conforme quantos fragments de constraint havia na tabela.

POR QUE O VALOR EXTRA ESTA NO FIM

O token phantom foi acrescentado no fim da lista de colunas, entao o valor
correspondente foi acrescentado no fim do SELECT. Nao e adivinhacao: conferido
caso a caso, o valor excedente e sempre o ultimo, e ele segue o mesmo padrao do
campo de texto que o vizinho (`'TESTE-'||g` depois de `deleted_at` recebendo
timestamp, por exemplo).

O QUE ESTE SCRIPT NAO FAZ

Nao inventa dado, nao completa coluna faltando e nao mexe nas 157 outras
tabelas. Ele so remove o token que nao e coluna e o valor que nao tem coluna
para onde ir. Verificacao: o arquivo todo e recarregado num banco novo com
ON_ERROR_STOP=1, e a contagem final de linhas e conferida contra os 10.000
declarados.
"""
import re
import sys

ARQUIVO = 'dump/schema_brasil_saas_10mil.sql'

# --- parser de lista de valores, com no aninhamento e strings ------------ #

def separa_valores(sel):
    """Quebra 'a, f(b, c), d' em ['a', 'f(b, c)', 'd'].

    Ignora virgula dentro de parenteses e dentro de string, que e o que
    acontece com `(SELECT min(id) + ((g-1) % 61) FROM ...)` e com
    `'TESTE-'||g`. Um split() normal cortaria no meio do SELECT aninhado e
    produziria lixo silencioso — o pior tipo de bug, porque o script roda e
    o arquivo fica com o tamanho errado.
    """
    valores, atual = [], []
    nivel, dentro, i = 0, False, 0
    while i < len(sel):
        c = sel[i]
        if c == "'":
            # string'': '' e o escape do Postgres
            if dentro and i + 1 < len(sel) and sel[i + 1] == "'":
                atual.append(sel[i:i + 2])
                i += 2
                continue
            dentro = not dentro
            atual.append(c)
        elif not dentro:
            if c == '(':
                nivel += 1
                atual.append(c)
            elif c == ')':
                nivel -= 1
                atual.append(c)
            elif c == ',' and nivel == 0:
                valores.append(''.join(atual).strip())
                atual = []
            else:
                atual.append(c)
        else:
            atual.append(c)
        i += 1
    if ''.join(atual).strip():
        valores.append(''.join(atual).strip())
    return valores


def main():
    linhas = open(ARQUIVO, encoding='utf-8').read().split('\n')

    consertos = []
    for i, linha in enumerate(linhas):
        m = re.match(r'(INSERT INTO brasil_saas\.\w+ \()(.*)(\))\s*$', linha)
        if not m or 'CONSTRAINT' not in m.group(2):
            continue

        tabela = re.search(r'INSERT INTO brasil_saas\.(\w+)', linha).group(1)
        antes_cols, depois_cols = m.group(1), m.group(3)

        # 1) tira o token phantom da lista de colunas
        cols = [c.strip() for c in m.group(2).split(',') if c.strip()]
        n_phantom = sum(1 for c in cols if c.upper().startswith('CONSTRAINT'))
        cols_limpas = [c for c in cols if not c.upper().startswith('CONSTRAINT')]

        # 2) tira os valores excedentes do fim do SELECT
        # O FROM generate_series pode estar na mesma linha do SELECT ou na de
        # seguinte — o arquivo tem os dois formatos, e assumir um só faz o
        # script quebrar em metade dos casos.
        sel = linhas[i + 1]
        pos = sel.upper().find('FROM GENERATE_SERIES')
        if pos >= 0:
            cabeca, cauda = sel[:pos], sel[pos:]
            linha_cauda = i + 1
        elif i + 2 < len(linhas) and 'GENERATE_SERIES' in linhas[i + 2].upper():
            cabeca, cauda = sel, linhas[i + 2]
            linha_cauda = i + 2
        else:
            print("  %-28s nao achei 'FROM generate_series' — nao toco" % tabela)
            continue

        # tira o "SELECT" da frente antes de re-contar os valores. Sem isso a
        # linha sai como "SELECT SELECT g, ..." — que foi exatamente o que a
        # primeira rodada deste script produziu.
        cabeca = re.sub(r'^\s*SELECT\s+', '', cabeca, flags=re.I)

        valores = separa_valores(cabeca)
        excedente = len(valores) - len(cols_limpas)
        if excedente < 0:
            print("  %-28s valores INSUFICIENTES (%d para %d colunas) — nao toco"
                  % (tabela, len(valores), len(cols_limpas)))
            continue
        valores = valores[:len(cols_limpas)]

        # As DUAS metades do conserto. A coluna e o valor tem que sair juntos:
        # se sai so o valor, sobra o token; se sai so o token, sobra o valor. A
        # primeira versao deste script fez so a segunda metade, e por isso
        # "corrigiu" 9 INSERTs sem tirar um unico CONSTRAINT — o teste de
        # recarga foi o que denunciou, nao o relatorio do script.
        linhas[i] = antes_cols + ', '.join(cols_limpas) + depois_cols

        novo_sel = 'SELECT ' + ', '.join(valores)
        if linha_cauda == i + 1:
            linhas[i + 1] = novo_sel + ' ' + cauda
        else:
            linhas[i + 1] = novo_sel
            linhas[i + 2] = cauda

        consertos.append((tabela, n_phantom, len(cols_limpas), len(valores)))

    print("  %-30s %10s %10s %10s" % ("TABELA", "PHANTOM", "COLUNAS", "VALORES"))
    print("  " + "-" * 64)
    for t, ph, c, v in consertos:
        print("  %-30s %10d %10d %10d" % (t, ph, c, v))
    print("  " + "-" * 64)
    print("  %d INSERT(s) corrigido(s)" % len(consertos))

    if not consertos:
        print("\n  NADA A CORRIGIR — o arquivo ja esta limpo.")
        return 1

    with open(ARQUIVO, 'w', encoding='utf-8') as f:
        f.write('\n'.join(linhas))
    print("\n  gravado. Agora recarrega num banco novo e confere a contagem.")
    return 0


if __name__ == '__main__':
    sys.exit(main())
