export const multiMemberSyncIntegrationScenario = {
  name: 'two members receive propagated mutation',
  steps: [
    'member A mutates shared list',
    'backend stores source-of-truth mutation',
    'member B receives mutation over realtime channel',
  ],
};
