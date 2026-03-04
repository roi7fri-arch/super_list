export const concurrencyActivityLogScenario = {
  name: 'concurrent mutations with ordered activity log',
  expectedPolicy: 'SERVER_TIMESTAMP_LWW_REMOVE_WINS',
};
