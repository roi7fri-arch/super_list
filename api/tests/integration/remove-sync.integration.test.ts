export const removeSyncIntegrationScenario = {
  name: 'remove mutation propagation',
  steps: [
    'member A removes item',
    'event persisted and projection updated',
    'sync event delivered to member B',
  ],
  expected: ['item hidden on both clients', 'activity log includes REMOVE'],
};
