# Implementation Plan: Shared Hebrew Voice Shopping List (Mobile)

**Branch**: `001-shared-hebrew-voice-list` | **Date**: 2026-03-03 | **Spec**: /specs/001-shared-hebrew-voice-list/spec.md
**Input**: Feature specification from `/specs/001-shared-hebrew-voice-list/spec.md`

## Summary

Deliver a production-ready MVP for shared household shopping lists with Hebrew-first, RTL-first,
voice-led interaction on iOS and Android. The primary flow is hold-to-talk (press to start,
release to commit), parse Hebrew product+quantity, update shared list, and sync to household
members in near real-time. The plan uses server-authoritative conflict resolution
(timestamp last-write-wins; remove-wins-if-latest), offline action queue with reconnect replay,
and strict privacy constraints (no raw audio persistence).

## Technical Context

**Language/Version**: Swift 5.10+ (iOS), Kotlin 2.x (Android + KMP), TypeScript 5.x on Node.js 20 LTS (backend)

**Primary Dependencies**: SwiftUI, Jetpack Compose, Kotlin Multiplatform shared domain module, Fastify, WebSocket gateway, PostgreSQL 16, Redis, approved Hebrew STT SDK/API

**Storage**:
- Server: PostgreSQL (households, memberships, list projection, immutable event log)
- Server cache/realtime fan-out: Redis
- Client: local SQLite for list snapshot + pending action queue

**Testing**:
- Backend: Vitest/Jest + integration harness + contract schema tests
- iOS: XCTest + XCUITest
- Android: JUnit + Compose UI tests + Espresso
- Cross-platform E2E: device-pair household scenarios in CI/staging

**Target Platform**:
- iOS 17+
- Android 10+
- Backend Linux containers in managed cloud runtime

**Project Type**: Mobile apps + web-service backend with real-time sync

**Product Context**: Super List shared household grocery workflow

**Locale/Direction**: he-IL, RTL-first UI baseline

**Regulatory/Privacy**:
- Explicit microphone consent
- Data minimization
- Household-scoped data isolation
- No third-party data sale

**Allowed Data Sources**: Voice input, approved Hebrew STT output, user-created entries

**Forbidden Data Sources**: Unlicensed catalogs, long-term raw audio retention, unapproved sharing

**Performance Goals**:
- Press-release voice commit perceived as immediate
- Household sync near real-time for active members

**Constraints**:
- No raw audio retention beyond transcription processing
- One active household per user (MVP)
- Deterministic conflict policy (server timestamp LWW, remove-wins-if-latest)

**Scale/Scope**:
- MVP target: up to 50k MAU, 5k concurrent households peak
- Typical household size: 2-8 members

**SLO Targets**: Voice update p95 < 2.5s; sync p95 < 1.0s; availability >= 99.5%

**MVP Non-goals**: Barcode scanning, pricing intelligence, recipe suggestions,
advanced offline conflict-resolution UI

**Mandatory Household Sync Requirement**: The supermarket list MUST be synchronized in near-real-time
between all family members who installed the app and joined the same household.

## Constitution Check (Pre-Design)

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Code Quality Gate
  - Evidence plan: strict typecheck + lint in CI for mobile/backend; protected branch rules;
    mandatory reviewer approval.
- [x] Test Gate
  - Evidence plan: unit+integration+contract+e2e suite; regression test required for each bugfix;
    release flow coverage for voice add/sync/select-remove.
- [x] UX/Accessibility Gate
  - Evidence plan: he-IL RTL checklist, keyboard/focus semantics, Hebrew labels, contrast checks,
    state coverage for empty/loading/error.
- [x] Super List UX Baseline
  - Evidence plan: top-start persistent control verified in RTL (top-right), 48x48 minimum targets,
    haptic and screen-reader behavior tests.
- [x] Performance Gate
  - Evidence plan: stage-level latency instrumentation (capture→STT→parse→commit), API p95/p99,
    sync fanout p95/p99 dashboards.
- [x] Product SLO Gate
  - Evidence plan: SLO dashboard and alerting for voice p95, sync p95, availability.
- [x] Security & Privacy Gate
  - Evidence plan: authN/authZ test matrix, TLS enforcement checks, token/session lifecycle tests,
    data minimization and retention validation.
- [x] Consent/Data Policy Gate
  - Evidence plan: permission denial/revocation scenarios, raw-audio non-retention checks,
    prohibited-sharing controls.
- [x] Contract & Compatibility Gate
  - Evidence plan: OpenAPI and event-schema versioning policy; backward-compat contract tests;
    deprecation note template.
- [x] Observability & Operations Gate
  - Evidence plan: health/readiness/metrics endpoints, structured logs with correlation IDs,
    critical-flow alerts and runbook references.
