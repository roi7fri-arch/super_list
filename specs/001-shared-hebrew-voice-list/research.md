# Phase 0 Research: Shared Hebrew Voice Shopping List (Mobile)

## Decision 1: Mobile architecture

**Decision**: Native UI per platform (SwiftUI for iOS, Jetpack Compose for Android) with a shared Kotlin Multiplatform domain module for parsing, queueing, and sync rules.

**Rationale**: Native UI provides the strongest RTL, accessibility, microphone-permission, and haptic behavior. Shared domain logic prevents drift in conflict resolution and offline replay behavior.

**Alternatives considered**:
- Full cross-platform UI framework: rejected for higher risk in nuanced native permission/accessibility behavior.
- Separate native-only business logic: rejected due to duplicated conflict/offline logic and higher defect risk.

---

## Decision 2: Real-time sync strategy

**Decision**: Use idempotent REST for writes and household-scoped WebSocket events for fan-out, backed by local pending action queue on device.

**Rationale**: REST gives deterministic write semantics; WebSockets satisfy near real-time sync; local queue preserves user actions during connectivity loss.

**Alternatives considered**:
- Polling-only sync: rejected due to latency/cost tradeoff and difficulty meeting sync p95 target.
- Full CRDT model: rejected as unnecessary complexity for approved LWW/remove-wins policy.

---

## Decision 3: Hebrew speech recognition and parsing

**Decision**: Streaming transcription with approved Hebrew STT provider; parse transcript on release into item update event; support quantity words + digits 1-99; default quantity to 1 when omitted.

**Rationale**: Streaming lowers end-to-end latency and improves interactive UX. Bounded quantity grammar improves precision and testability.

**Alternatives considered**:
- On-device-only STT: rejected for variable quality across devices in MVP.
- Batch transcription on stop: rejected due to higher latency.

---

## Decision 4: Conflict resolution semantics

**Decision**: Server-authoritative ordering via server timestamp with deterministic tie-break key; apply last-write-wins; if latest is remove, removal wins; persist append-only activity log.

**Rationale**: Aligns exactly with approved product policy and gives transparent auditability.

**Alternatives considered**:
- Client timestamp ordering: rejected due to clock skew risk.
- Manual conflict resolution UI: rejected as explicit MVP non-goal.

---

## Decision 5: Data storage model

**Decision**:
- Backend primary store: PostgreSQL for households, memberships, item projections, and event log.
- Realtime fan-out and ephemeral presence: Redis.
- Mobile local state + pending queue: SQLite.

**Rationale**: Relational integrity is required for authorization boundaries and event consistency. Local durable queue supports offline guarantees.

**Alternatives considered**:
- NoSQL-only backend: rejected for weaker transactional ergonomics in membership+event consistency.
- In-memory queue on client: rejected due to data-loss risk.

---

## Decision 6: Duplicate normalization strategy

**Decision**: Normalize item names (Unicode normalization, trim, case fold, punctuation/spacing normalization) and merge when normalized key matches.

**Rationale**: Meets approved duplicate behavior while avoiding heavy linguistic inference.

**Alternatives considered**:
- Aggressive stemming/fuzzy auto-merge: rejected due to false positive risk.

---

## Decision 7: Offline and retry behavior

**Decision**: Queue add/remove actions locally when offline; replay on reconnect in enqueue order with idempotency key; show Hebrew retry banner on persistent failures.

**Rationale**: Meets spec reliability and user feedback requirements.

**Alternatives considered**:
- Blocking edits while offline: rejected as poor UX and contrary to approved default.

---

## Decision 8: Privacy and compliance handling

**Decision**: Do not persist raw audio after transcription completion; retain only structured item-update events for limited audit period; enforce explicit microphone consent and revocation-aware behavior.

**Rationale**: Aligns with legal/privacy constraints and constitution gates.

**Alternatives considered**:
- Raw audio retention for QA: rejected due to policy prohibition.

---

## Decision 9: Test strategy

**Decision**: Test pyramid with explicit contract and E2E coverage for critical flows:
- Unit: parser, normalization, conflict logic, queue replay.
- Integration: auth/household boundaries, write+sync pipeline, reconnect replay.
- Contract: REST + event schema compatibility.
- E2E: iOS/Android core flows in he-IL RTL.

**Rationale**: Balances fast feedback and high confidence for cross-client sync behavior.

**Alternatives considered**:
- E2E-heavy only: rejected due to execution cost/flakiness.
- Unit-only: rejected due to inadequate distributed-system confidence.

---

## Decision 10: SLO measurement strategy

**Decision**: Measure and report:
- Voice pipeline latency (press to committed list update),
- Sync propagation latency (server commit to other client render),
- Availability SLI from successful request and heartbeat checks.

