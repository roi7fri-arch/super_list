export const rtlAccessibilityChecks = {
  id: 'rtl-accessibility-shared-list',
  assertions: [
    'primary controls have 48x48 touch targets',
    'Hebrew accessibility labels present',
    'visible focus states are rendered',
    'error/empty/loading states are Hebrew RTL-consistent',
  ],
};
