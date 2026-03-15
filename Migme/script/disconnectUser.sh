#!/bin/bash
# Force-disconnect a user from all gateway connections
# Usage: ./disconnectUser.sh <username|userId>
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/../unixbin/run.sh" \
    com.projectgoth.fusion.maintenance.DisconnectUser "$@"
