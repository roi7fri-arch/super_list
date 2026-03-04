export function applyRemoveIfLatest(currentRemoved: boolean, removeTimestamp: number, latestTimestamp: number): boolean {
  if (removeTimestamp >= latestTimestamp) {
    return true;
  }

  return currentRemoved;
}

export function nextQuantityAfterMerge(current: number, incoming: number): number {
  return current + incoming;
}
