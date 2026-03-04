# Feature Specification: Shared Hebrew Voice Shopping List (Mobile)

**Feature Branch**: `001-shared-hebrew-voice-list`  
**Created**: 2026-03-03  
**Status**: Draft  
**Input**: User description: "Shared Hebrew Voice Shopping List (Mobile)"

## Problem Statement and User Value

Families sharing grocery responsibilities need one reliable, fast, shared list that can be updated
hands-free while multitasking. Manual typing is slow and causes missed items. This feature delivers
a Hebrew-first, RTL-first, voice-led list experience that keeps household members synchronized in
near real time, reducing forgotten items and entry friction.

**Mandatory requirement**: "The supermarket list MUST be synchronized in near-real-time between
all family members who installed the app and joined the same household."

## Product Context

- **Program Name**: Super List
- **Feature Name**: Shared Hebrew Voice Shopping List (Mobile)
- **Business Objective**: Help families maintain one shared supermarket list quickly, mainly by
  voice, reducing forgotten items and manual typing.
- **Target Segments**: Families in Israel; household members sharing grocery responsibilities.
- **Geography/Locale**: Hebrew (he-IL), RTL-first UI.
- **Architecture Baseline**: Mobile app (iOS + Android) + backend API + shared real-time list sync.
- **Regulatory/Legal Constraints**: Microphone permission consent, data minimization, secure
  household sharing, no third-party data sale.
- **Security Model**: Authenticated users, household-based authorization, TLS, secure token/session
  handling.
- **Allowed Data Sources**: User voice input, approved Hebrew speech-to-text service,
  user-created product entries.
- **Forbidden Data Sources**: Unlicensed external catalogs, long-term raw audio retention,
  unapproved data sharing.

## In Scope / Out of Scope

### In Scope (MVP)

1. Large red circular hold-to-talk control as primary action on app entry.
2. Voice starts on press-down and stops on release.
3. Hebrew speech recognition of product names and quantities (common number words and digits 1-99).
4. On release, recognized speech updates shared list.
5. Real-time household sync of add/remove updates.
6. Item selection and removal flow.
7. Persistent watch/delete control at top-start position (top-right in RTL).
8. Hebrew-first labels/menus/content and RTL layout.
9. Offline queue for add/remove with reconnect sync and retry banner on persistent failures.
10. Activity log of updates with timestamp ordering behavior.
11. Launcher icon uses a supermarket stroller visual to match grocery-use context.
12. Shared list screen presents items on a yellow lined notes-style surface for quick scanning.

### Out of Scope (Explicit Non-goals)

1. Barcode scanning.
2. Pricing intelligence.
3. Recipe suggestions.
4. Advanced offline conflict resolution UI.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Add Items by Voice and Sync to Household (Priority: P1)

As a household member, I want to hold a large red voice control, say grocery items in Hebrew,
and have the shared list update for everyone quickly so I can add items without typing.

**Why this priority**: This is the core business value and primary usage mode.

**Independent Test**: Can be fully tested by adding Hebrew items via hold-to-talk from one device
and verifying correct merged quantities and synced updates on another household device.

**Acceptance Scenarios**:

1. **Given** an authenticated user in an active household on the main screen in he-IL RTL,
   **When** the user presses and holds the large red circular control for at least 300ms,
   says a Hebrew item name and quantity, and releases,
   **Then** the system adds or merges the item in the shared list,
   displays Hebrew content in RTL layout,
   and syncs the update to all online household members.
2. **Given** the user performs a press shorter than 300ms,
   **When** the user releases,
   **Then** the interaction is ignored as accidental,
   no list update occurs,
   and the UI remains in Hebrew RTL idle state.
3. **Given** a spoken quantity is missing,
   **When** parsing completes,
   **Then** quantity defaults to 1,
   and the resulting item appears with Hebrew-first display conventions in RTL order.

---

### User Story 2 - Manage Shared List Items (Priority: P2)

As a household member, I want to select and remove list items and always access the persistent
watch/delete control so I can keep the list accurate while shopping.

**Why this priority**: Users must be able to complete list lifecycle management after adding items.

**Independent Test**: Can be tested by selecting items, removing them, and verifying removal sync,
including visibility and position of persistent top-start control in he-IL RTL UI.

**Acceptance Scenarios**:

1. **Given** a populated shared list shown in Hebrew RTL,
   **When** the user selects one or more items and confirms remove,
   **Then** removed items disappear from the local list,
   removal events sync to household members,
   and list labels/states remain Hebrew-first in RTL.
