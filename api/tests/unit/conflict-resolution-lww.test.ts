import { resolveByServerTimestamp } from '../../src/list/conflict-resolver';

export function runConflictResolutionChecks(): void {
  const winner = resolveByServerTimestamp(
    { type: 'ADD_OR_MERGE', ts: 100 },
    { type: 'REMOVE', ts: 120 }
  );

  if (winner.type !== 'REMOVE') {
    throw new Error('latest mutation must win (remove precedence when latest)');
  }
}
