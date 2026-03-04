#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASE_DIR="$ROOT_DIR/tests/artifacts/mobile-device-runs"
mkdir -p "$BASE_DIR/media"

android_report="$BASE_DIR/android-critical-flow/report.junit.xml"
ios_report="$BASE_DIR/ios-critical-flow/report.junit.xml"
android_log="$BASE_DIR/android-critical-flow/device.log"
ios_log="$BASE_DIR/ios-critical-flow/device.log"

android_status="missing"
ios_status="missing"
[[ -f "$android_report" ]] && android_status="present"
[[ -f "$ios_report" ]] && ios_status="present"

cat > "$BASE_DIR/critical-flow-summary.json" <<JSON
{
  "generatedAt": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "androidReport": "$android_status",
  "iosReport": "$ios_status",
  "androidLogExists": $( [[ -f "$android_log" ]] && echo true || echo false ),
  "iosLogExists": $( [[ -f "$ios_log" ]] && echo true || echo false )
}
JSON

cat > "$BASE_DIR/media/README.md" <<'MD'
# Media Evidence

Add screenshots/videos for:

- Voice hold-to-talk add success (he-IL RTL)
- Select/remove flow with persistent top-start control
- Offline -> reconnect replay
- Two-member household sync updates

Include timestamp, device model, OS version, and test run ID in filenames.
MD

echo "Collected and summarized mobile artifacts at: $BASE_DIR"