**Rationale**: Directly maps to SC-002/SC-003/SC-004 and release gate criteria.

**Alternatives considered**:
- Aggregate endpoint-only latency: rejected because it misses client-perceived voice pipeline and cross-device sync timing.

---

## Decision 11: Coupon OCR and balance support

**Decision**: Use on-device OCR for coupon image import, store only structured coupon metadata locally, and support a configurable balance-check URL template until a dedicated backend lookup/parser is available.

**Rationale**: Coupon numbers are sensitive shopping credentials but do not require long-term image retention. On-device OCR avoids unnecessary uploads, while URL-template configuration keeps the balance flow flexible across supermarket website changes.

**Alternatives considered**:
- Hard-coded supermarket parsing directly in the app: rejected as fragile and difficult to maintain.
- Persisting raw coupon images indefinitely: rejected because structured coupon number and balance metadata are sufficient for MVP.

---

## Clarification Resolution Status

All technical context unknowns for MVP planning are resolved from approved defaults and research. No open NEEDS CLARIFICATION items remain.

---

## Regression Log

### 2026-03-04 — Multi-item separation with prefixed conjunction+quantity token

- **Reported symptom**: Utterance `שני לחם וגבינה אחת ושלוש מוצרלה` produced `לחם ×2`, but merged the remaining words into one item instead of two separate items.
- **Root cause**: In Android transcript splitting (`parseTranscriptItems`), prefixed connector tokens such as `ושלוש` were not consistently treated as item-boundary starters when they should begin a new item phrase.
- **Fix implemented**: Updated connector handling in `android/app/src/main/java/com/superlist/SuperListApp.kt` to split on prefixed `ו` tokens by default, while preserving numeric-continuation behavior for number compounds (for example `עשרים ושלוש`).
- **Regression prevention tests**:
	- Unit: `separates_items_with_prefixed_vav_and_following_quantity_phrase()` in `android/app/src/test/java/com/superlist/TranscriptSeparationTest.kt`.
	- Connected device: `separates_prefixed_vav_with_quantity_phrase_on_connected_device()` in `android/app/src/androidTest/java/com/superlist/realdevice/TranscriptSeparationConnectedTest.kt`.
- **Validation evidence**: Android local + connected tests passed and updated debug/release APKs were installed on the connected device.

### 2026-03-04 — Family list not shared across devices after app restart

- **Reported symptom**: After adding items and reopening app, list state was not converging across family devices; one device could show local-only state while another showed different state.
- **Root cause**: Android app state was local-memory only and lacked a runnable household source-of-truth backend integration path.
- **Fix implemented**:
	- Added Android local persistence across restarts (SharedPreferences JSON cache).
	- Added Android sync client for household snapshot fetch + mutation post + polling refresh loop.
	- Switched Android sync endpoint to fixed project URL (`https://list.friedman-makers.com`).
	- Added runnable local household sync API server (`api/server.js`) with idempotent mutation handling and household list snapshot endpoint.
- **Operational note**: API + tunnel run on local PC and must stay online for continuous synchronization.

### 2026-03-04 — Internet access for family sync via custom domain

- **Goal**: Allow household list synchronization outside local network boundaries.
- **Implementation**:
	- Cloudflare DNS route configured: `list.friedman-makers.com` -> existing named tunnel.
	- Tunnel ingress mapped to local API service (`http://127.0.0.1:8789`).
	- Android default sync URL switched to `https://list.friedman-makers.com`.
	- Runtime helper scripts added under `scripts/sync/`, including one-command stack startup (`start-sync-stack.sh`).
- **Constraint**: Because API runs on local PC, PC and tunnel process must remain online for continuous family sync.

### 2026-03-16 — One-device sync validation required debug fallback paths

- **Reported symptom**: During one-phone sync validation, the Android debug build did not pull remote coupon/list updates even though the local API and Cloudflare tunnel were healthy.
- **Root cause**:
	- The phone was joined to household `SL-890248`, while initial manual API tests used `890248` without the app prefix.
	- On the tested phone, DNS lookups for `dev-list.friedman-makers.com` failed intermittently in the app process, preventing the normal debug sync endpoint from resolving.
	- `adb reverse` was present but localhost connectivity from the app sandbox to `127.0.0.1:8789` was not reliable on that device.
- **Fix implemented**:
	- Added Android debug sync fallback attempts in `android/app/src/main/java/com/superlist/SuperListApp.kt` for `http://127.0.0.1:8789` and the validated workstation LAN address.
	- Validated coupon sync both directions with coupon `11447830316028` and list sync both directions with items `בננות ×3` and `חלב ×5`.
- **Operational note**: For future one-device validations, always copy the exact in-app household code and prefer direct LAN fallback if the public debug hostname does not resolve on-device.
