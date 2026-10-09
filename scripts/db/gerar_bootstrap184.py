#!/usr/bin/env python3
"""Gera o bootstrap vazio B184 do dump sanitizado e das migrations imutaveis.
Nao restaura dados, historico Flyway nem valores de sequencias do dump.
Executar antes de publicar B184; depois de aplicada, criar apenas migrations V novas.
"""
from pathlib import Path
import hashlib
import io
import re

ROOT = Path(__file__).resolve().parents[2]
MIGRATIONS = ROOT / 'src/main/resources/db/migration'
DUMP = ROOT / 'dump-sanitizado-partes'


def lines_parts():
    pending = b''
    checksums = dict(line.split()[::-1] for line in (DUMP / 'CHECKSUMS.md5').read_text().splitlines() if line.strip())
    for number in range(14):
        name = f'parte-{number:02d}'
        digest = hashlib.md5()
        with (DUMP / name).open('rb') as source:
            while chunk := source.read(1024 * 1024):
                digest.update(chunk)
                pieces = (pending + chunk).split(b'\n')
                pending = pieces.pop()
                for line in pieces:
                    yield line.decode('utf-8') + '\n'
        if digest.hexdigest() != checksums.get(name):
            raise ValueError(f'Checksum invalido: {name}')
    if pending:
        yield pending.decode('utf-8')


def schema_only():
    output = io.StringIO()
    copying = False
    for line in lines_parts():
        if copying:
            if line.rstrip() == r'\.':
                copying = False
            continue
        if line.startswith('COPY '):
            copying = True
            continue
        if line.startswith(('\\restrict', '\\unrestrict', 'SET transaction_timeout', 'SELECT pg_catalog.setval')):
            continue
        output.write(line)
    if copying:
        raise ValueError('Dump incompleto: COPY sem terminador')
    # O historico deve ser criado e preenchido exclusivamente pelo Flyway.
    blocks = output.getvalue().split('--\n-- Name: ')
    schema = blocks[0] + ''.join('--\n-- Name: ' + block for block in blocks[1:]
                                if 'flyway_schema_history' not in block.split('\n\n', 1)[0])
    schema = schema.replace('CREATE SCHEMA brasil_saas;', 'CREATE SCHEMA IF NOT EXISTS brasil_saas;')
    return schema.replace('CREATE SCHEMA dl;', 'CREATE SCHEMA IF NOT EXISTS dl;')


def main():
    parts = [schema_only()]
    files = sorted(MIGRATIONS.glob('V*.sql'), key=lambda path: int(path.name.split('__')[0][1:]))
    # Apenas sementes de permissoes/modulos, nunca dados empresariais do dump.
    for path in files:
        version = int(path.name.split('__')[0][1:])
        if version < 159:
            text = path.read_text()
            if path.name.endswith('_permissoes.sql'):
                parts.append(text)
            elif version in (129, 154):
                offset = text.find('INSERT INTO')
                if offset >= 0:
                    parts.append(text[offset:])
    # Dependencias declaradas depois de seus consumidores na serie historica.
    for version in (168, 170):
        parts.append(next(MIGRATIONS.glob(f'V{version}__*.sql')).read_text())
    parts.extend(path.read_text() for path in files if 159 <= int(path.name.split('__')[0][1:]) <= 184)
    sql = '\n\n'.join(parts)
    sql = re.sub(r'^--.*\n?', '', sql, flags=re.MULTILINE)
    sql = re.sub(r'\n{3,}', '\n\n', sql).strip() + '\n'
    if re.search(r'^COPY |CREATE TABLE .*flyway_schema_history|^SELECT pg_catalog.setval', sql, re.MULTILINE):
        raise ValueError('Bootstrap contem dados/historico indevidos')
    header = ('-- B184: bootstrap exclusivo de instalacoes novas e vazias.\n'
              '-- Esquema sem dados do dump sanitizado, consolidado com V159-V184.\n'
              '-- Inclui somente sementes de permissoes/modulos das migrations anteriores.\n'
              '-- Nao aplica baseline/repair em bancos existentes; migrations V* permanecem intactas.\n\n')
    target = MIGRATIONS / 'B184__bootstrap_erp_novo.sql'
    target.write_text(header + sql)
    print(target.name, hashlib.sha256(target.read_bytes()).hexdigest())


if __name__ == '__main__':
    main()
