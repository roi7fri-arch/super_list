export const syncReplayContractChecklist = {
  method: 'POST',
  path: '/households/{householdId}/sync:replay',
  requiredRequestFields: ['watermark', 'clientId'],
  requiredResponseFields: ['events', 'nextWatermark'],
};
