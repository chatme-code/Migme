#!/bin/bash
# ============================================================
# build.sh  –  Build the Fusion fat JAR using Maven
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FUSION_BASE="$(cd "$SCRIPT_DIR/.." && pwd)"

SKIP_TESTS="${SKIP_TESTS:-true}"
MVN_ARGS=""
if [ "$SKIP_TESTS" = "true" ]; then
    MVN_ARGS="-DskipTests"
fi

echo "──────────────────────────────────────────────────────"
echo "  Building Fusion  (FUSION_BASE=$FUSION_BASE)"
echo "  Maven args : $MVN_ARGS"
echo "──────────────────────────────────────────────────────"

cd "$FUSION_BASE"
mvn clean package $MVN_ARGS

JAR=$(ls "$FUSION_BASE/target/Fusion-"*.jar 2>/dev/null \
      | grep -v '\-sources' | grep -v '\-javadoc' | head -1 || true)
echo ""
echo "  ✓ Build complete"
echo "  JAR: $JAR"
echo ""
