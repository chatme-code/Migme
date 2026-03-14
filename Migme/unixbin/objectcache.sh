#!/bin/bash
# Start Object Cache
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.objectcache.ObjectCache \
    "$SCRIPT_DIR/../etc/ObjectCache.cfg" "$@"
