---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Test tasks are REQUIRED by constitution. Every feature MUST include appropriate unit,
integration, and (for critical journeys) e2e coverage. Every bugfix MUST include a regression test.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**Shared-list Rule**: For shared household list features, include explicit tasks for backend
source-of-truth synchronization, realtime mutation broadcast, client subscription/local merge,
fallback polling, offline replay, household-scoped authorization, and sync SLA evidence.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.
  
  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/
  
  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment
  
  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize [language] project with [framework] dependencies
- [ ] T003 [P] Configure linting and formatting tools
- [ ] T004 [P] Configure strict type-checking in CI
- [ ] T005 [P] Enforce protected branch and pull-request review rules
- [ ] T006 [P] Configure he-IL locale defaults and RTL layout testing baseline
- [ ] T007 [P] Define SLO dashboard baselines for voice latency, sync latency, and availability

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T008 Setup database schema and migrations framework
- [ ] T009 [P] Implement authentication/authorization with household-based authorization
- [ ] T010 [P] Setup API routing and middleware structure
- [ ] T011 Create base models/entities that all stories depend on
- [ ] T012 Configure error handling and structured logging with correlation IDs
- [ ] T013 Setup environment configuration and secrets management
- [ ] T014 Define health/readiness/metrics endpoints or platform equivalents
- [ ] T015 Define baseline alert rules for critical flows and SLO breach risk
- [ ] T016 Implement microphone-consent flow scaffolding and permission-revocation handling
- [ ] T017 Enforce privacy constraints (no long-term raw audio retention, no third-party data sale)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (MANDATORY) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T018 [P] [US1] Unit tests for core logic in tests/unit/test_[name].py
- [ ] T019 [P] [US1] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T020 [P] [US1] Integration test for [user journey] in tests/integration/test_[name].py
- [ ] T021 [US1] E2E test for critical path in tests/e2e/test_[name].py
- [ ] T122 [US1] Real-device two-member sync flow evidence in tests/artifacts/mobile-device-runs/[name]/report.junit.xml

### Implementation for User Story 1

- [ ] T022 [P] [US1] Create [Entity1] model in src/models/[entity1].py
- [ ] T023 [P] [US1] Create [Entity2] model in src/models/[entity2].py
- [ ] T024 [US1] Implement [Service] in src/services/[service].py (depends on T022, T023)
- [ ] T025 [US1] Implement [endpoint/feature] in src/[location]/[file].py
- [ ] T026 [US1] Add validation and error/empty/loading states
- [ ] T027 [US1] Add he-IL text and RTL UX behavior for user-facing surfaces
- [ ] T028 [US1] Add accessibility semantics, keyboard and focus support
- [ ] T029 [US1] Add performance instrumentation and budget assertions

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (MANDATORY) ⚠️

- [ ] T030 [P] [US2] Unit tests for core logic in tests/unit/test_[name].py
- [ ] T031 [P] [US2] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T032 [P] [US2] Integration test for [user journey] in tests/integration/test_[name].py
- [ ] T033 [US2] E2E test for critical path in tests/e2e/test_[name].py

### Implementation for User Story 2

- [ ] T034 [P] [US2] Create [Entity] model in src/models/[entity].py
- [ ] T035 [US2] Implement [Service] in src/services/[service].py
- [ ] T036 [US2] Implement [endpoint/feature] in src/[location]/[file].py
- [ ] T037 [US2] Integrate with User Story 1 components (if needed)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (MANDATORY) ⚠️

- [ ] T038 [P] [US3] Unit tests for core logic in tests/unit/test_[name].py
- [ ] T039 [P] [US3] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T040 [P] [US3] Integration test for [user journey] in tests/integration/test_[name].py
- [ ] T041 [US3] E2E test for critical path in tests/e2e/test_[name].py

### Implementation for User Story 3

- [ ] T042 [P] [US3] Create [Entity] model in src/models/[entity].py
- [ ] T043 [US3] Implement [Service] in src/services/[service].py
- [ ] T044 [US3] Implement [endpoint/feature] in src/[location]/[file].py

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation updates in docs/
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimization across all stories
- [ ] TXXX [P] Verify endpoint p95/p99 and frontend usability budgets with evidence artifacts
- [ ] TXXX [P] Verify product SLOs: voice update p95 < 2.5s, sync p95 < 1.0s, availability >= 99.5%
- [ ] TXXX [P] Validate Hebrew he-IL and RTL-first UX for release-critical screens
- [ ] TXXX [P] Validate backward compatibility and deprecation notes for contract changes
- [ ] TXXX [P] Confirm least-privilege, data minimization, retention, and secrets compliance
- [ ] TXXX [P] Validate consent UX and permission-revocation behavior for microphone capture
- [ ] TXXX Security hardening
- [ ] TXXX [P] Add regression tests for every bugfix included in scope
- [ ] TXXX [P] Confirm release criteria evidence for voice add, shared sync, and select/remove flows
- [ ] TXXX [P] Capture sync propagation latency and delivery success evidence in tests/artifacts/mobile-device-runs/
- [ ] TXXX [P] Save Android+iOS connected-device two-member sync logs/reports in tests/artifacts/mobile-device-runs/
- [ ] TXXX Run quickstart.md validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"

# Launch all models for User Story 1 together:
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
