-- 002_list_events_items.sql
-- Event-backed list projection schema.

CREATE TABLE IF NOT EXISTS list_events (
  id UUID PRIMARY KEY,
  household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
  actor_user_id UUID NOT NULL,
  action_type TEXT NOT NULL CHECK (action_type IN ('ADD_OR_MERGE', 'REMOVE')),
  item_normalized_name TEXT NOT NULL,
  item_display_name_he TEXT NOT NULL,
  quantity_delta INTEGER,
  resulting_quantity INTEGER,
  client_action_id TEXT NOT NULL,
  server_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  source TEXT NOT NULL CHECK (source IN ('VOICE', 'MANUAL', 'OFFLINE_REPLAY')),
  stt_provider TEXT,
  stt_confidence NUMERIC,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_household_client_action UNIQUE (household_id, client_action_id)
);

CREATE INDEX IF NOT EXISTS idx_list_events_household_server_ts ON list_events(household_id, server_timestamp);

CREATE TABLE IF NOT EXISTS list_items (
  id UUID PRIMARY KEY,
  household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
  normalized_name TEXT NOT NULL,
  display_name_he TEXT NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity >= 1),
  is_removed BOOLEAN NOT NULL DEFAULT FALSE,
  last_event_id UUID REFERENCES list_events(id),
  last_server_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_by_user_id UUID NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_list_items_household_name ON list_items(household_id, normalized_name);
