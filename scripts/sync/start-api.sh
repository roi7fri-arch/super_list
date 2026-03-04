#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR/api"

if [[ ! -f package.json ]]; then
  echo "ERROR: api/package.json not found" >&2
  exit 1
fi

if [[ ! -d node_modules ]]; then
  echo "Installing API dependencies..."
  npm install
fi

echo "Starting Super List API on port 8789..."
PORT=8789 npm start
