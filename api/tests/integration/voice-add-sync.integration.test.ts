export interface VoiceAddSyncIntegrationScenario {
  name: string;
  steps: string[];
  expected: string[];
}

export const voiceAddSyncIntegrationScenario: VoiceAddSyncIntegrationScenario = {
  name: 'release->parse->persist->sync',
  steps: [
    'press-and-hold >= 300ms',
    'release and parse Hebrew transcript',
    'persist event with idempotency key',
    'publish household sync event',
  ],
  expected: [
    'list item exists in projection',
    'sync event delivered to household subscribers',
  ],
};
