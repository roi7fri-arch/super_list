export const householdListSubscribeContractChecklist = {
  getPath: '/households/{householdId}/list',
  subscribeEventType: 'LIST_ITEM_MUTATED',
  requiredResponseFields: ['householdId', 'items', 'serverTime'],
};
