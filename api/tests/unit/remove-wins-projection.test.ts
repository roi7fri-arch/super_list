import { applyRemoveIfLatest } from '../../src/list/list-projection-updater';

export function runRemoveWinsProjectionChecks(): void {
  const older = applyRemoveIfLatest(false, 10, 20);
  const newer = applyRemoveIfLatest(false, 20, 10);

  if (older !== false) throw new Error('older remove should not win');
  if (newer !== true) throw new Error('latest remove must win');
}
