function normalizeItemName(raw: string): string {
  return raw
    .normalize('NFKC')
    .replace(/[\u0591-\u05C7]/g, '')
    .replace(/[-_]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

export const normalizationCases = [
  { input: ' עגבנייה  ', expected: 'עגבנייה' },
  { input: 'קוקה-קולה', expected: 'קוקה קולה' },
  { input: 'מלפפון\t', expected: 'מלפפון' },
];

export function runNormalizationChecks(): void {
  for (const testCase of normalizationCases) {
    const parsed = normalizeItemName(testCase.input);
    if (parsed !== testCase.expected) {
      throw new Error(`normalization failed: ${testCase.input} -> ${parsed}`);
    }
  }
}
