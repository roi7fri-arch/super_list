-- 001_households_memberships.sql
-- Shared Hebrew Voice Shopping List (Mobile)

CREATE TABLE IF NOT EXISTS households (
  id UUID PRIMARY KEY,
  name TEXT,
  invite_code TEXT NOT NULL UNIQUE,
  invite_link_token TEXT UNIQUE,
  owner_user_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS memberships (
  id UUID PRIMARY KEY,
  household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
  user_id UUID NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
  status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'LEFT', 'REMOVED')),
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  left_at TIMESTAMPTZ,
  CONSTRAINT uq_active_membership_per_user UNIQUE (user_id, status)
);

CREATE INDEX IF NOT EXISTS idx_memberships_household_id ON memberships(household_id);
CREATE INDEX IF NOT EXISTS idx_memberships_user_id ON memberships(user_id);
