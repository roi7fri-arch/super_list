#!/usr/bin/env bash
set -euo pipefail

ORANGE_HOST="${ORANGE_HOST:-orangepi}"
ORANGE_BASE_DIR="${ORANGE_BASE_DIR:-/root/vs_workspace}"
LOCAL_BASE_DIR="${LOCAL_BASE_DIR:-/home/roifr/vs_workspace}"
SKIP_INSTALL="${SKIP_INSTALL:-0}"

usage() {
  cat <<'EOF'
Deploy all projects from local PC to Orange Pi.

Usage:
  scripts/sync/deploy-orange-pi.sh [--skip-install]

Options:
  --skip-install   Skip npm install on Orange Pi

Environment overrides:
  ORANGE_HOST      SSH host alias (default: orangepi)
  ORANGE_BASE_DIR  Remote base path (default: /root/vs_workspace)
  LOCAL_BASE_DIR   Local base path (default: /home/roifr/vs_workspace)
  SKIP_INSTALL     1 to skip install (same as --skip-install)
EOF
}

for arg in "$@"; do
  case "$arg" in
    --skip-install)
      SKIP_INSTALL=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $arg" >&2
      usage
      exit 1
      ;;
  esac
done

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd rsync
require_cmd ssh

PROJECTS=(
  "ai_competition_website"
  "hebrew-book-recommender-main"
  "super_list"
)

RSYNC_EXCLUDES=(
  --exclude=.git/
  --exclude=node_modules/
  --exclude=dist/
  --exclude=build/
  --exclude=.next/
  --exclude=.gradle/
  --exclude=android/.gradle/
  --exclude=android/build/
  --exclude=android/app/build/
  --exclude=tests/artifacts/
  --exclude=*.log
)

echo "[1/4] Checking SSH connectivity to ${ORANGE_HOST}"
ssh -o ConnectTimeout=8 "$ORANGE_HOST" 'echo connected >/dev/null'

echo "[2/4] Syncing projects to ${ORANGE_HOST}:${ORANGE_BASE_DIR}"
for p in "${PROJECTS[@]}"; do
  src="${LOCAL_BASE_DIR}/${p}/"
  dst="${ORANGE_HOST}:${ORANGE_BASE_DIR}/${p}/"
  if [[ ! -d "$src" ]]; then
    echo "Skipping missing local project: $src"
    continue
  fi

  echo "  - rsync $p"
  rsync -az --delete "${RSYNC_EXCLUDES[@]}" "$src" "$dst"
done

if [[ "$SKIP_INSTALL" == "1" ]]; then
  echo "[3/4] Skipping npm install on Orange Pi"
else
  echo "[3/4] Installing dependencies on Orange Pi"
  ssh "$ORANGE_HOST" "
    set -e
    npm --prefix '${ORANGE_BASE_DIR}/ai_competition_website/backend' install --no-fund --no-audit
    npm --prefix '${ORANGE_BASE_DIR}/ai_competition_website/frontend' install --no-fund --no-audit
    npm --prefix '${ORANGE_BASE_DIR}/hebrew-book-recommender-main/api' install --no-fund --no-audit
    npm --prefix '${ORANGE_BASE_DIR}/hebrew-book-recommender-main/web' install --no-fund --no-audit
    npm --prefix '${ORANGE_BASE_DIR}/super_list/api' install --no-fund --no-audit
  "
fi

echo "[4/4] Restarting Orange Pi services"
ssh "$ORANGE_HOST" "
  set -e
  systemctl restart ai-backend.service ai-frontend.service hebrew-api.service hebrew-web.service super-list-api.service cloudflared-family-ai.service
  sleep 2
  systemctl is-active ai-backend.service ai-frontend.service hebrew-api.service hebrew-web.service super-list-api.service cloudflared-family-ai.service
"

echo "Deploy completed successfully."
