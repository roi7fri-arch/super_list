export interface HealthStatus {
  status: 'ok' | 'degraded';
  timestamp: string;
}

export function health(): HealthStatus {
  return { status: 'ok', timestamp: new Date().toISOString() };
}

export function readiness(): HealthStatus {
  return { status: 'ok', timestamp: new Date().toISOString() };
}

export function metrics(): string {
  return [
    '# HELP service_uptime_seconds Uptime in seconds',
    '# TYPE service_uptime_seconds counter',
    'service_uptime_seconds 0',
  ].join('\n');
}
