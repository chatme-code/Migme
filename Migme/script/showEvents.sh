#!/bin/bash
# Show pending user events
# Usage: ./showEvents.sh <userId>
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/../unixbin/run.sh" \
    com.projectgoth.fusion.userevent.ShowEventsForUser "$@"