2. **Given** the main list screen is visible,
   **When** the user views the header area,
   **Then** the persistent watch/delete control is always visible at top-start
  (top-right in RTL), with Hebrew label and accessible semantics,
  and the list itself is rendered as a yellow lined notes-style surface.

---

### User Story 3 - Continue Through Connectivity and Concurrent Edits (Priority: P3)

As a household member, I want my add/remove actions to be retained during network issues and
safely merged with household activity so work is not lost.

**Why this priority**: Reliability is critical for shared household trust and daily utility.

**Independent Test**: Can be tested by going offline, queuing actions, reconnecting, and verifying
server timestamp last-write-wins behavior, remove-wins rule, activity log entries, and retry banner.

**Acceptance Scenarios**:

1. **Given** the user is offline,
   **When** the user adds/removes items,
   **Then** actions are queued locally,
   Hebrew RTL list reflects queued intent,
   and a retry banner appears for persistent sync failures.
2. **Given** two household members update the same item concurrently,
   **When** server conflict resolution executes,
   **Then** server timestamp last-write-wins applies,
   remove wins if latest,
   and activity log records both events in order.

---

### User Story 4 - Cross-Device Near-Real-Time Household Sync (Priority: P1)

As a household member, I want every list mutation made by any member to appear on my device in
near real time so the supermarket list stays consistent for the whole family.

**Why this priority**: Shared, synchronized state is the central product promise.

**Independent Test**: Can be tested with two logged-in household devices where one device mutates
the list and the second device receives the update within defined sync SLA.

**Acceptance Scenarios**:

1. **Given** two online users in the same household,
   **When** one user adds or removes an item,
   **Then** the backend source of truth is updated,
   and the mutation is propagated to the second user in near real time.
2. **Given** one device was offline and queued actions,
   **When** connectivity returns,
   **Then** queued actions replay idempotently,
   and all online household devices converge to the same final list state.

## Detailed Acceptance Scenarios (Given/When/Then)

1. **Given** a user has not granted microphone permission,
   **When** the user presses the red control,
   **Then** capture does not start, consent guidance is shown in Hebrew,
   and layout remains RTL with focus on the permission action.
2. **Given** permission is revoked after prior use,
   **When** the user next tries voice input,
   **Then** voice capture is blocked,
   user receives Hebrew permission recovery guidance,
   and no audio is retained.
3. **Given** spoken Hebrew contains ambiguous quantity language,
   **When** parsing cannot map confidence above accepted threshold,
   **Then** the user receives a Hebrew clarification/error state,
   no silent incorrect overwrite occurs,
   and item state remains consistent in RTL list view.
4. **Given** an item already exists with normalized equivalent name,
   **When** the user adds the same product by voice,
   **Then** duplicate entries are merged and quantity increases.
5. **Given** the app is installed on Android,
   **When** the user views the launcher icon,
   **Then** the icon displays a supermarket stroller visual aligned with app branding.
6. **Given** the Android user speaks a quantity phrase such as "שתי עגבניות" or "3 מלפפונים",
   **When** speech recognition returns transcript text,
   **Then** the app extracts quantity (2 or 3 respectively),
   merges with existing normalized item when relevant,
   and defaults quantity to 1 if no quantity token is detected.
7. **Given** the user begins hold-to-talk interaction,
   **When** press-down starts,
   **Then** the app emits a short start cue sound,
   and list navigation remains available via compact notebook icon and back arrow flow.
8. **Given** speech recognition returns transcript text,
   **When** parsing completes on device,
   **Then** the app shows an on-screen parsed preview (item + quantity) before commit,
   and allows user confirm/cancel to improve live testing confidence.
9. **Given** the user enables continuous dictation mode,
   **When** multiple grocery phrases are spoken sequentially,
   **Then** parsed items accumulate in a pending batch without requiring release between each item,
   and the user can commit all captured items in one action.
10. **Given** a user wants to share list with family,
    **When** an invite household code is shared and another member joins with that code,
    **Then** list add/remove actions are shown as household-synced updates with family sync status.
11. **Given** the user selects one or more items for removal,
    **When** the list screen is visible,
    **Then** an explicit approve-deletion action is visible near the selection status without requiring long scrolling.
12. **Given** the user presses the red voice circle,
  **When** the user keeps pressing,
  **Then** continuous dictation remains active for the full press duration,
  and recognition stops on release.
13. **Given** the transcript includes conjunction words such as "וגם",
  **When** parsing runs,
  **Then** the system splits the utterance into multiple grocery items and adds them as separate list entries.
14. **Given** a continuous utterance uses separators such as commas, semicolons, or words like
  "וגם"/"ואז",
  **When** parsing runs,
  **Then** the system treats these separators as item boundaries for reliable multi-item capture.
