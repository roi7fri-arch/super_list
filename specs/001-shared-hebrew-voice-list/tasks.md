# Tasks: Shared Hebrew Voice Shopping List (Mobile)

**Input**: Design documents from /specs/001-shared-hebrew-voice-list/
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Mandatory per constitution. Includes unit, integration, contract, E2E, regression, and real-device execution.

**Organization**: Tasks grouped by user story for independent implementation and validation.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize project scaffolding, CI quality gates, and test artifact directories.

- [x] T001 Create mobile+API folder scaffolding in api/src/.gitkeep
- [x] T002 Create iOS feature module scaffolding in ios/Features/VoiceEntry/.gitkeep
- [x] T003 Create Android feature module scaffolding in android/feature/voice/.gitkeep
- [x] T004 [P] Configure backend lint/typecheck CI workflow in .github/workflows/api-quality.yml
- [x] T005 [P] Configure Android lint/typecheck CI workflow in .github/workflows/android-quality.yml
- [x] T006 [P] Configure iOS lint/typecheck CI workflow in .github/workflows/ios-quality.yml
- [x] T007 [P] Create mandatory test artifact directories in tests/artifacts/mobile-device-runs/.gitkeep
- [x] T008 [P] Add protected branch/review quality policy doc in docs/process/branch-protection.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build shared foundations needed by all user stories.

**⚠️ CRITICAL**: Complete before user story implementation.

- [x] T009 Implement household/membership schema migration in api/src/db/migrations/001_households_memberships.sql
- [x] T010 Implement list_events/list_items schema migration in api/src/db/migrations/002_list_events_items.sql
- [x] T011 [P] Implement auth middleware (token/session validation) in api/src/auth/auth-middleware.ts
- [x] T012 [P] Implement household authorization guard in api/src/auth/household-guard.ts
- [x] T013 [P] Implement WebSocket household channel bootstrap in api/src/sync/household-ws.ts
- [x] T014 Implement event persistence repository with idempotency key checks in api/src/list/list-event-repository.ts
- [x] T015 [P] Implement structured logging with correlation IDs in api/src/observability/logger.ts
- [x] T016 [P] Implement health/readiness/metrics endpoints in api/src/observability/health-routes.ts
- [x] T017 [P] Add STT adapter interface with raw-audio non-persistence contract in api/src/speech/stt-adapter.ts
- [x] T018 [P] Add OpenAPI schema validation test harness in api/tests/contract/openapi-contract.test.ts
- [x] T019 [P] Add sync-event JSON schema validation test harness in api/tests/contract/sync-events-schema.test.ts

**Checkpoint**: Foundation complete; user stories can proceed.

---

## Phase 3: User Story 1 - Voice Add and Shared Sync (Priority: P1) 🎯 MVP

**Goal**: Hold-to-talk voice flow adds Hebrew items with parsed quantities and syncs to household devices.

**Independent Test**: Owner on Device A adds via red button; Member on Device B sees merged item synced; accidental taps ignored.

### Tests for User Story 1 (MANDATORY)

- [x] T020 [P] [US1] Add unit tests for Hebrew quantity parser (words+digits 1-99, default=1) in api/tests/unit/hebrew-quantity-parser.test.ts
- [x] T021 [P] [US1] Add unit tests for product-name normalization in api/tests/unit/item-normalization.test.ts
- [x] T022 [P] [US1] Add unit tests for duplicate merge quantity increment logic in api/tests/unit/duplicate-merge.test.ts
- [x] T023 [P] [US1] Add contract test for POST /households/{id}/items:addByVoice in api/tests/contract/list-add-by-voice.contract.test.ts
- [x] T024 [P] [US1] Add integration test for release->parse->persist->sync pipeline in api/tests/integration/voice-add-sync.integration.test.ts
- [x] T025 [US1] Add mobile E2E test for red-button hold/release and synced add in tests/e2e/mobile/voice-add-shared-sync.e2e.ts

### Implementation for User Story 1

