import { publishToHousehold, type SyncEventEnvelope } from './household-ws';

export function broadcastHouseholdMutation(householdId: string, mutationEvent: SyncEventEnvelope): void {
  publishToHousehold(householdId, mutationEvent);
}
