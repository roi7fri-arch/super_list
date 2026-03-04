export const offlineReplayIntegrationScenario = {
  name: 'offline replay and retry banner trigger',
  steps: [
    'enqueue actions while offline',
    'reconnect and replay by client action id',
    'show retry banner on persistent failures',
  ],
  expected: ['idempotent replay', 'converged shared list state'],
};
