#!/bin/bash
# Start Exchange Rate Feed
# Usage: ./exchangeratefeed.sh [CURRENCY] [INTERVAL_SECONDS]
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CURRENCY="${1:-AUD}"
INTERVAL="${2:-60}"
exec "$SCRIPT_DIR/run.sh" \
    com.projectgoth.fusion.externalfeed.ExchangeRateFeed \
    "$CURRENCY" "$INTERVAL"
