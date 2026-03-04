export interface ActivityLogRequest {
  householdId: string;
  cursor?: string;
  limit?: number;
}

export function getActivityLogRoute(request: ActivityLogRequest): { nextCursor: string | null; events: unknown[] } {
  void request;
  return { nextCursor: null, events: [] };
}