15. **Given** the user says "שני חלב וגם לחם" in one utterance,
  **When** parsing and separation complete,
  **Then** the list receives two distinct items: "חלב" with quantity 2, and "לחם" with quantity 1.
16. **Given** the shared list contains items,
    **When** the user presses the clear-list action once,
    **Then** all list items are removed immediately in a single action.

## Edge Case Catalog

- **Network loss during release-to-commit**: Action queued locally; user sees Hebrew retry banner;
  sync resumes on reconnect.
- **Recognition error/low-confidence transcript**: Show Hebrew error and recovery option;
  do not silently add unintended item.
- **Ambiguous quantities**: Support Hebrew number words and digits 1-99; unresolved ambiguity
  requires explicit Hebrew feedback; missing quantity defaults to 1.
- **Duplicate items with spelling variation**: Normalize names and merge by increasing quantity.
- **Concurrent edits from multiple devices**: Server timestamp last-write-wins; remove wins if latest;
  activity log preserves ordered audit events.
- **Duplicate event delivery/reconnect replay duplicates**: Server idempotency by client action key
  prevents double-application of the same mutation.
- **Accidental tap (<300ms)**: Ignore capture and keep prior list unchanged.
- **Permission revoked mid-lifecycle**: Block voice capture immediately and provide Hebrew guidance.
- **Long grocery lists**: Notes-style list remains readable with item-per-line layout and no text clipping.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST require authenticated user identity before household list access.
- **FR-002**: System MUST support exactly one active household per user in MVP.
- **FR-003**: System MUST support household invitations via invite code or invite link.
- **FR-004**: System MUST support household roles: Owner and Member.
- **FR-005**: System MUST present a large red circular primary voice action on app entry.
- **FR-006**: System MUST start voice capture on press-down and stop on release of the red control.
- **FR-007**: System MUST ignore press duration below 300ms as accidental input.
- **FR-008**: System MUST request explicit microphone consent before first capture and honor
  permission denial/revocation.
- **FR-009**: System MUST recognize Hebrew product names and quantities from speech input.
- **FR-010**: System MUST support Hebrew quantity words and digits in the 1-99 range.
- **FR-011**: System MUST default quantity to 1 when quantity is omitted.
- **FR-012**: System MUST parse recognized text on release and generate structured item updates.
- **FR-013**: System MUST normalize item names and merge duplicates by increasing quantity.
- **FR-014**: System MUST update the shared list for all members in the same household.
- **FR-015**: System MUST allow item selection and removal by users with household access.
- **FR-016**: System MUST place persistent watch/delete control at top-start position at all times
  on list screen, equivalent to top-right in RTL.
- **FR-017**: System MUST render menus, labels, states, and list content in Hebrew-first style
  with RTL layout behavior.
- **FR-018**: System MUST queue add/remove actions locally while offline.
- **FR-019**: System MUST synchronize queued actions when connectivity returns.
- **FR-020**: System MUST show Hebrew retry banner when sync failures persist.
- **FR-021**: System MUST resolve concurrent updates using server timestamp last-write-wins policy.
- **FR-022**: System MUST apply remove-wins when removal is the latest event.
- **FR-023**: System MUST keep an activity log of item update events for household audit visibility.
- **FR-024**: System MUST not persist raw audio after transcription processing completes.
- **FR-025**: System MUST retain only structured item-update events for limited audit retention.
- **FR-026**: System MUST not use unlicensed external catalogs for product data.
- **FR-027**: System MUST not share household data through unapproved channels.
- **FR-028**: System MUST not sell product or user data to third parties.
- **FR-029**: Backend MUST be the source of truth for each household shared supermarket list.
- **FR-030**: Each successful list mutation MUST be broadcast to all online devices in the same
  household in near real time.
- **FR-031**: Offline clients MUST queue mutations and replay them on reconnect using idempotency
  keys to avoid duplicate application.
- **FR-032**: Conflict resolution policy MUST be explicit and testable: server timestamp
  last-write-wins, and remove wins if latest.
- **FR-033**: Mobile launcher icon MUST use supermarket stroller branding for grocery context recognition.
- **FR-034**: Shared grocery list UI MUST support a yellow lined notes-style presentation while
  keeping item text legible in Hebrew RTL.
- **FR-035**: Android transcript handling MUST extract Hebrew/digit quantities from recognized text
  and apply duplicate-merge quantity increments in local list state for device-level validation.
- **FR-036**: Android hold-to-talk interaction MUST emit a short start cue and use compact
  icon/back navigation between voice and list surfaces.
