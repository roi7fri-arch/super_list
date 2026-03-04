export interface MutationEnvelope {
  type: 'ADD_OR_MERGE' | 'REMOVE';
  ts: number;
}

export function resolveByServerTimestamp(current: MutationEnvelope, incoming: MutationEnvelope): MutationEnvelope {
  if (incoming.ts > current.ts) {
    return incoming;
  }

  return current;
}
