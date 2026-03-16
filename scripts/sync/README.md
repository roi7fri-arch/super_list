# Sync Runtime Scripts

## Purpose
Run the local Super List shared-sync stack:
- Local API server on port `8789`
- Cloudflare Tunnel routes for:
  - Debug builds: `https://dev-list.friedman-makers.com`
  - Release builds: `https://list.friedman-makers.com`

## Scripts

1. Start both API + tunnel together:
- `./scripts/sync/start-sync-stack.sh`

1. Start local API server:
- `./scripts/sync/start-api.sh`

2. Start Cloudflare Tunnel:
- `./scripts/sync/start-cloudflared-tunnel.sh`
- Optional tunnel name argument:
  - `./scripts/sync/start-cloudflared-tunnel.sh family-ai-competition`

## Typical usage
Single command:
- `./scripts/sync/start-sync-stack.sh`

Or open 2 terminals:

Terminal A:
- `./scripts/sync/start-api.sh`

Terminal B:
- `./scripts/sync/start-cloudflared-tunnel.sh`

Then set Android app sync URL to:
- Debug APK: `https://dev-list.friedman-makers.com` (from BuildConfig)
- Release APK: `https://list.friedman-makers.com` (from BuildConfig)
