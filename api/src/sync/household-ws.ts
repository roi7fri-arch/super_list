export interface SyncEventEnvelope {
  eventId: string;
  householdId: string;
  actionType: 'ADD_OR_MERGE' | 'REMOVE';
  serverTimestamp: string;
  correlationId: string;
}

export function publishToHousehold(householdId: string, event: SyncEventEnvelope): void {
  if (!householdId) {
    throw new Error('householdId is required');
  }

  // Placeholder publisher; real implementation will bind to WebSocket broker.
  void event;
}
