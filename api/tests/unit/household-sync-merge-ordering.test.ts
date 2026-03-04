import { applyMutationToProjection } from '../../src/list/shared-list-item-projection';

export function runHouseholdSyncMergeOrderingChecks(): void {
  const baseline = { quantity: 1, lastServerTs: 100, removed: false };
  const updated = applyMutationToProjection(baseline, { actionType: 'ADD_OR_MERGE', serverTs: 120, quantityDelta: 2 });

  if (updated.quantity !== 3) throw new Error('merge ordering failed');
}
