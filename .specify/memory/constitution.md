<!--
Sync Impact Report
- Version change: 1.0.0 -> 1.1.0
- Modified principles:
	- I. Policy as Enforceable Gates -> I. Product Mission Integrity
	- III. Quality by Default -> III. Quality, Privacy, and Trust by Default
	- IV. Compatibility Without Surprises -> IV. Compatibility Without Household Disruption
- Added sections:
	- Product Scope & Regulatory Guardrails
	- Service Level Objectives (SLOs)
	- MVP Non-goals
- Removed sections:
	- None
- Templates requiring updates:
	- ✅ .specify/templates/plan-template.md
	- ✅ .specify/templates/spec-template.md
	- ✅ .specify/templates/tasks-template.md
	- ⚠ pending: .specify/templates/commands/*.md (directory not present)
- Follow-up TODOs:
	- None
-->

# Super List Constitution

## Core Principles

### I. Product Mission Integrity
The product MUST optimize for fast shared household grocery capture, with voice-first workflows
for Hebrew-speaking families in Israel. All feature decisions MUST prioritize reducing forgotten
items and reducing manual typing in core grocery-list flows. Rationale: delivery focus must align
to measurable household value.

### II. Evidence Over Assertion
Pull requests MUST include evidence for each applicable gate: links to checks, reports,
dashboards, or test artifacts. Claims without evidence are non-compliant. Rationale: objective
evidence reduces ambiguity and review risk.

### III. Quality, Privacy, and Trust by Default
The default implementation path MUST satisfy type safety, testing depth, accessibility,
performance budgets, and security/privacy safeguards. Data collection MUST be minimized,
third-party data sale is prohibited, and raw voice audio retention beyond short-term processing
is prohibited. Any exception MUST be time-bound, approved, and tracked with owner and removal
date. Rationale: household trust is a product requirement, not an optional control.

### IV. Compatibility Without Household Disruption
Public contracts MUST evolve predictably with explicit versioning, compatibility guarantees,
deprecation windows, and migration notes. Rationale: consumers depend on stable behavior for
safe upgrades.

### V. Operability is a Feature
Services and critical flows MUST be observable, diagnosable, and supportable in production,
including health signals, structured logs, and actionable alerts. Rationale: unobservable systems
cannot be operated reliably.

## Product Scope & Regulatory Guardrails

- Program Name: Super List.
- Primary feature domain: shared Hebrew voice shopping list for mobile (iOS and Android) backed
	by real-time synchronized APIs.
- Geography and locale baseline: Hebrew (he-IL) with RTL-first UI behavior.
- Target segments: families in Israel sharing household grocery responsibilities.
- Regulatory/legal baseline:
	- explicit microphone permission consent is mandatory before capture,
	- secure household sharing is mandatory,
	- data minimization is mandatory,
	- third-party data sale is prohibited.
- Allowed data sources:
	- user voice input,
	- approved Hebrew speech-to-text service,
	- user-created product entries.
- Forbidden data sources/uses:
	- unlicensed external catalogs,
	- long-term raw audio retention,
	- unapproved data sharing.

## Service Level Objectives (SLOs)

- Voice capture-to-list update latency MUST meet p95 < 2.5s.
- Shared list sync latency MUST meet p95 < 1.0s.
- Service availability MUST meet at least 99.5%.
- SLO measurement windows and instrumentation methods MUST be documented in plans and release
	evidence.

## MVP Non-goals

The following are explicitly out of MVP scope and MUST NOT block release of core flows:

- barcode scanning,
- pricing intelligence,
- recipe suggestions,
- advanced offline conflict resolution.

## Enforceable Gates

### 1) Code Quality Gate
- All production code MUST pass strict type safety checks with no ignored type errors in
	changed files.
- Lint and typecheck MUST pass in CI for every pull request.
- Direct pushes to the default branch MUST be blocked; protected branches are mandatory.
- Every pull request MUST receive at least 1 approving review from a code owner or designated
	reviewer before merge.
- Mobile and backend shared contract changes MUST include generated type/client updates where
  applicable.

### 2) Test Gate
- Changes MUST follow a test pyramid strategy: unit tests as baseline, integration tests for
	cross-boundary behavior, and e2e tests for user-critical paths.
- Any API contract change MUST include updated/added contract tests.
- Workflow or multi-component changes MUST include integration tests.
- User-critical journeys MUST have e2e coverage that passes in CI.
- Every bugfix MUST add at least one regression test that fails before the fix and passes after.
- Core release flows MUST include passing automated coverage for: voice add, shared sync,
  and select/remove item.

### 3) UX/Accessibility Gate
- Locale formatting and text direction (LTR/RTL) MUST be consistent across changed surfaces.
- Hebrew (he-IL) and RTL-first behavior are mandatory defaults for MVP interfaces.
- Accessibility baseline is mandatory: full keyboard navigation, visible focus states,
	semantic labels, and WCAG-compliant contrast for key UI elements.
- Error, empty, and loading states MUST be present and consistent for all user-facing flows
	touched by the change.

### 4) Performance Gate
- Backend/API changes MUST define and meet p95 and p99 latency budgets for affected endpoints.
- Frontend changes MUST define and meet usability budgets for user-critical interactions
	(e.g., interaction readiness and visual stability).
- Voice capture-to-list update MUST meet p95 < 2.5s for release.
- Shared list synchronization MUST meet p95 < 1.0s for release.
- Capacity assumptions (expected throughput/concurrency/data size) MUST be documented.
- Performance measurement method (tooling, dataset/profile, environment) MUST be documented and
	reproducible.

### 5) Security & Privacy Gate
- Credential and session handling MUST use secure transport, secure storage, and safe expiration/
	rotation policies.
- Transport security MUST use TLS for all client-server and service-to-service communication.
- Authentication and household-based authorization are mandatory for shared list access.
- Access control MUST follow least-privilege principles for users, services, and infrastructure.
- Data collection MUST be minimized to required fields only; retention periods and deletion rules
	MUST be defined for stored personal/sensitive data.
- Microphone access MUST be gated by explicit user consent and revocation-aware behavior.
- Secrets MUST be stored in approved secret-management systems and MUST NOT be committed to source
	control or exposed in logs.
- Raw audio MUST NOT be retained long-term after transcription processing completes.
- Product and platform data MUST NOT be sold to third parties.

### 6) Contract & Compatibility Gate
- Public APIs/events/schemas MUST follow semantic versioning and declare compatibility impact.
- Backward compatibility is mandatory for non-major releases unless explicitly approved with
	migration documentation.
- Deprecations MUST include announcement date, support window, and removal target date.
- Mobile/client consumers MUST be supported with compatibility guarantees for at least one stable
	previous client version, unless a stricter policy is documented per product.
- Real-time sync protocol changes MUST include compatibility tests across iOS, Android, and backend
  versions in active support windows.

### 7) Observability & Operations Gate
- All deployable services MUST expose health, readiness, and metrics endpoints (or equivalent
	platform-native checks).
- Logs MUST be structured and include correlation IDs for request/transaction tracing.
- Critical user flows MUST have alert conditions with defined thresholds and on-call ownership.
- Runbooks or operational notes MUST be updated when alert logic, dependencies, or failure modes
	change.
- Alerts MUST include at minimum: SLO breach risk for voice add latency, sync latency,
  and availability.

## Delivery Workflow & Compliance Evidence

1. Plan stage MUST map requirements to applicable gates and define verification strategy.
2. Implementation stage MUST keep CI checks green for lint, typecheck, tests, and security
	 scanning relevant to the change.
3. Review stage MUST include a gate checklist with evidence links.
4. Release stage MUST confirm observability, alerting, and rollback readiness for critical changes.
5. Post-incident bugfixes MUST include regression tests and documented root-cause learnings.
6. Release readiness MUST verify core flows (voice add, shared sync, select/remove), Hebrew RTL UX,
   and all required quality checks.

## Governance

This constitution supersedes conflicting local conventions for engineering delivery.

### Amendment Policy
- Constitution changes MUST be submitted via pull request and approved by maintainers.
- Every amendment MUST include: reason, impact, and migration notes.
- Semantic versioning applies to this constitution:
	- MAJOR: incompatible governance or gate removals/redefinitions.
	- MINOR: new gate/section or materially expanded mandatory guidance.
	- PATCH: clarifications, wording improvements, and non-semantic refinements.

### Amendment Template
- Reason:
- Impact:
- Migration Notes:

### Compliance Review Expectations
- Every feature plan, specification, and task set MUST explicitly reflect active gates.
- Reviewers MUST block merges when gate evidence is missing or contradictory.
- Exceptions MUST cite approver, expiry date, and compensating controls.

### Amendment Record
- 2026-03-02 | v1.0.0 | Initial ratification with enforceable gates and governance.
- 2026-03-03 | v1.1.0 | Added Super List mobile/he-IL context, SLOs, privacy constraints,
  and release-critical flow gates.

**Version**: 1.1.0 | **Ratified**: 2026-03-02 | **Last Amended**: 2026-03-03
