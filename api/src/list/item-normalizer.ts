export function normalizeItemName(raw: string): string {
  return raw
    .normalize('NFKC')
    .replace(/[\u0591-\u05C7]/g, '')
    .replace(/[-_]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

export function mergeQuantity(currentQuantity: number, incomingQuantity: number): number {
  return currentQuantity + incomingQuantity;
}
