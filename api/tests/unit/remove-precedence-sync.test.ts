import { applyMutationToProjection } from '../../src/list/shared-list-item-projection';

export function runRemovePrecedenceChecks(): void {
  const baseline = { quantity: 4, lastServerTs: 100, removed: false };
  const updated = applyMutationToProjection(baseline, { actionType: 'REMOVE', serverTs: 130 });

  if (!updated.removed) throw new Error('remove precedence failed');
}
