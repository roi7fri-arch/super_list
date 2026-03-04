import { normalizeItemName } from '../list/item-normalizer';
import { parseHebrewQuantity } from './hebrew-quantity-parser';

export interface ParsedVoiceCommand {
  normalizedName: string;
  displayNameHe: string;
  quantity: number;
}

export function parseVoiceCommand(transcriptText: string): ParsedVoiceCommand {
  const quantity = parseHebrewQuantity(transcriptText);
  const displayNameHe = transcriptText.replace(/\b[1-9][0-9]?\b/g, '').trim();

  return {
    normalizedName: normalizeItemName(displayNameHe),
    displayNameHe,
    quantity,
  };
}
