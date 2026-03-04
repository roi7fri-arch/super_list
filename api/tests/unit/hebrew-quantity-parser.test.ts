import { parseHebrewQuantity } from '../../src/speech/hebrew-quantity-parser';

export const hebrewQuantityParserCases = [
  { input: 'שתיים עגבניות', expected: 2 },
  { input: '7 בננות', expected: 7 },
  { input: 'עגבניות', expected: 1 },
  { input: 'תשעים ותשע שקיות תה', expected: 99 },
];

export function runHebrewQuantityParserChecks(): void {
  for (const testCase of hebrewQuantityParserCases) {
    const parsed = parseHebrewQuantity(testCase.input);
    if (parsed !== testCase.expected) {
      throw new Error(`quantity parse failed: ${testCase.input} -> ${parsed}`);
    }
  }
}
