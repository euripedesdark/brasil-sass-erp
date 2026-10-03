#!/usr/bin/env python3
"""Corrige os workflows do BrasilCloudERP. Rode na raiz do repositório."""
import re, sys, pathlib

wf = pathlib.Path(".github/workflows")
if not wf.is_dir():
    sys.exit("Rode na raiz do repositório (pasta .github/workflows não encontrada).")

# 1) linha solta de branch dentro do run: | do ci-database.yml
p = wf / "ci-database.yml"
s = p.read_text()
s = re.sub(r"\n[ \t]*fix/auth-modes-and-cicd-2026-10-02[ \t]*(?=\n)", "", s)
p.write_text(s)

# 2) 'with:' duplicado + linhas em branco soltas nos checkouts
dup = re.compile(
    r"(uses: actions/checkout@v4)\n\s*\n(\s*)with:\n\s*\n(\s*)submodules: recursive\n\2with:\n"
)
for f in wf.glob("*.yml"):
    s = f.read_text()
    s = re.sub(r"(      - name: [^\n]*)\n\s*\n(\s*uses: actions/checkout@v4)", r"\1\n\2", s)
    s = dup.sub(r"\1\n\2with:\n\3submodules: recursive\n", s)
    s = re.sub(
        r"(uses: actions/checkout@v4)\n\s*\n(\s*)with:\n\s*\n(\s*submodules: recursive)",
        r"\1\n\2with:\n\3", s)
    f.write_text(s)

# 3) ci-cd.yml: migrate antes de validate
p = wf / "ci-cd.yml"
lines = p.read_text().split("\n")
iv = next((i for i, l in enumerate(lines) if "flyway:validate" in l), None)
if iv is not None and "flyway:migrate" in lines[iv + 1]:
    lines[iv], lines[iv + 1] = lines[iv + 1], lines[iv]
p.write_text("\n".join(lines))
print("Correções aplicadas.")
