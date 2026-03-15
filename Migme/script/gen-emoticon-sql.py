#!/usr/bin/env python3
"""Generate emoticon INSERT SQL from a template and TSV data file.

Usage:
    python3 gen-emoticon-sql.py <template-sql> <data-tsv> <output-sql>

Example:
    python3 gen-emoticon-sql.py sql/emoticon-template.sql /tmp/warriors.tsv /tmp/warriors-emoticon.sql

TSV columns (no header):
    PackName  PackFolder  FilePrefix  Alias  HotKey
"""

import sys

args = sys.argv[1:]
if len(args) != 3:
    print(f"Usage: {sys.argv[0]} template-file data-file output-file")
    print(f"  e.g.: {sys.argv[0]} sql/emoticon-template.sql /tmp/warriors.tsv /tmp/out.sql")
    print()
    print("  TSV columns: PackName  PackFolder  FilePrefix  Alias  HotKey")
    sys.exit(1)

template_file, data_file, output_file = args[0], args[1], args[2]

col_map = {0: 'pack', 1: 'folder', 2: 'fileprefix', 3: 'alias', 4: 'hotkey'}

with open(template_file, encoding="utf-8") as f:
    template = f.read()

count = 0
with open(data_file, encoding="utf-8") as fin, \
     open(output_file, 'w', encoding="utf-8") as fout:
    for line in fin:
        line = line.rstrip('\r\n')
        if not line or line.startswith('#'):
            continue
        cols = line.split('\t')
        if len(cols) != len(col_map):
            print(f"Error: expected {len(col_map)} columns, got {len(cols)}: {line!r}")
            continue
        row = {v: cols[k] for k, v in col_map.items()}
        fout.write(template % row)
        fout.write('\n')
        count += 1

print(f"Wrote {count} emoticon(s) to {output_file}")
