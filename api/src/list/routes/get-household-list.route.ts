export interface GetHouseholdListRequest {
  householdId: string;
  userId: string;
}

export interface HouseholdListResponse {
  householdId: string;
  serverTime: string;
  items: Array<{ id: string; name: string; quantity: number; removed: boolean }>;
}

export function getHouseholdListRoute(request: GetHouseholdListRequest): HouseholdListResponse {
  return {
    householdId: request.householdId,
    serverTime: new Date().toISOString(),
    items: [],
  };
}
