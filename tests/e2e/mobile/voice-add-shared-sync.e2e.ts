export const criticalVoiceAddE2EFlow = {
  id: 'voice-add-shared-sync',
  steps: [
    'Open app on Device A and Device B in same household',
    'Hold red button on Device A and speak Hebrew item+quantity',
    'Release button to trigger parse and update',
    'Verify updated item appears on Device B via sync',
  ],
  accessibilityChecks: ['Hebrew labels', 'RTL layout', 'touch target >= 48x48'],
};