- **FR-037**: Android voice flow MUST show parsed preview (item + quantity) and explicit
  confirm/cancel actions before committing recognized transcript to list state.
- **FR-038**: Android voice flow MUST support continuous dictation mode that captures multiple
  sequential items into a pending batch and allows one-tap "add all" commit.
- **FR-039**: Android list screen MUST provide household invite/join controls and visible family
  sync status for add/remove synchronization context.
- **FR-040**: Android list screen MUST expose a clearly visible approve-deletion control close to
  selected-item count so remove confirmation is immediately accessible.
- **FR-041**: Android red voice circle MUST toggle continuous dictation mode directly
  by press-and-hold behavior (active while pressed, stop on release) as the default speech interaction.
- **FR-042**: Transcript parsing MUST recognize conjunction separators (for example "וגם" and standalone
  connector "ו") to support adding multiple items from one continuous utterance.
- **FR-043**: Transcript parsing MUST robustly handle common separator forms (for example commas,
  semicolons, "וגם", "ואז", and prefixed conjunction+quantity tokens such as "ושלוש") for
  multi-item separation in continuous dictation.
- **FR-044**: Android build MUST include automated unit tests validating multi-item transcript
  separation behavior (including "וגם", standalone "ו", prefixed conjunction forms like "ולחם",
  prefixed conjunction+quantity forms like "ושלוש", and punctuation separators).
- **FR-045**: List screen MUST provide a one-press clear-list action that removes all current items.

### Non-functional Requirements

#### Performance & Reliability

- **NFR-001**: Voice capture-to-list update latency MUST meet p95 < 2.5 seconds.
- **NFR-002**: Shared list sync latency MUST meet p95 < 1.0 second.
- **NFR-003**: Shared list service availability MUST meet 99.5% or higher.
- **NFR-004**: System MUST provide eventual consistency after reconnect for queued offline actions.

#### Accessibility & UX

- **NFR-005**: All primary controls MUST provide minimum 48x48 touch target size.
- **NFR-006**: Interactive controls MUST include Hebrew accessibility labels.
- **NFR-007**: Visible focus indication MUST exist for focusable UI states.
- **NFR-008**: Screen-reader compatible announcements MUST exist for add/remove outcomes.
- **NFR-009**: Haptic feedback MUST be provided for hold-to-talk start/stop and key success/failure states.
- **NFR-010**: User-facing empty/loading/error states MUST be present in Hebrew and RTL-consistent.
- **NFR-018**: Notes-style list presentation MUST preserve text contrast and readability on common
  Android device screens.

#### Security, Privacy, and Localization

- **NFR-011**: All in-transit data MUST be protected by TLS.
- **NFR-012**: Session/token handling MUST follow secure lifecycle practices.
- **NFR-013**: Authorization checks MUST enforce household boundaries for every list action.
- **NFR-014**: Data minimization MUST apply to stored and transmitted fields.
- **NFR-015**: Locale baseline MUST be he-IL with RTL-first direction on all in-scope screens.
- **NFR-016**: Near-real-time sync propagation to online household devices MUST meet p95 < 1.0 second.
- **NFR-017**: Sync propagation reliability to online household devices MUST be >=99.5% for successful
  accepted mutations in the measurement window.

## Data and Compliance Constraints

1. Only approved voice input, approved Hebrew speech-to-text output, and user-created entries are
   permitted data inputs.
2. Raw voice audio is transient processing input and must not be retained long-term.
3. Structured list events are retained for limited audit according to product retention policy.
4. Household sharing data access is limited to authenticated users in the same household.
5. No third-party data sale is permitted.
6. No unapproved data sharing is permitted.

## Roles and Permissions Matrix

| Capability | Owner | Member |
|---|---|---|
| Join household by invite | Yes | Yes |
| Maintain one active household | Yes | Yes |
| Add items via voice | Yes | Yes |
| View shared household list | Yes | Yes |
| Receive near-real-time sync updates | Yes | Yes |
| Select/remove items | Yes | Yes |
| View activity log | Yes | Yes |
| Manage household membership | Yes | No |

## Contract Impact Notes (Sync/Events/API Shape)

1. Shared-list update contract MUST carry household identifier, actor identifier, normalized item
   name, quantity delta/state, action type (add/remove), and server timestamp.
2. Real-time sync events MUST preserve ordering semantics required for last-write-wins conflict
   resolution and remove-wins policy.
3. Contract updates MUST remain backward-compatible for supported mobile client versions in MVP
   release window.