- [x] T026 [P] [US1] Implement Hebrew quantity parser service in api/src/speech/hebrew-quantity-parser.ts
- [x] T027 [P] [US1] Implement item normalization and merge key utility in api/src/list/item-normalizer.ts
- [x] T028 [US1] Implement voice transcript parse-to-action service in api/src/speech/voice-command-service.ts
- [x] T029 [US1] Implement add-by-voice API route in api/src/list/routes/add-by-voice.route.ts
- [x] T030 [US1] Implement household sync event publish after add/merge in api/src/sync/publish-list-event.ts
- [x] T031 [P] [US1] Implement iOS red hold-to-talk screen (Hebrew RTL) in ios/Features/VoiceEntry/VoiceEntryView.swift
- [x] T032 [P] [US1] Implement Android red hold-to-talk screen (Hebrew RTL) in android/feature/voice/src/main/java/com/superlist/voice/VoiceEntryScreen.kt
- [x] T033 [US1] Implement accidental-tap (<300ms) guard in shared logic module at android/shared/src/commonMain/kotlin/com/superlist/voice/HoldToTalkGuard.kt
- [x] T034 [US1] Implement microphone consent and revocation state handling in ios/Features/VoiceEntry/MicrophonePermissionCoordinator.swift
- [x] T035 [US1] Implement microphone consent and revocation state handling in android/feature/voice/src/main/java/com/superlist/voice/MicPermissionCoordinator.kt

**Checkpoint**: US1 fully functional and independently testable.

---

## Phase 4: User Story 2 - Select/Remove and Persistent RTL Control (Priority: P2)

**Goal**: Users can select/remove items and always see top-start persistent watch/delete control in Hebrew RTL UI.

**Independent Test**: Populate list, remove selected items from one device, confirm synced removal and persistent top-right (RTL) control.

### Tests for User Story 2 (MANDATORY)

- [x] T036 [P] [US2] Add unit tests for remove-wins projection updates in api/tests/unit/remove-wins-projection.test.ts
- [x] T037 [P] [US2] Add contract test for POST /households/{id}/items/{itemId}:remove in api/tests/contract/remove-item.contract.test.ts
- [x] T038 [P] [US2] Add integration test for item removal sync propagation in api/tests/integration/remove-sync.integration.test.ts
- [x] T039 [US2] Add E2E test for select/remove and persistent top-start control in tests/e2e/mobile/select-remove-persistent-control.e2e.ts
- [x] T040 [US2] Add accessibility+RTL E2E assertions for list states in tests/e2e/mobile/rtl-accessibility.e2e.ts

### Implementation for User Story 2

- [x] T041 [US2] Implement remove-item API route and event write in api/src/list/routes/remove-item.route.ts
- [x] T042 [US2] Implement list projection updater for latest remove semantics in api/src/list/list-projection-updater.ts
- [x] T043 [P] [US2] Implement iOS persistent watch/delete control at top-start (RTL top-right) in ios/Features/SharedList/PersistentDeleteControl.swift
- [x] T044 [P] [US2] Implement Android persistent watch/delete control at top-start (RTL top-right) in android/feature/list/src/main/java/com/superlist/list/PersistentDeleteControl.kt
- [x] T045 [US2] Implement iOS multi-select + remove interaction in ios/Features/SharedList/SharedListView.swift
- [x] T046 [US2] Implement Android multi-select + remove interaction in android/feature/list/src/main/java/com/superlist/list/SharedListScreen.kt

**Checkpoint**: US2 functional and independently testable.

---

## Phase 5: User Story 3 - Offline Queue and Concurrency Reliability (Priority: P3)

**Goal**: Offline add/remove actions replay on reconnect; concurrency follows server timestamp LWW and remove-wins-if-latest.

**Independent Test**: Execute offline actions, reconnect, verify replay and final state under concurrent edits with activity log integrity.

### Tests for User Story 3 (MANDATORY)

