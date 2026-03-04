# Quickstart: Shared Hebrew Voice Shopping List (Mobile)

## Purpose

Validate MVP core flows and gate evidence for:
- Voice add (hold-to-talk)
- Shared sync across household members
- Select/remove
- Hebrew RTL UX + accessibility baseline
- Offline queue and reconnect replay

## Prerequisites

1. Two test accounts in the same household (Owner + Member).
2. Two devices/emulators:
   - iOS (he-IL locale, RTL enabled)
   - Android (he-IL locale, RTL enabled)
3. Backend and real-time sync service running in test/staging.
4. Approved Hebrew STT integration configured.
5. Metrics/log dashboards available.

## Local shared-sync server setup (current runnable path)

1. Install backend dependencies:
   - `cd api && npm install`
2. Start API + Cloudflare tunnel together:
   - `./scripts/sync/start-sync-stack.sh`
3. Ensure server route is active:
   - server binds to `0.0.0.0:8789`
   - Cloudflare Tunnel public hostname: `https://list.friedman-makers.com`
4. Android sync URL is hard-coded to `https://list.friedman-makers.com` (no per-device URL input required).
5. Use same household code on all family devices.
6. Keep this PC online while syncing (API + tunnel run on this host).

## Real-device command prerequisites (exact)

1. Android SDK platform-tools installed and `adb` available.
2. Connected Android device visible with `adb devices`.
3. macOS + Xcode for iOS physical-device execution.
4. Connected iOS device UDID exported:
   - `export IOS_DEVICE_ID=<device-udid>`
5. Scripts are executable:
   - `chmod +x scripts/test/run-android-real-device.sh`
   - `chmod +x scripts/test/run-ios-real-device.sh`
   - `chmod +x scripts/test/run-mobile-critical-flow-real-devices.sh`
   - `chmod +x scripts/test/collect-mobile-artifacts.sh`

## Real-device commands (exact)

1. Android-only critical flow:
   - `./scripts/test/run-android-real-device.sh`
2. iOS-only critical flow (run on macOS):
   - `./scripts/test/run-ios-real-device.sh`
3. Unified wrapper (runs Android + iOS + collection):
   - `./scripts/test/run-mobile-critical-flow-real-devices.sh`
4. Rebuild evidence summary only:
   - `./scripts/test/collect-mobile-artifacts.sh`

## Android release build for family distribution

1. Build installable release APK:
   - `cd android && ./gradlew :app:assembleRelease`
2. Release artifact path:
   - `android/app/build/outputs/apk/release/app-release.apk`
3. Optional checksum verification:
   - `sha256sum android/app/build/outputs/apk/release/app-release.apk`
4. Install release on connected device:
   - `cd android && ./gradlew :app:installRelease`

Expected outputs:
- `tests/artifacts/mobile-device-runs/android-critical-flow/report.junit.xml`
- `tests/artifacts/mobile-device-runs/android-critical-flow/device.log`
- `tests/artifacts/mobile-device-runs/ios-critical-flow/report.junit.xml`
- `tests/artifacts/mobile-device-runs/ios-critical-flow/device.log`
- `tests/artifacts/mobile-device-runs/critical-flow-summary.json`

## Scenario A: Voice add and sync

1. Open app on both devices and sign in.
2. Confirm main entry screen shows large red circular control.
3. On Device A, press and hold red control for >=300ms.
4. While still pressing, speak one or more Hebrew items in continuous dictation.
5. Release control to stop recognition and commit parsed items.
6. Verify on Device A:
   - Item appears with parsed quantity.
   - Hebrew labels and RTL list ordering are correct.
7. Verify on Device B:
   - Same item appears via sync within target latency.
8. Capture evidence:
   - Test output (E2E)
   - Voice pipeline latency metric
   - Sync latency metric

## Scenario G: Family sharing and synchronization validation

1. Install the same release APK on at least two family phones.
2. Open the app and navigate to list screen on each phone.
3. On phone A, read the household invite code shown in "שיתוף משפחתי".
4. On phone B, enter the same code and press "הצטרף למשפחה וסנכרן".
5. On phone A, add items by voice and by manual input.
6. On phone B, remove one selected item and approve deletion.
7. Verify sync status text updates after add/remove actions on both devices.
8. Close and reopen app on phone B; verify list restores from server snapshot.

Note: current runnable implementation uses a lightweight local API server with household source-of-truth + polling convergence in Android.

## Scenario B: Accidental tap filter

1. Tap red control for <300ms.
2. Verify no item is added.
3. Verify state remains consistent and Hebrew RTL UI unchanged.

## Scenario C: Multi-item separation regression (continuous dictation)

1. Press and hold the red control.
2. Speak: "שני חלב וגם לחם".
3. Release the red control.
4. Verify list result is two items, not one merged text item:
   - `חלב ×2`
   - `לחם ×1`
5. Optionally verify additional separators in one utterance:
   - "ביצים, גבינה, ואז מלפפונים"
   - "שני לחם וגבינה אחת ושלוש מוצרלה" → expected: `לחם ×2`, `גבינה ×1`, `מוצרלה ×3`

## Scenario D: Select/remove + persistent control

1. Confirm persistent watch/delete control is visible at top-start (top-right in RTL).
2. Select one or more items and remove.
3. Verify removal appears on second device via sync.
4. Verify Hebrew state messaging for success/empty list if applicable.

## Scenario E: One-press clear-list action

1. Add at least 3 items to the list.
2. Press "נקה רשימה" once.
3. Verify the list becomes empty immediately.
4. Verify sync status text updates accordingly when family sync is connected.

## Scenario F: Offline queue and reconnect

1. Disconnect Device A from network.
2. Add and remove items while offline.
3. Verify queued local state and retry banner (Hebrew) on persistent failures.
4. Reconnect network.
5. Verify queued actions replay and sync to Device B.
6. Verify activity log shows ordered events and remove-wins-if-latest behavior.

## Scenario G: Consent and privacy

## Automated parser verification commands (Android)

1. Local parser unit tests:
   - `cd android && ./gradlew :app:testDebugUnitTest`
2. Connected-device parser instrumentation test:
   - `cd android && ./gradlew :app:connectedDebugAndroidTest`

1. Revoke microphone permission on device.
2. Attempt hold-to-talk.
3. Verify Hebrew consent guidance and no capture.
4. Verify logs/storage contain no raw audio persistence artifacts.

## Accessibility and RTL checks

- Touch target minimum 48x48 for primary controls.
- Hebrew accessibility labels present.
- Visible focus indication for focusable controls.
- Screen-reader announcements for add/remove outcomes.
- Haptic feedback for hold start/stop and key outcomes.

## Go/No-Go Measurement Checklist

- Voice pipeline p95 < 2.5s.
- Sync latency p95 < 1.0s.
- Availability >= 99.5% (rolling period).
- Voice command accuracy >= 90% on validation corpus.
- Voice add task success >= 95%.
- Core flow tests pass (voice add, shared sync, select/remove).
- Hebrew RTL UX validation passes on all in-scope screens.

## Evidence Artifacts to Attach to Release Review

1. CI run links for unit/integration/contract/e2e suites.
2. Performance dashboard screenshots/export.
3. Security/privacy checklist sign-off.
4. RTL/accessibility validation report.
5. Incident-free canary run summary and rollback readiness note.
