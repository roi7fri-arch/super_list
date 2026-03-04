import { publishToHousehold, type SyncEventEnvelope } from './household-ws';

export function publishListMutation(householdId: string, event: SyncEventEnvelope): void {
  publishToHousehold(householdId, event);
}
