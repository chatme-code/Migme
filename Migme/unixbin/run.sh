#!/bin/bash
# ============================================================
# run.sh  –  Fusion service launcher (Maven v9 / Spring Boot 3)
#
# Usage:
#   ./run.sh <MainClass> [args...]
#
# Environment:
#   JAVA_ARGS   – extra JVM flags (e.g. -Xmx1g)
#   FUSION_BASE – override base directory (default: parent of this script)
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FUSION_BASE="${FUSION_BASE:-$(cd "$SCRIPT_DIR/.." && pwd)}"

# ── locate fat JAR ──────────────────────────────────────────
JAR=$(ls "$FUSION_BASE/target/Fusion-"*.jar 2>/dev/null \
      | grep -v '\-sources' \
      | grep -v '\-javadoc' \
      | head -1 || true)

if [ -z "$JAR" ]; then
    echo ""
    echo "  ╔══════════════════════════════════════════════════╗"
    echo "  ║  [ERROR] Fusion JAR not found in target/         ║"
    echo "  ║  Build first:                                    ║"
    echo "  ║    cd $FUSION_BASE"
    echo "  ║    mvn package -DskipTests                       ║"
    echo "  ╚══════════════════════════════════════════════════╝"
    echo ""
    exit 1
fi

if [ $# -lt 1 ]; then
    echo "Usage: $0 <com.projectgoth.fusion.XxxMain> [config-file] [args...]"
    exit 1
fi

MAIN_CLASS="$1"; shift

mkdir -p "$FUSION_BASE/logs"

echo "──────────────────────────────────────────────────────"
echo "  Fusion Service Launcher"
echo "  JAR        : $JAR"
echo "  Main class : $MAIN_CLASS"
echo "  Config dir : $FUSION_BASE/etc/"
echo "  Log dir    : $FUSION_BASE/logs/"
echo "  Extra args : $*"
echo "──────────────────────────────────────────────────────"

ulimit -n 65536 2>/dev/null || true

exec java \
    ${JAVA_ARGS:-} \
    -server \
    -Xmx512m \
    -XX:+UseG1GC \
    -Dlog.dir="$FUSION_BASE/logs/" \
    -Dconfig.dir="$FUSION_BASE/etc/" \
    -Dloader.main="$MAIN_CLASS" \
    -cp "$JAR" \
    org.springframework.boot.loader.launch.PropertiesLauncher \
    "$@"
