import type { ListEventRepository } from '../list-event-repository';

export interface RemoveItemRequest {
  householdId: string;
  actorUserId: string;
  itemId: string;
  normalizedName: string;
  displayNameHe: string;
  clientActionId: string;
}

export async function removeItemRoute(repo: ListEventRepository, request: RemoveItemRequest): Promise<{ status: 'ok' }> {
  await repo.insertIfNotExists({
    eventId: `${request.householdId}-${request.clientActionId}`,
    householdId: request.householdId,
    clientActionId: request.clientActionId,
    actorUserId: request.actorUserId,
    actionType: 'REMOVE',
    normalizedName: request.normalizedName,
    displayNameHe: request.displayNameHe,
  });

  return { status: 'ok' };
}
