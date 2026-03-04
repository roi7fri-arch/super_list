#!/usr/bin/env bash
set -euo pipefail

TUNNEL_NAME="${1:-family-ai-competition}"
CONFIG_FILE="${HOME}/.cloudflared/config.yml"

if ! command -v cloudflared >/dev/null 2>&1; then
  echo "ERROR: cloudflared is not installed" >&2
  exit 1
fi

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "ERROR: cloudflared config not found at $CONFIG_FILE" >&2
  exit 1
fi

echo "Starting Cloudflare tunnel '$TUNNEL_NAME' using $CONFIG_FILE"
cloudflared tunnel --config "$CONFIG_FILE" run "$TUNNEL_NAME"
