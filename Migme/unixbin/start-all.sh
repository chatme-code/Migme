#!/bin/bash
# ============================================================
# start-all.sh  –  Start all Fusion services in the background
#
# Each service's stdout/stderr is written to logs/<service>.log
# A PID file is created at logs/<service>.pid
#
# Usage: ./start-all.sh
# Stop : ./stop-all.sh
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FUSION_BASE="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$FUSION_BASE/logs"

mkdir -p "$LOG_DIR"

start_service() {
    local name="$1"; shift
    local log="$LOG_DIR/${name}.log"
    local pid_file="$LOG_DIR/${name}.pid"

    if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
        echo "  [SKIP] $name already running (pid $(cat "$pid_file"))"
        return
    fi

    echo "  [START] $name → $log"
    nohup "$@" >> "$log" 2>&1 &
    echo $! > "$pid_file"
    sleep 0.5
}

echo "──────────────────────────────────────────────────────"
echo "  Starting Fusion services"
echo "  Logs in: $LOG_DIR"
echo "──────────────────────────────────────────────────────"

start_service "registry"     "$SCRIPT_DIR/registry.sh"
start_service "sessioncache" "$SCRIPT_DIR/sessioncache.sh"
start_service "objectcache"  "$SCRIPT_DIR/objectcache.sh"
start_service "botserver"    "$SCRIPT_DIR/botserver.sh"
start_service "monitor"      "$SCRIPT_DIR/monitor.sh"
start_service "smsengine"    "$SCRIPT_DIR/smsengine.sh"
start_service "voiceengine"  "$SCRIPT_DIR/voiceengine.sh"
start_service "emailalert"   "$SCRIPT_DIR/emailalert.sh"

echo ""
echo "  ✓ All services started. Use ./stop-all.sh to stop them."
echo ""
