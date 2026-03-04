#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
API_SCRIPT="$ROOT_DIR/scripts/sync/start-api.sh"
TUNNEL_SCRIPT="$ROOT_DIR/scripts/sync/start-cloudflared-tunnel.sh"
TUNNEL_NAME="${1:-family-ai-competition}"
API_HEALTH_URL="http://127.0.0.1:8789/health"

if [[ ! -x "$API_SCRIPT" ]]; then
  echo "ERROR: missing executable $API_SCRIPT" >&2
  exit 1
fi

if [[ ! -x "$TUNNEL_SCRIPT" ]]; then
  echo "ERROR: missing executable $TUNNEL_SCRIPT" >&2
  exit 1
fi

cleanup() {
  echo "Stopping sync stack..."
  [[ -n "${api_pid:-}" ]] && kill "$api_pid" 2>/dev/null || true
  [[ -n "${tunnel_pid:-}" ]] && kill "$tunnel_pid" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

echo "Starting API..."
if curl -fsS "$API_HEALTH_URL" >/dev/null 2>&1; then
  echo "API already running on port 8789. Reusing existing process."
  api_pid=""
else
  "$API_SCRIPT" &
  api_pid=$!

  sleep 2
  if ! curl -fsS "$API_HEALTH_URL" >/dev/null 2>&1; then
    echo "ERROR: API failed to start on port 8789" >&2
    exit 1
  fi
fi

echo "Starting Cloudflare tunnel: $TUNNEL_NAME"
"$TUNNEL_SCRIPT" "$TUNNEL_NAME" &
tunnel_pid=$!

echo "Sync stack running. Press Ctrl+C to stop both processes."
if [[ -n "${api_pid:-}" ]]; then
  wait "$api_pid" "$tunnel_pid"
else
  wait "$tunnel_pid"
fi
