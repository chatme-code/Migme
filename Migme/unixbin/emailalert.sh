#!/bin/bash
# Start Email Alert service  (requires root for raw socket / low ports)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec sudo "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.emailalert.EmailAlert \
    "$SCRIPT_DIR/../etc/EmailAlert.cfg" "$@"