- [x] Evidence Plan
  - Artifacts: CI reports, test dashboards, perf dashboards, security checklist, release checklist.
- [x] Release Criteria Gate
  - Core flow tests + RTL validation + quality checks defined as explicit go/no-go criteria.

## Architecture Decisions + Alternatives Rejected

### A1) Mobile architecture approach
- **Decision**: Native UI per platform (SwiftUI + Compose) with shared domain/sync logic via KMP.
- **Why**: Best RTL/accessibility fidelity and native permission UX; shared logic reduces divergence
  in parsing/conflict/queue rules.
- **Alternatives rejected**:
  1. Full cross-platform UI framework: faster initial delivery but higher risk for platform-specific
     microphone/RTL accessibility edge cases.
  2. Fully separate native codebases: doubles maintenance for critical sync/conflict logic.

### A2) Real-time sync strategy
- **Decision**: WebSocket household channel + idempotent REST writes + fallback polling + local action queue.
- **Why**: Low-latency fan-out with robust offline/reconnect behavior.
- **Alternatives rejected**:
  1. Polling-only sync: unlikely to sustain p95 < 1.0s without high cost.
  2. CRDT-first model: over-complex for MVP semantics already specified (LWW/remove-wins).

Fallback polling policy:
- Clients poll household list snapshot when realtime channel is unavailable/degraded.
- Polling remains active until WebSocket recovery.

### A3) Hebrew speech recognition approach
- **Decision**: Streaming STT with approved Hebrew provider, immediate transcript parsing,
  no raw-audio persistence.
- **Why**: Improves latency and accuracy while meeting privacy constraints.
- **Alternatives rejected**:
  1. On-device-only ASR: variable Hebrew quality across devices for MVP targets.
  2. Batch upload post-recording: worsens voice-to-update latency.

### A4) Conflict resolution approach
- **Decision**: Server-authoritative ordered events using server timestamps; LWW with deterministic
  tie-breaker; remove-wins-if-latest; append-only activity log.
- **Why**: Deterministic, auditable, matches approved clarification decisions.
- **Alternatives rejected**:
  1. Client-clock ordering: unsafe due to skew.
  2. Manual conflict UI in MVP: excluded by non-goals and adds complexity.

### A5) Visual branding and list readability approach
- **Decision**: Use supermarket stroller launcher icon and notebook-like yellow lined grocery list UI.
- **Why**: Faster user recognition of grocery context and improved scanability for line-by-line shopping entries.
- **Alternatives rejected**:
  1. Generic app icon: weaker grocery context signaling.
  2. Plain card list only: lower visual affordance for handwritten-list mental model.

## Project Structure

### Documentation (this feature)

```text
specs/001-shared-hebrew-voice-list/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── list-api.openapi.yaml
│   └── sync-events.schema.json
└── tasks.md
```

### Source Code (planned)

```text
api/
├── src/
│   ├── auth/
│   ├── households/
│   ├── list/
│   ├── sync/
│   ├── speech/
│   └── observability/
└── tests/
    ├── contract/
    ├── integration/
    └── unit/

ios/
├── App/
├── Features/
│   ├── VoiceEntry/
│   ├── SharedList/
│   └── Activity/
└── Tests/

android/
├── app/src/main/
├── feature/voice/
├── feature/list/
├── feature/activity/
└── app/src/test/ + app/src/androidTest/
```

**Structure Decision**: Mobile + API structure selected to match required iOS/Android clients,
real-time backend, and contract-testing separation.

## API/Contract Plan

Detailed artifacts in:
- /specs/001-shared-hebrew-voice-list/contracts/list-api.openapi.yaml
- /specs/001-shared-hebrew-voice-list/contracts/sync-events.schema.json

Planned contract surfaces:
1. Subscribe/get household list snapshot.
2. Add/update item from parsed speech.
3. Remove selected item(s).
4. Fetch activity log.
5. Real-time household event stream.
6. Sync replay endpoint for reconnect recovery (if client watermark is stale).

Versioning/backward compatibility:
- Start with `/v1` contracts.
- Additive changes are backward-compatible.
- Breaking changes require version bump and deprecation window documentation.

## Test Plan (Mapped to Requirements)

### Unit Tests
- Hebrew quantity parser (words/digits 1-99, default=1) → FR-009/010/011.
- Name normalization/duplicate merge → FR-013.
- Multi-item transcript separation (`וגם`, standalone `ו`, `ולחם`, `ושלוש`, punctuation/`ואז`)
  including regression phrases `שני חלב וגם לחם` and `שני לחם וגבינה אחת ושלוש מוצרלה` → FR-042/043/044.
- Conflict resolver (LWW/remove-wins) → FR-021/022.
- Offline queue replay/idempotency → FR-018/019.

