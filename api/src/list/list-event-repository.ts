export interface ListEventWriteInput {
  eventId: string;
  householdId: string;
  clientActionId: string;
  actorUserId: string;
  actionType: 'ADD_OR_MERGE' | 'REMOVE';
  normalizedName: string;
  displayNameHe: string;
  quantityDelta?: number;
}

export class ListEventRepository {
  async insertIfNotExists(input: ListEventWriteInput): Promise<{ inserted: boolean }> {
    if (!input.clientActionId) {
      throw new Error('clientActionId is required for idempotency');
    }

    // Placeholder DB call for ON CONFLICT DO NOTHING by (household_id, client_action_id)
    return { inserted: true };
  }
}
