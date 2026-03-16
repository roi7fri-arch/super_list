# Data Model: Shared Hebrew Voice Shopping List (Mobile)

## Overview

The model is event-backed with a current-state projection for fast list rendering. Server is authoritative for conflict resolution (`server_timestamp` LWW; `remove` wins if latest). Mobile maintains local snapshot plus pending queue.

## Entities

### 1) Household
- **Purpose**: Shared list boundary for a family.
- **Fields**:
  - `id` (UUID, PK)
  - `name` (string, optional display name)
  - `invite_code` (string, unique, active)
  - `invite_link_token` (string, unique, optional)
  - `owner_user_id` (UUID, FK -> User)
  - `created_at` (timestamp)
  - `updated_at` (timestamp)
- **Validation**:
  - Invite code and invite link token must be unique when active.
  - Exactly one owner per household.

### 2) Membership
- **Purpose**: User-to-household association and role.
- **Fields**:
  - `id` (UUID, PK)
  - `household_id` (UUID, FK -> Household)
  - `user_id` (UUID, FK -> User)
  - `role` (enum: `OWNER`, `MEMBER`)
  - `status` (enum: `ACTIVE`, `LEFT`, `REMOVED`)
  - `joined_at` (timestamp)
  - `left_at` (timestamp, nullable)
- **Validation**:
  - Unique active membership per user (MVP one active household).
  - Role must be `OWNER` or `MEMBER`.

### 3) SharedListItem (Current Projection)
- **Purpose**: Fast-read projected shared list state.
- **Fields**:
  - `id` (UUID, PK)
  - `household_id` (UUID, FK -> Household)
  - `normalized_name` (string, indexed)
  - `display_name_he` (string)
  - `quantity` (integer, min 1, max 999)
  - `is_removed` (boolean)
  - `last_event_id` (UUID, FK -> ListEvent)
  - `last_server_timestamp` (timestamp)
  - `updated_by_user_id` (UUID)
  - `updated_at` (timestamp)
- **Validation**:
  - For active item, `quantity >= 1` and `is_removed = false`.
  - For removed item, `is_removed = true`; quantity may be retained for audit context.
  - (`household_id`, `normalized_name`) unique among non-removed projection rows.

### 4) ListEvent (Activity Log / Source of Truth)
- **Purpose**: Immutable event log for add/remove operations.
- **Fields**:
  - `id` (UUID, PK)
  - `household_id` (UUID, FK -> Household)
  - `actor_user_id` (UUID)
  - `action_type` (enum: `ADD_OR_MERGE`, `REMOVE`)
  - `item_normalized_name` (string)
  - `item_display_name_he` (string)
  - `quantity_delta` (integer, nullable for remove)
  - `resulting_quantity` (integer, nullable)
  - `client_action_id` (string, unique per household)
  - `server_timestamp` (timestamp, indexed)
  - `source` (enum: `VOICE`, `MANUAL`, `OFFLINE_REPLAY`)
  - `stt_provider` (string, nullable)
  - `stt_confidence` (decimal, nullable)
  - `created_at` (timestamp)
- **Validation**:
  - `REMOVE` events must set resulting projection to removed if latest.
  - `client_action_id` enforces idempotency on retries/replay.
  - No raw audio payload field permitted.

### 5) DeviceActionQueue (Client Local)
- **Purpose**: Durable offline queue for add/remove actions.
- **Fields**:
  - `local_id` (UUID, PK)
  - `client_action_id` (string, unique)
  - `household_id` (UUID)
  - `action_type` (enum: `ADD_OR_MERGE`, `REMOVE`)
  - `payload_json` (json)
  - `enqueued_at` (timestamp)
  - `retry_count` (integer)
  - `last_error_code` (string, nullable)
  - `last_attempt_at` (timestamp, nullable)
  - `acked_server_event_id` (UUID, nullable)
- **Validation**:
  - FIFO replay by `enqueued_at` for MVP.
  - Remove action must reference item identity in payload.

### 6) CouponWalletEntry (Household Shared Snapshot + Client Cache)
- **Purpose**: Persist household-visible supermarket coupons for fast reuse at checkout across family members.
- **Fields**:
  - `local_id` (string, PK)
  - `household_id` (string)
  - `coupon_number` (string, 9+ digits)
  - `remaining_balance` (string, nullable)
  - `balance_last_checked_at` (timestamp, nullable)
  - `last_imported_at` (timestamp)
- **Validation**:
  - `coupon_number` must contain at least 9 digits after normalization.
  - Coupon number is visible in the coupon screen UI for quick checkout use.
  - Raw coupon image is not required after OCR succeeds.

### 7) CouponBalanceLookupConfig (Client Local)
- **Purpose**: Store user-configured URL template for opening supermarket balance lookup page.
- **Fields**:
  - `url_template` (string, nullable)
- **Validation**:
  - Template should contain `{coupon}` placeholder.
  - Invalid or missing template disables the lookup-launch action but does not block coupon storage.

## Relationships

- Household `1 -> N` Membership
- Household `1 -> N` ListItem
- Household `1 -> N` ListEvent
- User `1 -> N` Membership
- User `1 -> N` ListEvent (as actor)
- ListItem references latest ListEvent

## State Transitions

### Voice Add/Merge
1. Press/hold >=300ms -> capture
2. Release -> transcript parse -> structured action
3. Server writes ListEvent (`ADD_OR_MERGE`) with server timestamp
4. Projection updates ListItem quantity (merge by normalized name)
5. Event fan-out to household clients

### Remove
1. User selects item(s) and removes
2. Server writes ListEvent (`REMOVE`)
3. Projection marks removed when remove is latest
4. Fan-out + activity log update

### Offline Queue Replay
1. Client enqueues actions while offline
2. On reconnect replay in order with `client_action_id`
3. Server idempotently acknowledges already-processed actions
4. Client clears acked entries

### Coupon Import
1. User selects coupon image from gallery
2. App runs OCR on-device and extracts candidate 9+ digit numbers
3. User confirms/edits the detected number
4. App stores structured coupon metadata locally and synchronizes it to the active household when connected
5. Household members can later view the same coupon number and update remaining balance

## Conflict Resolution Rules

- Primary order: `server_timestamp` ascending.
- Tie-breaker: lexical `event_id` or monotonic sequence.
- Latest event wins for state projection.
- If latest event is remove, item state is removed.

## Migration Strategy

### Initial Migration Set
1. Create `households` table.
2. Create `memberships` with unique active-user constraint.
3. Create `list_events` immutable table + indexes:
   - (`household_id`, `server_timestamp`)
   - (`household_id`, `client_action_id`) unique
4. Create `list_items` projection table + index:
   - (`household_id`, `normalized_name`) for active rows.
5. Add retention policy support columns/config for event TTL window where applicable.

### Evolution Guidelines
- Backward-compatible additive fields first.
- For breaking changes, dual-write/dual-read transition until mobile clients migrate.
- Never add raw-audio persistence fields.

## Retention and Minimization

- Persist structured events only (no raw audio).
- Persist structured coupon metadata only when possible; avoid long-term raw coupon image retention unless future product requirements demand it.
- Retain only minimum required fields for sync, audit, and troubleshooting.
- Apply configured retention window to event logs consistent with policy.
