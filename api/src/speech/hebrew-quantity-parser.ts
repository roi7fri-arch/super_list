const DIGIT_PATTERN = /\b([1-9][0-9]?)\b/;

const HEBREW_NUMBERS: Record<string, number> = {
  'אחד': 1,
  'אחת': 1,
  'שניים': 2,
  'שתיים': 2,
  'שלוש': 3,
  'ארבע': 4,
  'חמש': 5,
  'שש': 6,
  'שבע': 7,
  'שמונה': 8,
  'תשע': 9,
  'עשר': 10,
  'עשרה': 10,
  'תשעים ותשע': 99,
};

export function parseHebrewQuantity(transcriptText: string): number {
  const normalized = transcriptText.trim();
  const digitMatch = normalized.match(DIGIT_PATTERN);

  if (digitMatch) {
    const asNumber = Number(digitMatch[1]);
    if (asNumber >= 1 && asNumber <= 99) {
      return asNumber;
    }
  }

  for (const [phrase, value] of Object.entries(HEBREW_NUMBERS)) {
    if (normalized.includes(phrase)) {
      return value;
    }
  }

  return 1;
}
