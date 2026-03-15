#!/bin/bash
# Start Registry
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.registry.Registry \
    "$SCRIPT_DIR/../etc/Registry.cfg" "$@"
