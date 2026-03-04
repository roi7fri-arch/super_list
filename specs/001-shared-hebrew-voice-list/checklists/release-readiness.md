# Release Readiness Checklist: Shared Hebrew Voice Shopping List (Mobile)

Status: IN PROGRESS (Current GA decision: NO-GO)

Policy: This is a GA-phase checklist. During implementation, use planned/blocked markers.
Switch to strict `- [ ]` and `- [x]` only in final hardening/release phase.

## Mandatory quality gates

- [~] Core flow tests pass (US1-US4)
- [~] Contract and schema validation pass
- [~] RTL/accessibility verification complete
- [~] Security/privacy checks complete
- [~] SLO evidence attached (voice, sync, availability)
- [~] Android real-device artifacts attached
- [!] iOS real-device artifacts attached (or approved blocker)
- [~] Two-member sync real-device artifacts attached

## Evidence links

- CI runs: PLANNED
- Android artifacts: tests/artifacts/mobile-device-runs/android-critical-flow/report.junit.xml, tests/artifacts/mobile-device-runs/android-critical-flow/device.log, tests/artifacts/mobile-device-runs/android-critical-flow/BLOCKED-prerequisites.md
- iOS artifacts: tests/artifacts/mobile-device-runs/ios-critical-flow/report.junit.xml, tests/artifacts/mobile-device-runs/ios-critical-flow/device.log, tests/artifacts/mobile-device-runs/ios-critical-flow/BLOCKED-prerequisites.md
- Two-member sync artifacts: tests/artifacts/mobile-device-runs/two-member-sync/report.junit.xml, tests/artifacts/mobile-device-runs/two-member-sync/device-sync.log, tests/artifacts/mobile-device-runs/two-member-sync/BLOCKED-prerequisites.md
- SLO dashboard export: tests/artifacts/mobile-device-runs/slo-evidence.md
- Security/privacy sign-off: PLANNED

## Final sign-off

- Product Owner: [~]
- Engineering Lead: [~]
- QA Lead: [~]
- Release Manager: [~]

## Release decision log

- Date: 2026-03-03
- Decision: NO-GO for GA release
- Reason:
	- Android real-device run now passes on connected device, but GA still blocked by iOS and two-member cross-platform evidence.
	- iOS physical-device execution is blocked in current Linux environment.
	- Two-member Android+iOS real-device sync evidence is still blocked by iOS prerequisite gap.
- References:
	- tests/artifacts/mobile-device-runs/android-critical-flow/report.junit.xml
	- tests/artifacts/mobile-device-runs/ios-critical-flow/report.junit.xml
	- tests/artifacts/mobile-device-runs/two-member-sync/report.junit.xml
	- tests/artifacts/mobile-device-runs/slo-evidence.md

## Exit criteria to switch NO-GO -> GO

1. ~~Connect Android device and rerun `scripts/test/run-android-real-device.sh` with passing report.~~ ✅ completed on 2026-03-03.
2. Run iOS physical-device flow on macOS and attach passing report/log.
3. Execute two-member Android+iOS sync run with passing report/log/summary.
4. Replace planned sign-off markers with explicit approvals from Product, Engineering, QA, and Release.

## Local-PC implementation sign-off (this environment)

Status: COMPLETE (implementation phase)

- Scope: Linux-only development environment validation completed.
- Outcome: Implementation can continue; GA release remains blocked by external platform prerequisites.
- Approved path in this environment: keep iOS physical-device evidence as documented blocker until a supported runner is available.
