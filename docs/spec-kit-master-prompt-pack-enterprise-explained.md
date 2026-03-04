# Spec-Kit Master Prompt Pack (Enterprise) — Explained Version

This is a beginner-friendly version of the enterprise pack.
It keeps the same structure, but explains each term in plain language.

---

## How to use this file

1. Copy one section at a time.
2. Replace placeholders like `[FEATURE]` with your real values.
3. Run in this order:
   - `/speckit.constitution`
   - `/speckit.specify`
   - `/speckit.plan`
   - `/speckit.tasks` (optional but recommended)
4. If the AI is unsure, it should output `NEEDS CLARIFICATION` instead of guessing.

---

## Quick glossary (simple definitions)

- **Constitution**: project rules that all future work must follow.
- **Spec**: what you want to build (requirements, behavior, edge cases).
- **Plan**: how to build it (architecture, phases, testing strategy).
- **Tasks**: concrete implementation checklist with file paths.
- **Contract test**: checks API request/response shapes stay correct.
- **Integration test**: checks multiple modules work together.
- **E2E test**: simulates real user flow in the UI.
- **Regression test**: test added to prevent an old bug from returning.
- **SLO**: target service reliability/performance (for example response time).
- **p95 latency**: 95% of requests finish faster than this value.
- **Observability**: ability to understand runtime behavior via logs/metrics/health.
- **Backward compatibility**: new version does not break existing clients.
- **Deprecation**: controlled phase-out process for old API behavior.
- **ACL / authorization**: who is allowed to do what.

---

## A) Initialization Input (single source of truth)

> Fill this first. This gives the AI clear business and technical boundaries.

```text
Program Name: [PROGRAM]
Feature Name: [FEATURE]
Business Objective: [OBJECTIVE]
Target Segments: [SEGMENTS]
Geography/Locale: [LOCALE]
Regulatory/Legal: [GDPR/PII/LICENSING/etc]
SLOs: [LATENCY/AVAILABILITY]
Security Model: [AUTHN/AUTHZ/SECRETS]
Architecture Baseline: [MONOLITH/MICROSERVICE + CONTRACT STRATEGY]
Data Sources Allowed: [ALLOWED_SOURCES]
Data Sources Forbidden: [FORBIDDEN_SOURCES]
Real-Device Test Requirement: [YES/NO + WHICH DEVICES + WHAT FLOWS MUST PASS ON CONNECTED DEVICE]
MVP Non-goals: [NON_GOALS]
Release Criteria: [QUALITY_GATES + ACCEPTANCE + OPS_READINESS]
```

### Clarification for each line

- **Business Objective**: one sentence about success value (example: “increase recommendation click-through”).
- **Target Segments**: who uses this (example: students, parents, admin staff).
- **Regulatory/Legal**: privacy and licensing requirements.
- **SLOs**: measurable service targets (example: p95 < 300ms).
- **Security Model**:
  - `AUTHN` = authentication (who are you?)
  - `AUTHZ` = authorization (what can you do?)
- **Architecture Baseline**:
  - `Monolith` = one deployable app.
  - `Microservice` = multiple services.
- **Real-Device Test Requirement**:
  - Use this when your feature includes mobile apps.
  - Define whether tests MUST run on a physical phone/tablet connected to your development PC (USB or equivalent), not only emulator/simulator.
  - Include minimum required flows (example: login, voice add, list sync, delete).
- **Release Criteria**: minimum quality to ship.

---

## B) Prompt for /speckit.constitution (Explained)

Paste this:

```text
Create/update the project constitution with enforceable gates and governance.

Required gate sections:
1) Code Quality Gate
- strict type safety, lint/typecheck, protected branches, review requirements.

2) Test Gate
- mandatory test pyramid policy,
- contract tests for API changes,
- integration tests for workflows,
- e2e for user-critical paths,
- regression test for every bugfix.

3) UX/Accessibility Gate
- locale + direction consistency,
- accessibility baseline (keyboard, focus, semantic labels, contrast),
- consistent error and empty/loading states.

4) Performance Gate
- endpoint p95/p99 budgets,
- frontend usability budget,
- capacity assumptions and measurement method.

5) Security & Privacy Gate
- credential/session handling policy,
- least privilege,
- data minimization and retention,
- secrets management expectations.

6) Contract & Compatibility Gate
- versioning rules,
- backward compatibility policy,
- deprecation lifecycle,
- mobile/client compatibility obligations.

7) Observability & Operations Gate
- health/ready/metrics requirements,
- structured logging and correlation IDs,
- alert conditions for critical flows.

Governance:
- semantic versioning for constitution,
- amendment template (reason, impact, migration notes),
- dated ratification/amendment records in ISO format.
```

### What this section does

- Prevents random quality drops later.
- Forces “definition of done” at project level.
- Makes future AI outputs more aligned with your standards.

---

## C) Prompt for /speckit.specify (Explained)

Paste this:

```text
Produce a production-grade feature specification for [FEATURE].

Mandatory structure:
1. Problem statement and user value.
2. In-scope / out-of-scope (explicit non-goals).
3. Prioritized user stories (P1/P2/P3), each independently testable.
4. Detailed acceptance scenarios (Given/When/Then).
5. Edge case catalog (failure, retries, timeout, stale session, malformed input, partial provider outage).
6. Functional requirements FR-001..N (atomic, testable).
7. Non-functional requirements NFR (performance, accessibility, reliability, security, localization).
8. Data and compliance constraints (source licensing, PII handling, retention).
9. Roles and permissions matrix.
10. Contract impact notes (API/schema changes).
11. Success criteria SC-001..N (measurable).
12. Assumptions + dependencies.
13. NEEDS CLARIFICATION section for unresolved items.

Hard rules:
- Do not infer business policy when unclear; emit NEEDS CLARIFICATION.
- Every requirement must be testable.
- Every user-facing behavior must define expected copy/state.
```

