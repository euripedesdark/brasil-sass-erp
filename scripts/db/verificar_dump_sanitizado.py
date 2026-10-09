#!/usr/bin/env python3
"""Confere integridade e dados permitidos sem exibir linhas do dump."""
import argparse
import hashlib
import json
from pathlib import Path
import re

REFERENCE_TABLES = {
    "bc_fis_issqn", "bc_fis_ncm", "bc_fis_cest", "bc_fis_cfop",
    "bc_fis_cnae_servico", "bc_fis_servico_lc116", "bc_fis_palavra_chave",
    "bc_cad_municipio",
}


def verify(directory):
    checks = {}
    for line in (directory / "CHECKSUMS.md5").read_text().splitlines():
        if not line.strip():
            continue
        digest, name = line.split()
        name = name.lstrip("*")
        if not re.fullmatch(r"parte-\d{2}", name) or name in checks:
            raise ValueError("Manifesto de partes invalido")
        checks[name] = digest
    if sorted(checks) != [f"parte-{i:02d}" for i in range(14)]:
        raise ValueError("Sao exigidas as 14 partes completas")
    pending = b""
    copying = None
    counts = {}
    size = 0
    for name, expected in sorted(checks.items()):
        digest = hashlib.md5()
        with (directory / name).open("rb") as source:
            while chunk := source.read(1024 * 1024):
                digest.update(chunk)
                size += len(chunk)
                lines = (pending + chunk).split(b"\n")
                pending = lines.pop()
                for line in lines:
                    if copying:
                        if line == b"\\.":
                            copying = None
                        else:
                            counts[copying] += 1
                    elif line.startswith(b"COPY "):
                        table = line.split()[1].decode("ascii")
                        if table not in {"brasil_saas." + t for t in REFERENCE_TABLES}:
                            raise ValueError("COPY fora das tabelas de referencia autorizadas")
                        copying = table
                        counts.setdefault(table, 0)
        if digest.hexdigest() != expected:
            raise ValueError("Checksum divergente: " + name)
    if copying or pending.strip():
        raise ValueError("Dump truncado ou COPY sem terminador")
    if set(counts) != {"brasil_saas." + t for t in REFERENCE_TABLES}:
        raise ValueError("Tabelas de referencia ausentes")
    return {"parts": len(checks), "bytes": size, "rows": counts}


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path, nargs="?", default=Path("dump-sanitizado-partes"))
    arguments = parser.parse_args()
    print(json.dumps(verify(arguments.directory), sort_keys=True))
