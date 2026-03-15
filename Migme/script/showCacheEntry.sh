#!/bin/bash
# Show a cache entry from Memcached
# Usage: ./showCacheEntry.sh <key> [host] [port]
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/../unixbin/run.sh" \
    com.projectgoth.fusion.cache.ShowCacheEntry "$@"
