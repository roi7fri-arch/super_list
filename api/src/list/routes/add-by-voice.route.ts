import type { ListEventRepository } from '../list-event-repository';
import { parseVoiceCommand } from '../../speech/voice-command-service';

export interface AddByVoiceRequest {
  householdId: string;
  actorUserId: string;
  clientActionId: string;
  transcriptText: string;
}

export async function addByVoiceRoute(repo: ListEventRepository, request: AddByVoiceRequest): Promise<{ status: 'ok' }> {
  const parsed = parseVoiceCommand(request.transcriptText);

  await repo.insertIfNotExists({
    eventId: crypto.randomUUID(),
    householdId: request.householdId,
    clientActionId: request.clientActionId,
    actorUserId: request.actorUserId,
    actionType: 'ADD_OR_MERGE',
    normalizedName: parsed.normalizedName,
    displayNameHe: parsed.displayNameHe,
    quantityDelta: parsed.quantity,
  });

  return { status: 'ok' };
}
