export interface AuthContext {
  userId: string;
  sessionId: string;
}

export function requireAuthenticatedContext(headers: Record<string, string | undefined>): AuthContext {
  const token = headers['authorization'];

  if (!token || !token.startsWith('Bearer ')) {
    throw new Error('UNAUTHORIZED');
  }

  // Placeholder implementation for Phase 2 foundation.
  // Token parsing and signature validation will be integrated in implementation phases.
  return {
    userId: 'stub-user-id',
    sessionId: 'stub-session-id',
  };
}