- [x] T047 [P] [US3] Add unit tests for conflict resolution policy (server timestamp LWW, remove-wins) in api/tests/unit/conflict-resolution-lww.test.ts
- [x] T048 [P] [US3] Add unit tests for client pending queue replay ordering/idempotency in android/shared/src/commonTest/kotlin/com/superlist/offline/PendingQueueReplayTest.kt
- [x] T049 [P] [US3] Add integration test for offline queue replay and retry banner trigger in api/tests/integration/offline-replay.integration.test.ts
- [x] T050 [P] [US3] Add integration test for concurrent edits and activity log ordering in api/tests/integration/concurrency-activity-log.integration.test.ts
- [x] T051 [US3] Add E2E test for offline add/remove then reconnect sync in tests/e2e/mobile/offline-reconnect-sync.e2e.ts

### Implementation for User Story 3

- [x] T052 [US3] Implement client pending action queue store in android/shared/src/commonMain/kotlin/com/superlist/offline/PendingActionQueueStore.kt
- [x] T053 [US3] Implement reconnect replay coordinator in android/shared/src/commonMain/kotlin/com/superlist/offline/ReconnectReplayCoordinator.kt
- [x] T054 [US3] Implement retry banner view model/state in ios/Features/SharedList/RetryBannerViewModel.swift
- [x] T055 [US3] Implement retry banner view model/state in android/feature/list/src/main/java/com/superlist/list/RetryBannerViewModel.kt
- [x] T056 [US3] Implement backend conflict resolver and deterministic tie-breaker in api/src/list/conflict-resolver.ts
- [x] T057 [US3] Implement activity log query route in api/src/list/routes/activity-log.route.ts

**Checkpoint**: US3 functional and independently testable.

---

## Phase 6: User Story 4 - Mandatory Household Synchronization (Priority: P1)

**Goal**: Ensure supermarket list is synchronized in near-real-time across all joined household members with backend source-of-truth guarantees.

**Independent Test**: Two household members on separate devices see synchronized list state for add/update/remove with websocket primary path and polling fallback.

### Tests for User Story 4 (MANDATORY)

- [x] T075 [P] [US4] Add unit tests for server-authoritative merge and timestamp ordering in api/tests/unit/household-sync-merge-ordering.test.ts
- [x] T076 [P] [US4] Add unit tests for remove-precedence conflict handling in api/tests/unit/remove-precedence-sync.test.ts
- [x] T077 [P] [US4] Add contract test for GET /households/{id}/list snapshot+subscribe handshake in api/tests/contract/household-list-subscribe.contract.test.ts
- [x] T078 [P] [US4] Add contract test for sync replay endpoint in api/tests/contract/sync-replay.contract.test.ts
- [x] T079 [P] [US4] Add integration test for multi-member mutation propagation in api/tests/integration/multi-member-sync.integration.test.ts
- [x] T080 [P] [US4] Add integration test for websocket failure fallback polling convergence in api/tests/integration/fallback-polling.integration.test.ts
- [x] T081 [US4] Add E2E two-member synchronized updates journey in tests/e2e/mobile/two-member-sync-journey.e2e.ts

### Implementation for User Story 4

- [x] T082 [US4] Implement SharedListItem projection model/service in api/src/list/shared-list-item-projection.ts
- [x] T083 [US4] Implement household list subscribe/get endpoint in api/src/list/routes/get-household-list.route.ts
- [x] T084 [US4] Implement sync replay endpoint for reconnect recovery in api/src/sync/routes/sync-replay.route.ts
- [x] T085 [US4] Implement mutation broadcast pipeline to all online household devices in api/src/sync/household-mutation-broadcast.ts
- [x] T086 [US4] Implement client realtime subscription and local state merge in android/shared/src/commonMain/kotlin/com/superlist/sync/RealtimeSyncCoordinator.kt
- [x] T087 [US4] Implement client fallback polling strategy when realtime channel is unavailable in android/shared/src/commonMain/kotlin/com/superlist/sync/FallbackPollingCoordinator.kt
- [x] T088 [US4] Implement iOS realtime subscription and local merge coordinator in ios/Features/SharedList/RealtimeSyncCoordinator.swift
- [x] T089 [US4] Implement iOS fallback polling coordinator in ios/Features/SharedList/FallbackPollingCoordinator.swift
- [x] T090 [US4] Enforce household-scoped authorization checks across list/sync/replay routes in api/src/auth/household-list-authorization.ts

