function mergeQuantity(currentQuantity: number, incomingQuantity: number): number {
  return currentQuantity + incomingQuantity;
}

export const duplicateMergeCases = [
  { current: 2, incoming: 3, expected: 5 },
  { current: 1, incoming: 1, expected: 2 },
];

export function runDuplicateMergeChecks(): void {
  for (const testCase of duplicateMergeCases) {
    const merged = mergeQuantity(testCase.current, testCase.incoming);
    if (merged !== testCase.expected) {
      throw new Error(`merge failed: ${testCase.current}+${testCase.incoming} -> ${merged}`);
    }
  }
}
