# iOS Real-Device Run Blockers

Status: BLOCKED (environment-dependent)

## Required prerequisites

1. macOS host with Xcode and Command Line Tools installed.
2. Signed Apple developer team/profile for `SuperListApp`.
3. Connected physical iOS device with trusted pairing.
4. `IOS_DEVICE_ID` exported with the device UDID.
5. Test plan available at `ios/Tests/RealDevice/RealDeviceTestPlan.xctestplan`.

## Current Linux workspace note

This repository is currently being worked from a Linux environment, so `xcodebuild`-based iOS physical-device execution cannot run here.

## Next step

Run `scripts/test/run-ios-real-device.sh` on a prepared macOS machine and replace this blocker file with actual report + logs.
