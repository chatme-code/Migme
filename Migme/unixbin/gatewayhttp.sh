#!/bin/bash
# Start HTTP Gateway  (requires root for port < 1024)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec sudo "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.gateway.Gateway \
    "$SCRIPT_DIR/../etc/GatewayHTTP.cfg" "$@"