### Integration Tests
- Speech transcript commit pipeline (release→parse→store→event publish) → FR-012/014.
- Household auth boundaries and role checks → FR-001..004, NFR-013.
- Reconnect sync + retry banner trigger semantics → FR-019/020.
- Multi-member propagation across two household devices for each mutation → FR-030.

### Contract Tests
- REST endpoints request/response schemas and backward-compat rules.
- WebSocket event envelope schema and ordering semantics.

### E2E Tests
- Two-device household sync: voice add from member A appears on member B under p95 budget.
- Select/remove flows with persistent top-start control at RTL top-right.
- Offline queue scenario with reconnect reconciliation and activity log trace.
- Permission denial/revocation and Hebrew guidance states.
- Fallback polling scenario: websocket unavailable, updates converge via polling on second device.
- Real-device sync scenario: connected Android+iOS devices as two household members.
- Clear-list scenario: one-press full-list clear transitions list to empty state and sync status updates.

### Regression Policy
- Every defect fix adds a failing test first and passing test after fix.

### RTL/NLP Special Coverage
- RTL layout snapshots for all core screens.
- Hebrew accessibility labels and screen-reader announcements.
- Hebrew parsing corpus for grocery terms and number phrases.

## Observability Plan

Metrics:
1. `voice_pipeline_latency_ms` (capture_start→list_commit), p50/p95/p99.
2. `stt_transcription_latency_ms` and `stt_confidence_score` distribution.
3. `sync_propagation_latency_ms` (server commit→other client render).
4. `sync_delivery_success_rate` (delivered household mutations / accepted mutations).
4. `pending_queue_depth` and `pending_queue_replay_failures_total`.
5. `voice_command_accuracy_rate` (validated sample-based metric).
6. Availability SLI from successful request and sync heartbeat signals.

Logging:
- Structured JSON logs with `correlation_id`, `household_id`, `user_id`, `action_id`,
  `event_id`, `client_platform`.
- No raw audio or sensitive free-form transcript content in logs.

Alerts:
- Voice p95 > 2.5s for 15m.
- Sync p95 > 1.0s for 15m.
- Availability burn rate threatening 99.5% monthly SLO.
- Queue replay failures above threshold over rolling 10m.
- Sync delivery success rate drop below SLO threshold over rolling 15m.

## Security/Privacy Checklist

- [x] Authenticated access required for every list/sync endpoint.
- [x] Household-based authorization enforced server-side per request/event.
- [x] Household-scoped authorization checks required on subscribe/get/list mutate/replay operations.
- [x] TLS required for all transport paths.
- [x] Secure token/session handling with expiry and revocation support.
- [x] Explicit microphone consent flow with denial/revocation handling.
- [x] Data minimization enforced in storage and logs.
- [x] Raw audio not retained after transcription.
- [x] Event retention limited to structured audit events.
- [x] No third-party data sale and no unapproved data sharing.

## Traceability Matrix (Requirements → Plan → Tests → Evidence)

| Requirement Group | Plan Element | Test Layer | Evidence Artifact |
|---|---|---|---|
| Voice hold-to-talk + parse (FR-005..012) | Voice pipeline + parser design | Unit + Integration + E2E | CI tests + latency dashboard |
| Shared sync (FR-014, FR-021..023) | Event model + WS strategy | Integration + Contract + E2E | Contract report + sync metrics |
| Household realtime sync (FR-029..032) | WS + fallback polling + replay strategy | Integration + E2E + Real-device | Sync SLA reports + device-run artifacts |
| Remove/select + persistent control (FR-015..017) | RTL UI architecture | UI tests + E2E | RTL checklist + accessibility report |
| Visual branding + notebook list (FR-033..034) | Android icon + notes-style list surface | Connected-device validation + UI checks | android-critical-flow report + media artifacts |
| Offline queue (FR-018..020) | Pending action queue | Unit + Integration + E2E | Queue replay metrics + test logs |
| Privacy/security (FR-024..028, NFR-011..014) | Security/privacy controls | Integration + security checks | Security checklist + audit logs |
| Deletion UX + transcript separation + clear list (FR-040..045) | Visible delete-approve action + parser boundary rules + one-press clear-all action | Unit + Connected-device tests | Android unit/instrumentation reports |

## Delivery Phases and Dependency Order

### Phase 0: Research (complete)
- Finalize architecture decisions and tradeoffs.
- Validate Hebrew parsing strategy and RTL accessibility baselines.
- Output: research.md.

