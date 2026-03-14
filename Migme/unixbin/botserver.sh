#!/bin/bash
# Start BotService
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.botservice.server.BotServiceMain \
    "$SCRIPT_DIR/../etc/BotServer.cfg" "$@"
