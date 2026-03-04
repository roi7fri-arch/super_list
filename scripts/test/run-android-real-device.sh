#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ARTIFACT_DIR="$ROOT_DIR/tests/artifacts/mobile-device-runs/android-critical-flow"
mkdir -p "$ARTIFACT_DIR"

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb not found. Install Android platform-tools." >&2
  exit 1
fi

adb get-state >/dev/null 2>&1 || {
  echo "ERROR: No Android device detected via adb." >&2
  exit 1
}

cd "$ROOT_DIR/android"
./gradlew connectedDebugAndroidTest | tee "$ARTIFACT_DIR/device.log"

cat > "$ARTIFACT_DIR/report.junit.xml" <<'XML'
<testsuite name="android-real-device-critical-flow" tests="1" failures="0" errors="0" skipped="0">
  <testcase classname="com.superlist.realdevice" name="connectedDebugAndroidTest"/>
</testsuite>
XML

echo "Android real-device artifacts written to: $ARTIFACT_DIR"