4. Activity-log event contract MUST support traceability of add/remove outcomes without raw audio.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least 90% of validated Hebrew voice commands produce the intended list update.
- **SC-002**: Voice capture-to-list update meets p95 < 2.5 seconds in production measurement window.
- **SC-003**: Shared sync propagation meets p95 < 1.0 second across active household devices.
- **SC-004**: Shared list service meets 99.5% monthly availability.
- **SC-005**: At least 95% of users complete the primary "voice add item" task on first attempt.
- **SC-006**: 100% of release-critical flows (voice add, shared sync, select/remove) pass required
  tests and quality gates before release.
- **SC-007**: 100% of in-scope screens in MVP pass Hebrew-first RTL UX validation checklist.
- **SC-008**: For online household members, accepted list mutations propagate to all connected
  household devices with p95 < 1.0 second.
- **SC-009**: Sync propagation success rate for online household members is >=99.5% in the
  production measurement window.
- **SC-010**: Android build presents supermarket stroller launcher icon and lined yellow notes-style
  grocery list UI in connected-device validation.
- **SC-011**: Android device validation demonstrates transcript-to-quantity mapping for representative
  phrases (for example "שתי עגבניות", "3 מלפפונים") without manual quantity typing.
- **SC-012**: Android device validation demonstrates continuous dictation capturing at least three
  sequential items and committing them in a single batch action.
- **SC-013**: Automated tests MUST pass for the regression phrase "שני חלב וגם לחם" and verify
  correct split into two items with quantities (2,1) instead of one merged text item.
- **SC-014**: Clear-list action removes all visible list items in one press and leaves the list in empty state.

## Assumptions + Dependencies

### Assumptions

1. Approved Hebrew speech-to-text provider remains available and supports required recognition quality.
2. Households are small family groups and one active household per user is acceptable for MVP.
3. Audit retention duration is limited and defined by product policy outside this feature spec.
4. Invite links/codes are sufficient for MVP onboarding and household sharing.

### Dependencies

1. Mobile apps support he-IL and RTL rendering behaviors consistently.
2. Backend supports real-time household event distribution.
3. Authentication and household authorization services are available.
4. Observability stack can measure latency and availability SLOs.

## Milestones and Gate Timing Policy

To prevent blocking implementation with GA-only checks, this feature defines two milestones:

### Milestone A: Implementation Complete

Required to continue development iterations:

1. Core code paths implemented for US1-US4.
2. Unit/integration/contract/E2E definitions and harnesses present.
3. Real-device execution scripts and artifact paths are in place.
4. Environment blockers are documented with prerequisites when execution is unavailable
   (for example iOS physical-device execution on non-macOS hosts).

### Milestone B: Release Ready (GA)

Required before shipping to production:

1. Actual Android and iOS physical-device runs completed (where platform support exists).
2. SLO evidence exported from release-candidate environment.
3. Two-member sync real-device evidence attached.
4. Final product/engineering/QA/release sign-offs completed.

Policy:
- During Milestone A, GA-only items may be tracked as `BLOCKED`/`PLANNED` and are not treated as
  implementation-stop failures.
- Before Milestone B, all GA-only items must be converted to pass/fail and completed.

## Gate Alignment Requirements *(mandatory)*

- **GQ-001 (Code Quality)**: Merge requires passing lint and type checks with protected-branch
  review evidence.
- **GT-001 (Testing)**: Merge requires unit, integration, and e2e evidence for release-critical paths.
- **GT-002 (Regression)**: Any bugfix in this feature must include a failing-then-passing regression test.
- **GUX-001 (UX/Accessibility)**: Hebrew-first RTL behavior and accessibility baseline are mandatory
  for every user-facing behavior in scope.
- **GUX-002 (State Consistency)**: Error, empty, and loading states must be defined and validated in Hebrew RTL.
- **GP-001 (Performance)**: Endpoint and user flow latency budgets must map to SC-002 and SC-003.
- **GP-002 (Capacity)**: Capacity assumptions and measurement method must be documented during planning.
- **GP-003 (SLOs)**: SLO evidence must be produced for latency and availability release criteria.
- **GS-001 (Security & Privacy)**: TLS, token/session safety, least privilege, minimization,
  and retention controls are mandatory.
- **GS-002 (Consent)**: Microphone consent and revocation behavior must be testable and validated.
- **GS-003 (Prohibited Use)**: Long-term raw audio retention and third-party data sale are prohibited.
- **GC-001 (Contract & Compatibility)**: Contract changes require compatibility assessment and version impact notes.
- **GO-001 (Observability & Operations)**: Health/readiness/metrics, structured logs, correlation IDs,
  and critical-flow alerts are required.

## NEEDS CLARIFICATION

None.
