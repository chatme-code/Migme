#!/bin/bash
# Start Session Cache
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.sessioncache.Main "$@"
