#!/bin/bash
# Start SMS Engine
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.smsengine.SMSEngine \
    "$SCRIPT_DIR/../etc/SMSEngine.cfg" "$@"