**Checkpoint**: US4 synchronized household behavior is independently testable across two members.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, device execution, release evidence, and documentation.

- [x] T058 [P] Add regression-test policy doc for future bugfixes in tests/regression/README.md
- [x] T059 [P] Configure Android USB real-device test run profile in android/app/src/androidTest/realdevice/AndroidRealDeviceTestRunner.kt
- [x] T060 [P] Configure iOS physical-device XCTest/XCUITest scheme in ios/Tests/RealDevice/RealDeviceTestPlan.xctestplan
- [x] T061 Add local Android physical-device command script in scripts/test/run-android-real-device.sh
- [x] T062 Add local iOS physical-device command script in scripts/test/run-ios-real-device.sh
- [x] T063 Add unified local PC test command wrapper for critical flow in scripts/test/run-mobile-critical-flow-real-devices.sh
- [x] T064 [P] Add artifact collector script (machine-readable report + logs + media) in scripts/test/collect-mobile-artifacts.sh
- [x] T065 Execute Android connected real-device E2E critical flow and save report in tests/artifacts/mobile-device-runs/android-critical-flow/report.junit.xml
- [x] T066 Execute Android connected real-device E2E critical flow and save logs in tests/artifacts/mobile-device-runs/android-critical-flow/device.log
- [x] T067 Execute iOS connected real-device E2E critical flow and save report in tests/artifacts/mobile-device-runs/ios-critical-flow/report.junit.xml
- [x] T068 Execute iOS connected real-device E2E critical flow and save logs in tests/artifacts/mobile-device-runs/ios-critical-flow/device.log
- [x] T069 If iOS physical-device environment is unavailable, record blocker prerequisites in tests/artifacts/mobile-device-runs/ios-critical-flow/BLOCKED-prerequisites.md
- [x] T070 [P] Save pass/fail summary for mandatory critical flow in tests/artifacts/mobile-device-runs/critical-flow-summary.json
- [x] T071 [P] Save screenshots/video evidence where possible in tests/artifacts/mobile-device-runs/media/README.md
- [x] T072 Update quickstart with exact real-device prerequisites and commands in specs/001-shared-hebrew-voice-list/quickstart.md
- [x] T073 [P] Validate SLO dashboards and export evidence links in tests/artifacts/mobile-device-runs/slo-evidence.md
- [x] T074 Final release readiness checklist sign-off in specs/001-shared-hebrew-voice-list/checklists/release-readiness.md
- [x] T091 [P] Capture sync propagation SLA evidence (p95 and success rate) in tests/artifacts/mobile-device-runs/sync-propagation-sla.json
- [x] T092 Execute Android+iOS two-member real-device sync run and save machine-readable report in tests/artifacts/mobile-device-runs/two-member-sync/report.junit.xml
- [x] T093 Execute Android+iOS two-member real-device sync run and save logs in tests/artifacts/mobile-device-runs/two-member-sync/device-sync.log
- [x] T094 [P] Save household-sync pass/fail summary in tests/artifacts/mobile-device-runs/two-member-sync/summary.json
- [x] T095 [P] Implement Android supermarket-stroller launcher icon in android/app/src/main/res/drawable/ic_supermarket_stroller.xml and reference it in android/app/src/main/AndroidManifest.xml
- [x] T096 [P] Implement yellow lined notebook-style grocery list UI in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T097 Install updated Android debug app on connected device using android/gradlew :app:installDebug
- [x] T098 Re-run Android connected real-device validation after UI/icon changes and refresh evidence in tests/artifacts/mobile-device-runs/android-critical-flow/
- [x] T099 [P] Implement Android transcript quantity extraction + duplicate merge behavior in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T100 Reinstall Android debug app and re-run connected validation after transcript parsing update using android/gradlew :app:installDebug and :app:connectedDebugAndroidTest
- [x] T101 [P] Implement Android parsed voice-result preview (item+quantity) with confirm/cancel before commit in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T102 Reinstall Android debug app and re-run connected validation after preview-confirm UX update using android/gradlew :app:installDebug and :app:connectedDebugAndroidTest
- [x] T103 [P] Implement Android continuous dictation mode with pending batch and one-tap add-all commit in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T104 [P] Implement Android household share/join controls and visible family sync status on list screen in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T105 Reinstall Android debug app and re-run connected validation after continuous-mode and share/sync updates using android/gradlew :app:installDebug and :app:connectedDebugAndroidTest
- [x] T106 [P] Move approve-deletion action to always-visible list header area in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T107 [P] Make red voice circle toggle continuous dictation mode by default (start/stop) in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T108 Reinstall Android debug app and re-run connected validation after deletion-control and red-button-toggle updates using android/gradlew :app:installDebug and :app:connectedDebugAndroidTest
- [x] T109 [P] Change red voice circle interaction to press-and-hold continuous dictation (active while pressed, stop on release) in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T110 [P] Add multi-item transcript parsing for conjunction word "וגם" in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T111 Reinstall Android debug app and re-run connected validation after hold-continuous and "וגם" parsing updates using android/gradlew :app:installDebug and :app:connectedDebugAndroidTest
- [x] T112 [P] Improve Android item-separation parser for conjunction/punctuation boundaries ("וגם", "ואז", commas/semicolons) in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T113 Reinstall Android debug app and re-run connected validation after parser reliability fix using android/gradlew :app:installDebug and :app:connectedDebugAndroidTest
- [x] T114 [P] Add Android unit tests for transcript separation scenarios ("וגם", "ולחם", commas/"ואז") in android/app/src/test/java/com/superlist/TranscriptSeparationTest.kt
- [x] T115 Execute Android local unit tests for transcript separation using android/gradlew :app:testDebugUnitTest
- [x] T116 [P] Fix release-time batch commit path so multi-item transcripts are not collapsed into one item in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T117 [P] Add regression test for reported phrase "שני חלב וגם לחם" in android/app/src/test/java/com/superlist/TranscriptSeparationTest.kt
- [x] T118 [P] Add connected-device instrumentation parser test for reported phrase in android/app/src/androidTest/java/com/superlist/realdevice/TranscriptSeparationConnectedTest.kt
- [x] T119 Build and install Android release APK for family-device testing using android/gradlew :app:assembleRelease :app:installRelease
- [x] T120 Update quickstart with release distribution and family share/sync validation workflow in specs/001-shared-hebrew-voice-list/quickstart.md
- [x] T121 [P] Implement one-press clear-list action on Android list screen in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T122 Update spec/plan/quickstart for clear-list behavior and validation workflow in specs/001-shared-hebrew-voice-list/
- [x] T123 [P] Fix Android transcript separation for prefixed conjunction+quantity phrase "ושלוש" in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T124 [P] Add Android unit regression test for phrase "שני לחם וגבינה אחת ושלוש מוצרלה" in android/app/src/test/java/com/superlist/TranscriptSeparationTest.kt
- [x] T125 [P] Add connected-device instrumentation regression test for the same phrase in android/app/src/androidTest/java/com/superlist/realdevice/TranscriptSeparationConnectedTest.kt
- [x] T126 [P] Implement Android local list persistence across app restart in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T127 [P] Implement Android household server sync client (GET snapshot + POST mutations + polling refresh) in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T128 [P] Add Android network permissions for shared household sync in android/app/src/main/AndroidManifest.xml
- [x] T129 [P] Implement runnable local household sync API server in api/server.js and api/package.json
- [x] T130 Update quickstart with local sync server startup and Android server URL configuration in specs/001-shared-hebrew-voice-list/quickstart.md
- [x] T131 [P] Configure Cloudflare tunnel DNS route for list.friedman-makers.com to household sync tunnel
- [x] T132 [P] Configure cloudflared ingress to route list.friedman-makers.com to local API service
- [x] T133 [P] Add sync runtime helper scripts for API and tunnel in scripts/sync/start-api.sh and scripts/sync/start-cloudflared-tunnel.sh
- [x] T134 [P] Set Android default sync URL to public Cloudflare hostname in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T135 [P] Add one-command sync stack launcher script for API+tunnel in scripts/sync/start-sync-stack.sh
- [x] T136 [P] Remove editable sync URL field and keep Cloudflare sync endpoint hard-coded in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T137 [P] Configure Android build-type sync URLs (debug->dev-list, release->list) in android/app/build.gradle.kts and SuperListApp.kt
- [x] T138 [P] Configure Cloudflare DNS+tunnel ingress for dev-list.friedman-makers.com to local API
- [x] T139 [P] Add Android coupon OCR dependency and gallery import flow in android/app/build.gradle.kts and android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T140 [P] Implement Android coupon management screen with masked/unmasked number display in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T141 [P] Implement Android local persistence for coupon numbers, balance metadata, and lookup URL template in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T142 [P] Add manual coupon balance update workflow and configurable `{coupon}` balance URL launcher in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T143 Update spec/plan/data-model/quickstart/research for coupon OCR and balance support in specs/001-shared-hebrew-voice-list/
- [x] T144 Rebuild Android debug app after coupon feature implementation using android/gradlew :app:assembleDebug
- [x] T145 [P] Fix Android coupon-page back navigation to return directly to main voice screen in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T146 [P] Simplify Android coupon cards to show one always-visible coupon number field in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T147 [P] Add household coupon number/balance sync endpoints in api/server.js and Android sync client in android/app/src/main/java/com/superlist/SuperListApp.kt
- [x] T148 Update spec/plan/data-model/quickstart for always-visible shared household coupons in specs/001-shared-hebrew-voice-list/
- [x] T149 Update spec/quickstart for header-only coupon number presentation and one-device household sync fallback in specs/001-shared-hebrew-voice-list/
- [x] T150 Document successful one-device bidirectional coupon+list sync validation and Android debug fallback URLs in specs/001-shared-hebrew-voice-list/

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: starts immediately.
- **Foundational (Phase 2)**: depends on Setup; blocks all user stories.
- **User Stories (Phases 3-5)**: depend on Foundational completion.
- **User Stories (Phases 3-6)**: depend on Foundational completion.
- **Polish (Phase 7)**: depends on completion of desired user stories.

