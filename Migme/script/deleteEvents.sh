#!/bin/bash
# Delete pending user events
# Usage: ./deleteEvents.sh <userId>
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/../unixbin/run.sh" \
    com.projectgoth.fusion.userevent.DeleteEventsForUser "$@"
