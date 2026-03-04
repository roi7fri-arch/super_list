export interface SharedListItemProjection {
  quantity: number;
  lastServerTs: number;
  removed: boolean;
}

export interface SharedListMutation {
  actionType: 'ADD_OR_MERGE' | 'REMOVE';
  serverTs: number;
  quantityDelta?: number;
}

export function applyMutationToProjection(
  current: SharedListItemProjection,
  mutation: SharedListMutation,
): SharedListItemProjection {
  if (mutation.serverTs < current.lastServerTs) {
    return current;
  }

  if (mutation.actionType === 'REMOVE') {
    return { ...current, removed: true, lastServerTs: mutation.serverTs };
  }

  return {
    quantity: current.quantity + (mutation.quantityDelta ?? 1),
    lastServerTs: mutation.serverTs,
    removed: false,
  };
}
