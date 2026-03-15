#!/bin/bash
# ============================================================
# startBotHunter.sh  –  Start BotHunter packet analyser
#
# Requires: libpcap (apt install libpcap-dev), root privileges
# Usage:    ./startBotHunter.sh [-PacketSource <iface>] [-PcapFilter '<expr>']
# Default filter: port 9119
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DEFAULT_ARGS=(
    -AnalyseRatios
    -AnalyseSequence
    -PacketSource eth0
    -AnalysisThreadCount 5
    -PcapFilter 'port 9119'
)

exec sudo "$SCRIPT_DIR/../unixbin/run.sh" \
    com.projectgoth.fusion.bothunter.BotHunter \
    "${@:-${DEFAULT_ARGS[@]}}"
