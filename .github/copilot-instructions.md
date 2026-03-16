# Copilot Instructions for super_list

## Big picture architecture
- Monorepo with 3 product surfaces (`android/`, `ios/`, `api/`) plus spec/governance sources (`specs/001-shared-hebrew-voice-list/`, `.specify/`).
- Runtime backend today is `api/server.js` (Express + in-memory household store). Most `api/src/**` files are contract/domain modules and placeholders not yet wired into the running server.
- End-to-end flow: Hebrew voice input on device → parse quantity/name → POST mutation with `clientActionId` → server appends event + updates snapshot → clients converge via polling/replay.
- Android is the most complete implementation (`android/app/src/main/java/com/superlist/SuperListApp.kt`), including speech parsing, household sync client, and local persistence.
- iOS `ios/Features/**` mirrors key UX/sync concepts (RTL layout, persistent delete control, retry/polling coordinators) but is mostly thin scaffolding.

## Critical developer workflows
- Start local sync stack from repo root: `./scripts/sync/start-sync-stack.sh` (boots API on `:8789`, health-checks `/health`, then starts Cloudflare tunnel).
- API-only loop: `cd api && npm start` (installs deps automatically when using `scripts/sync/start-api.sh`).
- Android fast loop (from `android/`):
	- `./gradlew :app:testDebugUnitTest`
	- `./gradlew :app:connectedDebugAndroidTest`
	- `./gradlew :app:installDebug`
	- `./gradlew :app:assembleRelease`
- Real-device evidence scripts live in `scripts/test/`; output goes to `tests/artifacts/mobile-device-runs/` (`device.log`, stub `report.junit.xml`, `critical-flow-summary.json`).
- `scripts/test/run-ios-real-device.sh` requires macOS + Xcode + `IOS_DEVICE_ID`; on Linux it should fail early by design.
- CI gates are path-scoped: `.github/workflows/api-quality.yml`, `android-quality.yml`, `ios-quality.yml`.

## Project-specific conventions
- Hebrew + RTL are baseline defaults across UI (`.environment(\.layoutDirection, .rightToLeft)` in iOS, Hebrew copy in Android Compose).
- Mutation semantics are strict: only `ADD_OR_MERGE` and `REMOVE` (keep enums/contracts/routes aligned).
- Idempotency is required for writes via `clientActionId` (see `api/src/list/list-event-repository.ts` and SQL unique key `(household_id, client_action_id)` in `api/src/db/migrations/002_list_events_items.sql`).
- Conflict rule is server-authoritative LWW; remove wins if latest (`api/src/list/shared-list-item-projection.ts`, `specs/.../data-model.md`).
- Raw audio must not be persisted beyond transcription (`api/src/speech/stt-adapter.ts`, `.specify/memory/constitution.md`).
- Android transcript parsing is centralized in `parseTranscriptItems()` / `parseHebrewTranscript()` with regression coverage in `android/app/src/test/java/com/superlist/TranscriptSeparationTest.kt` (e.g., separators `וגם`, prefixed `ו`, commas).

## Integration points to keep synchronized
- Contract sources of truth: `specs/001-shared-hebrew-voice-list/contracts/list-api.openapi.yaml` and `sync-events.schema.json`.
- If API behavior changes in runtime `api/server.js`, update contract modules/tests under `api/src/**` and `api/tests/**` to avoid drift.
- Android sync host is build-type specific (`BuildConfig.SYNC_SERVER_URL` in `android/app/build.gradle.kts`): debug → `dev-list...`, release → `list...`.
- `android/settings.gradle.kts` currently includes only `:app`; `android/feature/**` and `android/shared/**` are not active Gradle modules yet.
- Spec workflow scripts under `.specify/scripts/bash/` enforce branch/spec conventions; preserve strict shell flags and existing CLI behavior when editing scripts.