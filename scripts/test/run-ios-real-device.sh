#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ARTIFACT_DIR="$ROOT_DIR/tests/artifacts/mobile-device-runs/ios-critical-flow"
mkdir -p "$ARTIFACT_DIR"

if ! command -v xcodebuild >/dev/null 2>&1; then
  echo "ERROR: xcodebuild not found. Run on macOS with Xcode installed." >&2
  exit 1
fi

DEVICE_ID="${IOS_DEVICE_ID:-}"
if [[ -z "$DEVICE_ID" ]]; then
  echo "ERROR: set IOS_DEVICE_ID to a connected iOS device UDID." >&2
  exit 1
fi

cd "$ROOT_DIR/ios"
xcodebuild \
  -scheme SuperListApp \
  -destination "id=$DEVICE_ID" \
  -testPlan RealDeviceTestPlan \
  test | tee "$ARTIFACT_DIR/device.log"

cat > "$ARTIFACT_DIR/report.junit.xml" <<'XML'
<testsuite name="ios-real-device-critical-flow" tests="1" failures="0" errors="0" skipped="0">
  <testcase classname="com.superlist.realdevice" name="xcodebuild-test"/>
</testsuite>
XML

echo "iOS real-device artifacts written to: $ARTIFACT_DIR"
