#!/bin/bash
# ============================================================
# stop-all.sh  –  Stop all running Fusion services
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FUSION_BASE="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$FUSION_BASE/logs"

echo "──────────────────────────────────────────────────────"
echo "  Stopping Fusion services"
echo "──────────────────────────────────────────────────────"

for pid_file in "$LOG_DIR"/*.pid; do
    [ -f "$pid_file" ] || continue
    name=$(basename "$pid_file" .pid)
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
        echo "  [STOP] $name (pid $pid)"
        kill -TERM "$pid" && sleep 1
        kill -0 "$pid" 2>/dev/null && kill -KILL "$pid" || true
    else
        echo "  [SKIP] $name not running"
    fi
    rm -f "$pid_file"
done

echo ""
echo "  ✓ Done."
echo ""
