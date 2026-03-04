export interface HouseholdOperationContext {
  userId: string;
  householdId: string;
  operation: 'LIST_GET' | 'LIST_MUTATE' | 'SYNC_REPLAY' | 'SUBSCRIBE';
}

export function assertHouseholdListAuthorization(context: HouseholdOperationContext): void {
  if (!context.userId || !context.householdId) {
    throw new Error('FORBIDDEN');
  }

  // Membership/role check placeholder.
}
