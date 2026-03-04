# Feature Specification: [FEATURE NAME]

**Feature Branch**: `[###-feature-name]`  
**Created**: [DATE]  
**Status**: Draft  
**Input**: User description: "$ARGUMENTS"

## Product Context *(mandatory)*

- **Program Name**: [e.g., Super List]
- **Business Objective**: [e.g., reduce forgotten grocery items via fast shared voice capture]
- **Target Segments**: [e.g., families in Israel sharing household responsibilities]
- **Geography/Locale**: [e.g., he-IL, RTL-first UI]
- **Architecture Baseline**: [e.g., iOS + Android mobile apps, backend API, real-time sync]
- **Regulatory/Legal Constraints**: [e.g., microphone consent, data minimization, no third-party data sale]
- **Allowed Data Sources**: [voice input, approved STT, user-created entries]
- **Forbidden Data Sources**: [unlicensed catalogs, long-term raw audio retention, unapproved sharing]
- **MVP Non-goals**: [explicit features out of scope]

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - [Brief Title] (Priority: P1)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently - e.g., "Can be fully tested by [specific action] and delivers [specific value]"]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]
2. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

### User Story 2 - [Brief Title] (Priority: P2)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

### User Story 3 - [Brief Title] (Priority: P3)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

### User Story 4 - Cross-Device Household Sync (Priority: P1 for shared-list features)

[For shared-list features, explicitly define the cross-device near-real-time sync journey]

**Why this priority**: [Explain user/business impact of synchronized household state]

**Independent Test**: [Describe validation across at least two household member devices]

**Acceptance Scenarios**:

1. **Given** [household with at least two members], **When** [member A mutates list], **Then** [member B sees near-real-time update]

[Add more user stories as needed, each with an assigned priority]

### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

- What happens when [boundary condition]?
- How does system handle [error scenario]?
- How does system handle [offline edits + reconnect replay + duplicate event delivery]?
- How does system handle [concurrent edits with explicit conflict policy]?

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: System MUST [specific capability, e.g., "allow users to create accounts"]
- **FR-002**: System MUST [specific capability, e.g., "validate email addresses"]  
- **FR-003**: Users MUST be able to [key interaction, e.g., "reset their password"]
- **FR-004**: System MUST [data requirement, e.g., "persist user preferences"]
- **FR-005**: System MUST [behavior, e.g., "log all security events"]
- **FR-006**: For shared-list features, backend MUST be source of truth for household list state.
- **FR-007**: For shared-list features, each mutation MUST be broadcast to all online household devices.
- **FR-008**: For shared-list features, offline clients MUST queue list actions and replay on reconnect.
- **FR-009**: For shared-list features, conflict resolution policy MUST be explicit and testable.

### Gate Alignment Requirements *(mandatory)*

- **GQ-001 (Code Quality)**: Specification MUST define how strict type safety will be enforced,
  and what lint/typecheck criteria are required for merge.
- **GT-001 (Testing)**: Specification MUST define required unit/integration/e2e coverage and
  identify contract tests for any API or schema changes.
- **GT-002 (Regression)**: For bugfix work, specification MUST identify the regression test that
  reproduces the defect.
- **GT-003 (Physical Device Mobile Testing)**: For mobile features, specification MUST require
  automated tests on at least one USB-connected physical device (not emulator-only), include at
  least one critical E2E flow passing on real device, define Android and iOS physical-device
  acceptance criteria where supported, and require machine-readable test results plus logs as
  CI/local artifacts.
- **GUX-001 (UX/Accessibility)**: Specification MUST define locale/direction behavior and
  accessibility expectations (keyboard, focus, semantic labels, contrast) for affected UI.
- **GUX-002 (State Consistency)**: Specification MUST define error, empty, and loading states for
  user-facing surfaces in scope.
- **GP-001 (Performance)**: Specification MUST define endpoint p95/p99 budgets and/or frontend
  usability budgets for critical paths in scope.
- **GP-002 (Capacity)**: Specification MUST document capacity assumptions and a measurement method.
- **GP-003 (SLOs)**: Specification MUST define latency and availability SLOs for critical flows.
- **GS-001 (Security & Privacy)**: Specification MUST define credential/session handling,
  least-privilege access expectations, data minimization, retention, and secrets handling.
- **GS-002 (Consent)**: Specification MUST define explicit microphone consent and behavior on
  permission denial/revocation.
- **GS-003 (Prohibited Use)**: Specification MUST explicitly prohibit long-term raw audio
  retention and third-party data sale.
- **GC-001 (Contract & Compatibility)**: Specification MUST state versioning impact,
  backward-compatibility expectations, deprecation lifecycle, and client compatibility obligations.
- **GO-001 (Observability & Operations)**: Specification MUST define health/readiness/metrics,
  structured logging with correlation IDs, and alert conditions for critical flows.

*Example of marking unclear requirements:*

- **FR-006**: System MUST authenticate users via [NEEDS CLARIFICATION: auth method not specified - email/password, SSO, OAuth?]
- **FR-007**: System MUST retain user data for [NEEDS CLARIFICATION: retention period not specified]

### Key Entities *(include if feature involves data)*

- **[Entity 1]**: [What it represents, key attributes without implementation]
- **[Entity 2]**: [What it represents, relationships to other entities]

### Roles & Permissions *(include for multi-user/shared features)*

| Capability | Owner | Member |
|---|---|---|
| View shared household list | [Y/N] | [Y/N] |
| Receive synced updates | [Y/N] | [Y/N] |
| Create/update/remove items | [Y/N] | [Y/N] |

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: [Measurable metric, e.g., "Users can complete account creation in under 2 minutes"]
- **SC-002**: [Measurable metric, e.g., "System handles 1000 concurrent users without degradation"]
- **SC-003**: [User satisfaction metric, e.g., "90% of users successfully complete primary task on first attempt"]
- **SC-004**: [Business metric, e.g., "Reduce support tickets related to [X] by 50%"]
- **SC-005**: [Quality gate metric, e.g., "100% of applicable constitution gates have passing
  evidence before merge"]
- **SC-006**: [SLO metric, e.g., "Voice capture-to-list update p95 < 2.5s and list sync p95 < 1.0s"]
- **SC-007**: [Availability metric, e.g., ">=99.5% monthly availability for shared list service"]
- **SC-008**: [Sync propagation SLA, e.g., "Near-real-time household mutation propagation p95 < 1.0s to online members"]
- **SC-009**: [Sync reliability metric, e.g., ">=99.5% successful mutation propagation to online household devices"]