### Phase 1: Design Artifacts (complete in this plan cycle)
- Data model and migration strategy.
- API and event contracts.
- Quickstart for local validation and evidence collection.
- Output: data-model.md, contracts/*, quickstart.md.

### Phase 2: Foundation Implementation
1. Auth + household membership boundaries.
2. Core data schema + event log.
3. API scaffolding + contract checks.
4. Real-time channel + observability baseline.
5. Household sync broadcast pipeline + fallback polling contract.

### Phase 3: Core User Flow (P1)
1. Hold-to-talk voice UX.
2. Hebrew quantity parsing + duplicate merge.
3. Shared sync and activity logging.

### Phase 4: Manage List (P2)
1. Select/remove UX and persistent top-start control.
2. Role-aware actions and guardrails.

### Phase 5: Reliability/Offline (P3)
1. Pending action queue + reconnect replay.
2. Retry banner and failure-handling states.
3. Concurrency semantics hardening and tests.
4. Sync replay endpoint and stale-watermark reconciliation.

### Phase 6: Hardening and Release
1. Performance tuning to SLO.
2. Security/privacy verification.
3. Release checklist and go/no-go review.

## Gate Timing and Checklist Policy

This plan separates implementation continuation gates from release (GA) gates.

### A) Implementation Continuation Gates (active during development)

Required for continuing implementation:

1. Story code/test tasks are completed per phase.
2. Contract/event artifacts exist and validate structurally.
3. Real-device command paths and artifact destinations are defined.
4. Platform/environment constraints are documented as blockers where execution is not currently possible.

### B) Release/GA Gates (active only before shipping)

Required only for final release decision:

1. Physical-device Android+iOS evidence attached (or approved platform exception).
2. SLO and sync propagation evidence from RC/prod-like runs attached.
3. Two-member sync run artifacts attached.
4. Final release readiness sign-offs complete.

Checklist handling policy:

- During development, GA-only items may remain in `PLANNED`/`BLOCKED` state and must not be treated
  as implementation-stop failures.
- In final hardening/release phase, GA items switch to strict pass/fail and become blocking.

### Rollback Strategy
- Backend: versioned API deploy with blue/green rollback and event consumer version pinning.
- Mobile: feature flags for voice pipeline and sync strategy toggles.
- Data: forward-only migrations with reversible operational fallback scripts for non-destructive changes.

## Constitution Check (Post-Design Re-evaluation)

- [x] Code Quality Gate: CI quality strategy and branch protections planned.
- [x] Test Gate: pyramid coverage + contract + integration + e2e + regression policy explicit.
- [x] UX/Accessibility Gate: Hebrew RTL, accessibility baseline, and state consistency covered.
- [x] Super List UX Baseline: persistent top-start control and hold-to-talk behavior testable.
- [x] Performance Gate: p95/p99 and measurement method defined.
- [x] Product SLO Gate: explicit thresholds and alerting mapped.
- [x] Security & Privacy Gate: authN/authZ, TLS, minimization, retention, secrets handling defined.
- [x] Consent/Data Policy Gate: microphone consent and prohibited data use controls included.
- [x] Contract & Compatibility Gate: versioning, backward compatibility, and deprecation path defined.
- [x] Observability & Operations Gate: metrics/logs/correlation IDs/alerts defined.
- [x] Evidence Plan: traceability matrix and artifact map present.
- [x] Release Criteria Gate: go/no-go checklist defined below.

## Release Readiness Checklist (Go/No-Go)

Go only if all are true:
1. Voice add flow passes end-to-end on iOS and Android in he-IL RTL.
2. Shared sync flow meets p95 < 1.0s in release-candidate environment.
3. Voice capture-to-list pipeline meets p95 < 2.5s in release-candidate environment.
4. Select/remove flow and persistent top-start control validated in RTL.
5. No raw audio persistence verified by storage/log audits.
6. Contract tests pass for REST and event schemas.
7. Security checklist passes (authN/authZ/TLS/session handling/consent).
8. Observability dashboards and alerts are active with on-call ownership.
9. Regression suite passes for all resolved defects in scope.
10. Two-member real-device sync test passes with evidence artifacts and sync SLA report.

## Household Synchronization Design Snapshot

Data model entities for synchronization:
- Household
- Membership
- SharedListItem (projection entity for shared list state)
- ListEvent (immutable event log)
- DeviceActionQueue (client-side queued mutation envelope)

Conflict policy:
- Server authoritative ordering.
- Timestamp-based last-write-wins.
- Remove precedence when latest mutation is remove.

Offline/reconnect policy:
- Client queues actions while offline.
- Reconnect replay with retry/backoff and idempotency key.
- Replay endpoint supports recovery from stale client watermark.

## Complexity Tracking

No constitution violations requiring exception.

## Implementation Status Snapshot (2026-03-03)

- Android connected-device run: PASS
  - Evidence: `tests/artifacts/mobile-device-runs/android-critical-flow/report.junit.xml`
- Android app install/launch on connected device: PASS
- iOS physical-device run: BLOCKED by host OS constraints (Linux environment)