### User Story Dependencies

- **US1 (P1)**: starts after Phase 2; no dependency on US2/US3.
- **US2 (P2)**: starts after Phase 2; integrates with US1 list state but independently testable.
- **US3 (P3)**: starts after Phase 2; can proceed in parallel with US2 if staffing allows.
- **US4 (P1 sync)**: starts after Phase 2; depends on foundational sync/auth components and validates mandatory household synchronization.
- **Coupon utility flow**: can be implemented after Android app baseline is stable; depends only on local app persistence and image-processing support.

### Task-Level Dependency Highlights

- T026-T030 depend on T014 and T017.
- T031-T035 depend on T011-T013 and T026-T030.
- T041-T046 depend on T014 and T030.
- T052-T057 depend on T013-T016.
- T082-T085 depend on T013-T014 and T030.
- T086-T089 depend on T052-T053 and T082-T085.
- T090 depends on T012 and applies to T083-T085 routes.
- T065-T070 depend on T059-T064 and completion of T025, T039, T051.
- T092-T094 depend on T081, T086-T090 and connected real devices.
- T095-T098 depend on Android app bootstrap and connected-device path (T059-T066).
- T072 depends on T061-T063.
- T074 depends on T065-T073 and T091-T094.

---

## Parallel Opportunities Summary