### What this section does

- Converts your idea into precise behavior.
- Reduces rework because acceptance criteria are explicit.
- Protects you from vague implementation decisions.

---

## D) Prompt for /speckit.plan (Explained)

Paste this:

```text
Generate an implementation plan tightly aligned to the approved spec.

Plan must include:
1. Technical context (languages, dependencies, storage, platform, constraints, scale).
2. Constitution gate check before and after design.
3. Architecture decisions with rationale and alternatives rejected.
4. Data model and migration strategy.
5. API contract plan (versioning, compatibility, changelog path).
6. Test plan mapped to requirements (unit/contract/integration/e2e/perf).
  - For mobile features, include physical-device test execution requirements (connected to developer/CI host), not emulator-only evidence.
7. Observability plan (metrics/logging/tracing/alerts).
8. Security checklist for the feature.
9. Delivery phases with dependencies and rollback strategy.
10. Release readiness checklist.
11. Gate timing policy that separates:
  - Implementation-Complete gates (required to keep coding), and
  - Release/GA gates (required only before ship).

Task generation requirements:
- include exact file paths,
- include test tasks first for risky behavior,
- include regression tasks for all bug-risk areas,
- include docs/ops updates,
- preserve traceability FR -> task -> test.

If any item is ambiguous, create a blocked item with NEEDS CLARIFICATION.
```

### Mandatory gate timing rule (prevents false blocking)

When generating checklists and gates for mobile/shared features:

1. Keep implementation progress gates and release gates separate.
2. During implementation, do **not** use open `- [ ]` release-only items in files that are used as implementation blockers.
3. For release-only evidence that is not yet runnable (for example iOS real-device execution from Linux), mark as `BLOCKED` with prerequisites and owner, not as failed implementation gate.
4. Convert release gate items to actionable `- [ ]` checklist boxes only in the dedicated release phase.
5. Spec and plan must explicitly define milestone boundaries:
  - Milestone A: Implementation Complete
  - Milestone B: Release Ready (GA)

Suggested status markers before release phase:
- `- [~]` Planned for GA
- `- [!]` Blocked with prerequisites
- `- [x]` Completed

Use `- [ ]` only for currently enforceable gates in the active phase.

### What this section does

- Turns requirements into safe implementation steps.
- Adds rollout safety (`rollback strategy`) before coding.
- Ensures every requirement is linked to tests and tasks.

---

## E) Prompt for /speckit.tasks (Explained, optional but recommended)

Paste this:

```text
Create tasks with strict traceability and implementation safety:
- group by user story,
- mark parallelizable tasks [P],
- identify blockers,
- include exact file paths,
- add validation commands for each phase,
- include explicit rollback or mitigation tasks for high-risk changes,
- include documentation updates for public behavior changes.
```

### What this section does

- Gives you execution visibility.
- Makes it easier to delegate work.
- Prevents forgetting tests/docs for new behavior.

---

## F) Pre-implementation audit add-on

Paste this after tasks:

```text
Run a coverage matrix and report gaps:
- columns: Intent, Spec, Plan, Tasks, Tests, Docs, Ops
- status values: OK / Needs Update / Missing
- include evidence links to modified files/sections.
```

### Why this is valuable

This catches misalignment before implementation starts.

---

## G) Copy-ready execution order

1. Fill Section A.
2. Run Section B with `/speckit.constitution`.
3. Run Section C with `/speckit.specify`.
4. Run Section D with `/speckit.plan`.
5. Run Section E with `/speckit.tasks`.
6. Run Section F (audit).
7. Start implementation.

---

## H) Minimal example values (for quick start)

```text
Program Name: Hebrew Book Recommender
Feature Name: Personalized Recommendations + Hebrew Discovery
Business Objective: Increase successful book discovery and recommendation engagement
Target Segments: Hebrew-reading users and admin moderators
Geography/Locale: Hebrew (RTL), Israel
Regulatory/Legal: privacy-safe user data handling, only licensed/free metadata sources
SLOs: books/recommendations p95 < 300ms
Security Model: JWT auth, role-based admin access
Architecture Baseline: web + api monorepo, versioned REST contract
Data Sources Allowed: Google Books, Open Library, Wikidata, Internet Archive, LoC
Data Sources Forbidden: unlicensed copyrighted image feeds
Real-Device Test Requirement: N/A (web-only project)
MVP Non-goals: guest recommendations, paid provider integrations
Release Criteria: lint/typecheck pass, required tests pass, acceptance checklist complete
```

---

## I) Common mistakes to avoid

- Asking for “best guess” when requirements are unclear.
- Missing non-goals.
- No measurable success criteria.
- No regression requirements for bug fixes.
- No API compatibility policy.
- No observability requirements.
- For mobile projects: forgetting to state real-device test requirements (and relying only on simulator/emulator tests).
- Mixing implementation gates with GA-only release evidence in one mandatory checklist, causing development to halt too early.

---

## J) Rule of thumb

$$
\text{Better Inputs} \Rightarrow \text{Less Rework} \Rightarrow \text{Higher Spec-Kit Output Quality}
$$
