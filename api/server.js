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
      coupons: new Map(),
      events: [],
      seq: 0,
    });
  }
  return households.get(householdId);
}

function normalizeCouponNumber(raw) {
  return String(raw ?? '').replace(/\D+/g, '');
}

function maxIsoTimestamp(first, second) {
  return [first, second].filter(Boolean).sort().at(-1) ?? null;
}

function mergeCoupon(existing, incoming) {
  const balanceLastCheckedAt = maxIsoTimestamp(existing?.balanceLastCheckedAt, incoming.balanceLastCheckedAt);
  const keepExistingBalance = balanceLastCheckedAt && balanceLastCheckedAt === existing?.balanceLastCheckedAt;

  return {
    id: existing?.id ?? incoming.id ?? `coupon-${incoming.couponNumber}`,
    couponNumber: incoming.couponNumber,
    remainingBalance: keepExistingBalance
      ? (existing?.remainingBalance ?? incoming.remainingBalance ?? null)
      : (incoming.remainingBalance ?? existing?.remainingBalance ?? null),
    balanceLastCheckedAt,
    lastImportedAt: maxIsoTimestamp(existing?.lastImportedAt, incoming.lastImportedAt) ?? new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
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

app.get('/households/:householdId/coupons', (req, res) => {
  const { householdId } = req.params;
  const store = getStore(householdId);

  const coupons = Array.from(store.coupons.values()).sort((a, b) => {
    const left = a.balanceLastCheckedAt ?? a.lastImportedAt ?? '';
    const right = b.balanceLastCheckedAt ?? b.lastImportedAt ?? '';
    return right.localeCompare(left);
  });

  res.json({
    householdId,
    serverTime: new Date().toISOString(),
    coupons,
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

app.post('/households/:householdId/coupons', (req, res) => {
  const { householdId } = req.params;
  const {
    clientActionId,
    couponNumber,
    remainingBalance,
    balanceLastCheckedAt,
    lastImportedAt,
  } = req.body ?? {};

  const normalizedCouponNumber = normalizeCouponNumber(couponNumber);
  if (normalizedCouponNumber.length < 9) {
    return res.status(400).json({ error: 'couponNumber must contain at least 9 digits' });
  }

  const store = getStore(householdId);
  if (clientActionId && store.actions.has(clientActionId)) {
    return res.json({ status: 'duplicate_ignored' });
  }

  if (clientActionId) {
    store.actions.add(clientActionId);
  }

  const merged = mergeCoupon(store.coupons.get(normalizedCouponNumber), {
    id: `coupon-${normalizedCouponNumber}`,
    couponNumber: normalizedCouponNumber,
    remainingBalance: String(remainingBalance ?? '').trim() || null,
    balanceLastCheckedAt: String(balanceLastCheckedAt ?? '').trim() || null,
    lastImportedAt: String(lastImportedAt ?? '').trim() || new Date().toISOString(),
  });

  store.coupons.set(normalizedCouponNumber, merged);
  return res.json({ status: 'ok', coupon: merged });
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
