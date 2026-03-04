export interface SyncReplayRequest {
  householdId: string;
  clientId: string;
  watermark: string;
}

export interface SyncReplayResponse {
  events: unknown[];
  nextWatermark: string;
}

export function syncReplayRoute(request: SyncReplayRequest): SyncReplayResponse {
  return {
    events: [],
    nextWatermark: request.watermark,
  };
}
