export interface LogFields {
  correlationId: string;
  householdId?: string;
  userId?: string;
  eventId?: string;
  actionId?: string;
  clientPlatform?: string;
  [key: string]: unknown;
}

export function logInfo(message: string, fields: LogFields): void {
  const payload = {
    level: 'info',
    message,
    timestamp: new Date().toISOString(),
    ...fields,
  };

  console.info(JSON.stringify(payload));
}

export function logError(message: string, fields: LogFields): void {
  const payload = {
    level: 'error',
    message,
    timestamp: new Date().toISOString(),
    ...fields,
  };

  console.error(JSON.stringify(payload));
}
