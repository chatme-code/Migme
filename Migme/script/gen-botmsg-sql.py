#!/usr/bin/env python3
"""Generate INSERT SQL for bot messages from a TSV file.

Usage:
    python3 gen-botmsg-sql.py <BotCommandName> <input-tsv-file>

TSV columns: MessageKey <TAB> MessageValue
"""

import sys

args = sys.argv[1:]

if len(args) < 2:
    print(f"Usage: {sys.argv[0]} BotCommandName input-tsv-file")
    print(" input file two columns: key\\tmsg")
    sys.exit(1)

botname = args[0]
with open(args[1], encoding="utf-8") as f:
    for line in f:
        line = line.rstrip('\r\n')
        if not line:
            continue
        parts = line.split('\t', 2)
        if len(parts) < 2:
            continue
        key, msg = parts[0], parts[1]
        msg_escaped = msg.replace("'", "''")
        print(
            f"INSERT INTO botmessage (BotID, LanguageCode, MessageKey, MessageValue) "
            f"VALUES ((SELECT id FROM bot WHERE commandname='{botname}'), "
            f"'ENG', '{key}', '{msg_escaped}');"
        )
