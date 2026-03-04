export const offlineReconnectE2EFlow = {
  id: 'offline-reconnect-sync',
  steps: [
    'go offline on member device',
    'add/remove list items while offline',
    'reconnect and replay queue',
    'verify shared state convergence across household devices',
  ],
};
