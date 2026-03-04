export const removeItemContractChecklist = {
  method: 'POST',
  path: '/households/{householdId}/items/{itemId}:remove',
  requiredRequestFields: ['clientActionId'],
  requiredResponseFields: ['eventId', 'householdId', 'item', 'serverTimestamp'],
};
