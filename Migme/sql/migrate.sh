#!/bin/bash
# ============================================================
# migrate.sh  –  Run Fusion SQL migration files against MySQL
#
# Prerequisites:
#   mysql CLI installed and reachable
#   Environment variables set (or edit defaults below):
#     DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME
#
# Usage:
#   ./migrate.sh                     # run all SQL files
#   ./migrate.sh path/to/file.sql    # run a single file
#
# Order of execution (when running all):
#   1. Schema / DDL  (CREATE TABLE, ALTER TABLE)
#   2. Data / DML    (INSERT INTO)
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-fusion}"
DB_PASS="${DB_PASS:-}"
DB_NAME="${DB_NAME:-fusiondb}"

MYSQL_CMD=(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" --database="$DB_NAME")
if [ -n "$DB_PASS" ]; then
    MYSQL_CMD+=(-p"$DB_PASS")
fi

run_sql() {
    local file="$1"
    echo "  → $(basename "$file")"
    "${MYSQL_CMD[@]}" < "$file"
}

# ── Single-file mode ──────────────────────────────────────────
if [ $# -ge 1 ]; then
    echo "[migrate] Running single file: $1"
    run_sql "$1"
    echo "[migrate] Done."
    exit 0
fi

# ── Run all SQL files (schema first, then data) ───────────────
echo "══════════════════════════════════════════════════════"
echo "  Fusion DB Migration"
echo "  Host : $DB_HOST:$DB_PORT"
echo "  DB   : $DB_NAME"
echo "══════════════════════════════════════════════════════"

# Schema / DDL  – files that typically contain CREATE/ALTER TABLE
SCHEMA_FILES=(
    "$SCRIPT_DIR/userid.sql"
    "$SCRIPT_DIR/UserSetting.sql"
    "$SCRIPT_DIR/UserWall.sql"
    "$SCRIPT_DIR/broadcastList.sql"
    "$SCRIPT_DIR/pendingContact.sql"
    "$SCRIPT_DIR/ContactListVersion.sql"
    "$SCRIPT_DIR/group_3.0.sql"
    "$SCRIPT_DIR/GroupCategories.sql"
    "$SCRIPT_DIR/Groups.NumMembers.sql"
    "$SCRIPT_DIR/GroupThreads.sql"
    "$SCRIPT_DIR/UserOwnedChatRooms.sql"
    "$SCRIPT_DIR/BE-845-ChatroomNUE.sql"
    "$SCRIPT_DIR/chatroomextradata.sql"
    "$SCRIPT_DIR/commenting.sql"
    "$SCRIPT_DIR/Bots.sql"
    "$SCRIPT_DIR/ContentSupplier.sql"
    "$SCRIPT_DIR/store.sql"
    "$SCRIPT_DIR/Payment.sql"
    "$SCRIPT_DIR/VAS.sql"
    "$SCRIPT_DIR/registrationcontext.sql"
    "$SCRIPT_DIR/ReferralSource.sql"
    "$SCRIPT_DIR/sessionarchive.sql"
    "$SCRIPT_DIR/MonitorDB.sql"
    "$SCRIPT_DIR/DID table.sql"
    "$SCRIPT_DIR/ratings_voting.sql"
    "$SCRIPT_DIR/sweepstakescode.sql"
    "$SCRIPT_DIR/Location.sql"
    "$SCRIPT_DIR/MerchantLocation.sql"
    "$SCRIPT_DIR/Merchant Discounts.sql"
    "$SCRIPT_DIR/CricketModule.sql"
    "$SCRIPT_DIR/CricketEventModule.sql"
    "$SCRIPT_DIR/GroupEventSMS.sql"
    "$SCRIPT_DIR/jobscheduling.sql"
    "$SCRIPT_DIR/Facebook.sql"
    "$SCRIPT_DIR/IRC-Bots.sql"
    "$SCRIPT_DIR/EmoteCommand.sql"
    "$SCRIPT_DIR/datagrid.sql"
    "$SCRIPT_DIR/ApplicationIconSize.sql"
    # Patch / ticket fixes
    "$SCRIPT_DIR/BE-245-blueLabel.sql"
    "$SCRIPT_DIR/BE-279.sql"
    "$SCRIPT_DIR/BE-437-Credential.sql"
    "$SCRIPT_DIR/BE-445-quartz.sql"
    "$SCRIPT_DIR/BE-503-groupTypes.sql"
    "$SCRIPT_DIR/BE-531-score.sql"
    "$SCRIPT_DIR/BE-531-score-alter.sql"
    "$SCRIPT_DIR/BE-679.sql"
    "$SCRIPT_DIR/BE-855.sql"
    "$SCRIPT_DIR/BE-1153.sql"
    "$SCRIPT_DIR/ENG-188.sql"
    "$SCRIPT_DIR/SMS-69.sql"
    "$SCRIPT_DIR/SNSER-223.sql"
    "$SCRIPT_DIR/SNSER-226.sql"
    "$SCRIPT_DIR/ANC.sql"
)

# Data / DML – emoticons, badges, virtual gifts, etc.
DATA_FILES=(
    "$SCRIPT_DIR/emoticon-template.sql"
    "$SCRIPT_DIR/4.1_test_emoticons.sql"
    "$SCRIPT_DIR/v4 emoticon pack.sql"
    "$SCRIPT_DIR/Version 4.1.sql"
    "$SCRIPT_DIR/Super Emoticons.sql"
    "$SCRIPT_DIR/Dec09 Emoticons.sql"
    "$SCRIPT_DIR/Dice Emoticons.sql"
    "$SCRIPT_DIR/Eid emoticon pack.sql"
    "$SCRIPT_DIR/cny_emoticons.sql"
    "$SCRIPT_DIR/cricket_emoticon.sql"
    "$SCRIPT_DIR/paintwars-emoticon.sql"
    "$SCRIPT_DIR/diwali_emot.sql"
    "$SCRIPT_DIR/newyearemoticonpack.sql"
    "$SCRIPT_DIR/indocontent.sql"
    "$SCRIPT_DIR/bruneisneakpeek.sql"
    "$SCRIPT_DIR/Ramadhan_content.sql"
    "$SCRIPT_DIR/halloween.sql"
    "$SCRIPT_DIR/halloween_indosat.sql"
    "$SCRIPT_DIR/earthday.sql"
    "$SCRIPT_DIR/Dec09 Virtual Gifts.sql"
    "$SCRIPT_DIR/cny_virtualgifts.sql"
    "$SCRIPT_DIR/virtualgifts.sql"
    "$SCRIPT_DIR/virtualgifts2.sql"
    "$SCRIPT_DIR/diwali_vg.sql"
    "$SCRIPT_DIR/indosatjun09_vg.sql"
    "$SCRIPT_DIR/Indosat VIP.sql"
    "$SCRIPT_DIR/indoringtones.sql"
    "$SCRIPT_DIR/badges.sql"
    "$SCRIPT_DIR/badges-9-16.sql"
    "$SCRIPT_DIR/fanatik-badge.sql"
    "$SCRIPT_DIR/opera-badge.sql"
    "$SCRIPT_DIR/avatar/avataritem.sql"
    "$SCRIPT_DIR/avatar/avatarbody.sql"
    "$SCRIPT_DIR/paintwars-item.sql"
    "$SCRIPT_DIR/migbo.sql"
    "$SCRIPT_DIR/migbo_guardset.sql"
    "$SCRIPT_DIR/migWars.sql"
    "$SCRIPT_DIR/Cricket.sql"
    "$SCRIPT_DIR/cricket_vg.sql"
    "$SCRIPT_DIR/Content20090821.sql"
)

echo ""
echo "── Phase 1: Schema / DDL ─────────────────────────────"
for f in "${SCHEMA_FILES[@]}"; do
    [ -f "$f" ] && run_sql "$f" || echo "  [SKIP] $(basename "$f") not found"
done

echo ""
echo "── Phase 2: Data / DML ───────────────────────────────"
for f in "${DATA_FILES[@]}"; do
    [ -f "$f" ] && run_sql "$f" || echo "  [SKIP] $(basename "$f") not found"
done

echo ""
echo "  ✓ Migration complete."
echo ""
