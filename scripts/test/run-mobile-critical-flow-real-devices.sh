#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

"$ROOT_DIR/scripts/test/run-android-real-device.sh"
"$ROOT_DIR/scripts/test/run-ios-real-device.sh"
"$ROOT_DIR/scripts/test/collect-mobile-artifacts.sh"

echo "Completed mobile critical-flow real-device wrapper run."
