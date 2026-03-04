import express from 'express';
import cors from 'cors';

const app = express();
app.use(cors());
app.use(express.json());

const households = new Map();

function normalizeItemName(raw) {
  return String(raw ?? '')
    .normalize('NFKC')
    .replace(/[\u0591-\u05C7]/g, '')
    .replace(/[-_]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

function getStore(householdId) {
  if (!households.has(householdId)) {
    households.set(householdId, {
      actions: new Set(),
      items: new Map(),
      events: [],
      seq: 0,
    });
  }
  return households.get(householdId);
}

function pushEvent(store, householdId, event) {
  store.seq += 1;
  const full = {
    eventId: `${householdId}-${store.seq}`,
    sequence: store.seq,
    serverTimestamp: new Date().toISOString(),
    ...event,
  };
  store.events.push(full);
  return full;
}

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', time: new Date().toISOString() });
});

app.get('/households/:householdId/list', (req, res) => {
  const { householdId } = req.params;
  const store = getStore(householdId);

  const items = Array.from(store.items.values())
    .filter((x) => x.quantity > 0)
    .sort((a, b) => a.name.localeCompare(b.name, 'he'));

  res.json({
    householdId,
    serverTime: new Date().toISOString(),
    items,
  });
});

app.post('/households/:householdId/mutations', (req, res) => {
  const { householdId } = req.params;
  const {
    clientActionId,
    actionType,
    itemName,
    quantityDelta,
  } = req.body ?? {};

  if (!clientActionId || !actionType || !itemName) {
    return res.status(400).json({ error: 'clientActionId, actionType and itemName are required' });
  }

  if (actionType !== 'ADD_OR_MERGE' && actionType !== 'REMOVE') {
    return res.status(400).json({ error: 'actionType must be ADD_OR_MERGE or REMOVE' });
  }

  const store = getStore(householdId);
  if (store.actions.has(clientActionId)) {
    return res.json({ status: 'duplicate_ignored' });
  }

  store.actions.add(clientActionId);
  const key = normalizeItemName(itemName);

  if (!key) {
    return res.status(400).json({ error: 'itemName is invalid' });
  }

  if (actionType === 'ADD_OR_MERGE') {
    const delta = Math.max(1, Math.min(99, Number(quantityDelta ?? 1) || 1));
    const existing = store.items.get(key);
    if (existing) {
      existing.quantity += delta;
      existing.updatedAt = new Date().toISOString();
    } else {
      store.items.set(key, {
        itemId: key,
        name: String(itemName).trim(),
        quantity: delta,
        updatedAt: new Date().toISOString(),
      });
    }

    const event = pushEvent(store, householdId, {
      actionType,
      itemName: String(itemName).trim(),
      quantityDelta: delta,
      clientActionId,
    });

    return res.json({ status: 'ok', event });
  }

  store.items.delete(key);
  const event = pushEvent(store, householdId, {
    actionType,
    itemName: String(itemName).trim(),
    quantityDelta: null,
    clientActionId,
  });

  return res.json({ status: 'ok', event });
});

app.get('/households/:householdId/sync/replay', (req, res) => {
  const { householdId } = req.params;
  const since = Number(req.query.since ?? 0) || 0;
  const store = getStore(householdId);
  const events = store.events.filter((e) => e.sequence > since);

  res.json({
    householdId,
    events,
    nextSequence: store.seq,
  });
});

const port = Number(process.env.PORT ?? 8789);
app.listen(port, '0.0.0.0', () => {
  console.log(`super-list api listening on http://0.0.0.0:${port}`);
});