- Setup parallel: T004-T008.
- Foundational parallel: T011-T013 and T015-T019.
- US1 parallel tests: T020-T024.
- US1 parallel mobile implementation: T031 and T032.
- US2 parallel UI controls: T043 and T044.
- US3 parallel tests: T047-T050.
- US4 parallel tests: T075-T080.
- Polish parallel evidence tasks: T064, T070, T071, T073.

---

## Independent Test Criteria per Story

- **US1**: Red hold-to-talk (>=300ms) adds parsed Hebrew item+quantity, duplicate merge works, sync visible to second household device.
- **US2**: Item selection/removal works, persistent watch/delete control remains at top-start (RTL top-right), removal syncs to second device.
- **US3**: Offline actions queue and replay on reconnect, retry banner appears on persistent failures, concurrent edits resolve by server timestamp LWW with remove-wins-if-latest.
- **US4**: Backend source-of-truth shared list synchronizes near-real-time mutations to all online household devices, with fallback polling and replay recovery.

---

## Requirement Traceability (Requirement -> Tasks -> Evidence)

- **FR-005/006/007/009/010/011/012/013** -> T020-T035 -> tests/artifacts/mobile-device-runs/critical-flow-summary.json
- **FR-014/021/022/023** -> T024, T030, T047, T050, T056, T057 -> tests/artifacts/mobile-device-runs/slo-evidence.md
- **FR-015/016/017 + NFR-005..010** -> T039-T046 -> tests/artifacts/mobile-device-runs/media/README.md
- **FR-018/019/020** -> T049, T051, T052-T055 -> tests/artifacts/mobile-device-runs/android-critical-flow/report.junit.xml
- **FR-024/025/027/028 + NFR-011..014** -> T017, T058, T073, T074 -> specs/001-shared-hebrew-voice-list/checklists/release-readiness.md
- **FR-029/030/031/032 + NFR-016/017** -> T075-T090, T091-T094 -> tests/artifacts/mobile-device-runs/two-member-sync/
- **FR-029/030/031/032 + NFR-016/017** -> T075-T090, T091-T094, T127-T130 -> api/server.js, android/app/src/main/java/com/superlist/SuperListApp.kt
- **FR-029/030/031/032 + NFR-016/017** -> T075-T090, T091-T094, T127-T136 -> api/server.js, scripts/sync/, android/app/src/main/java/com/superlist/SuperListApp.kt
- **FR-033/034 + NFR-018** -> T095-T098 -> tests/artifacts/mobile-device-runs/android-critical-flow/
- **FR-040/041/042/043/044** -> T106-T118, T123-T125 -> android/app/src/test/java/com/superlist/TranscriptSeparationTest.kt, android/app/src/androidTest/java/com/superlist/realdevice/TranscriptSeparationConnectedTest.kt
- **FR-045** -> T121-T122 -> android/app/src/main/java/com/superlist/SuperListApp.kt, specs/001-shared-hebrew-voice-list/quickstart.md
- **FR-046/047/048/049/050/051/052** -> T139-T144 -> android/app/src/main/java/com/superlist/SuperListApp.kt, android/app/build.gradle.kts, specs/001-shared-hebrew-voice-list/
- **GT-003 real-device requirement** -> T059-T070, T072 -> tests/artifacts/mobile-device-runs/

