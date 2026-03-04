# Copilot Instructions for super_list

## Big picture architecture
- This repo combines product code + Spec-Kit workflow. Treat `android/`, `ios/`, and `api/` as implementation surfaces; treat `specs/001-shared-hebrew-voice-list/` and `.specify/` as delivery/governance sources.
- Core product flow is voice-first grocery mutation: capture Hebrew transcript → parse quantity/item → write immutable list event (idempotent by `clientActionId`) → update projection → broadcast household sync.
- Backend is currently mostly contract-first scaffolding (`api/src/**` pure functions/placeholders), while Android has the richest executable MVP logic in `android/app/src/main/java/com/superlist/SuperListApp.kt`.
- iOS mirrors key UX/sync concepts in feature-level SwiftUI components under `ios/Features/**` (RTL, persistent delete control, retry/sync coordinators).

## Critical developer workflows
- Android fast loop (from `android/`): `./gradlew :app:testDebugUnitTest`, `./gradlew :app:connectedDebugAndroidTest`, `./gradlew :app:installDebug`, `./gradlew :app:assembleRelease`.
- Real-device evidence scripts live in `scripts/test/` and write artifacts to `tests/artifacts/mobile-device-runs/` (JUnit XML stubs, logs, summary JSON, media README).
- iOS real-device script requires macOS + Xcode + `IOS_DEVICE_ID`; on Linux it is expected to fail early with environment checks.
- CI quality gates are path-scoped in `.github/workflows/*-quality.yml` (`api/**`, `android/**`, `ios/**`).
- Spec workflow remains active: `create-new-feature.sh`, `setup-plan.sh`, `check-prerequisites.sh`, `update-agent-context.sh` under `.specify/scripts/bash/`.

## Project-specific conventions and patterns
- Hebrew + RTL is default UX baseline (examples: `.environment(\.layoutDirection, .rightToLeft)` in iOS views; Hebrew labels across Android Compose screens).
- Keep action semantics consistent across clients/backend/contracts: `ADD_OR_MERGE` and `REMOVE` only.
- Conflict policy is server-authoritative LWW with remove-wins-if-latest (see `api/src/list/shared-list-item-projection.ts` and `specs/.../data-model.md`).
- Idempotency is mandatory for mutations (`clientActionId`, unique household+action semantics in `api/src/list/list-event-repository.ts` and SQL migrations).
- Privacy rule is explicit: no long-term raw-audio persistence (`api/src/speech/stt-adapter.ts`, constitution, spec docs).
- Android speech parsing logic is centralized in `parseTranscriptItems()` / `parseHebrewTranscript()` in `SuperListApp.kt`; tests assert Hebrew separators like `וגם`, prefixed `ו`, and punctuation splitting.

## Integration points to keep synchronized
- API contracts live in `specs/001-shared-hebrew-voice-list/contracts/list-api.openapi.yaml` and `sync-events.schema.json`; update these with `api/src/list/routes/**` and `api/src/sync/**` changes.
- Database assumptions are in `api/src/db/migrations/*.sql` (households, memberships, list_events, list_items); keep repository and projection logic aligned with schema constraints.
- `android/settings.gradle.kts` currently includes only `:app`; code in `android/feature/**` and `android/shared/**` documents intended modular structure but is not wired into Gradle modules yet.
- VS Code chat behavior/auto-approved script paths are configured in `.vscode/settings.json`.
- `update-agent-context.sh copilot` updates `.github/agents/copilot-instructions.md`; keep that file-path convention stable if adjusting automation.

## Spec-Kit guardrails that impact code changes
- Feature branch naming is enforced as `^[0-9]{3}-...`; spec directory resolution is numeric-prefix based (`.specify/scripts/bash/common.sh`).
- Preserve script dual-output contracts (`--json` and human-readable) and strict shell flags (`set -e`, often `set -u -o pipefail`).
- Governance and release evidence expectations come from `.specify/memory/constitution.md` (quality, privacy, contract compatibility, observability gates).