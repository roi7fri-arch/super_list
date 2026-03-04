import type { AuthContext } from './auth-middleware';

export interface HouseholdAuthorizationInput {
  auth: AuthContext;
  householdId: string;
  allowedRoles?: Array<'OWNER' | 'MEMBER'>;
}

export function assertHouseholdAccess(input: HouseholdAuthorizationInput): void {
  if (!input.auth.userId || !input.householdId) {
    throw new Error('FORBIDDEN');
  }

  // Placeholder for membership lookup by household and role.
}
