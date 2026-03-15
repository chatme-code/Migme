#!/bin/bash
# Start Voice Engine
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.voiceengine.VoiceEngine \
    "$SCRIPT_DIR/../etc/VoiceEngine.cfg" "$@"