---

## Implementation Strategy

### MVP First (US1 only)
1. Complete Phase 1 and 2.
2. Complete US1 tests and implementation.
3. Validate Android real-device critical flow (T065-T066) before expanding scope.

### Incremental Delivery
1. Deliver US1 (voice add + sync).
2. Add US2 (select/remove + persistent RTL control).
3. Add US3 (offline + concurrency reliability).
4. Add US4 (mandatory household synchronization across family members).
5. Execute full real-device evidence and release sign-off.

### Multi-Developer Parallel Plan
1. Team A: Backend core + contracts (T009-T030, T041-T042, T056-T057).
2. Team B: iOS flows + real-device iOS execution (T031, T034, T043, T045, T054, T060, T062, T067-T069).
3. Team C: Android flows + real-device Android execution (T032, T035, T044, T046, T052-T053, T055, T059, T061, T065-T066).
4. QA/Release: cross-device E2E and artifact curation (T063-T064, T070-T074, T091-T094).

---

## Notes

- Every task follows checklist syntax with explicit file path.
- [P] indicates no blocking dependency on unfinished tasks in same phase.
- Real-device mobile execution is mandatory; emulator/simulator-only validation is insufficient.
- iOS real-device blocker task (T069) is only to document environmental prerequisite gaps; release remains blocked until requirement is satisfied.
