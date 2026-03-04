export const addByVoiceContractChecklist = {
  method: 'POST',
  path: '/households/{householdId}/items:addByVoice',
  requiredRequestFields: ['clientActionId', 'transcriptText'],
  requiredResponseFields: ['eventId', 'householdId', 'item', 'serverTimestamp'],
};
