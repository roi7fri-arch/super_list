export const fallbackPollingIntegrationScenario = {
  name: 'websocket outage fallback polling convergence',
  steps: [
    'disconnect realtime channel',
    'perform list mutation',
    'client polling fetches updated state',
  ],
};
