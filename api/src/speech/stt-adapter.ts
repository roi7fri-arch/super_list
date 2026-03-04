export interface TranscriptionResult {
  transcriptText: string;
  confidence?: number;
  provider: string;
}

export interface SpeechToTextAdapter {
  transcribe(audioBuffer: ArrayBuffer, locale: 'he-IL'): Promise<TranscriptionResult>;
}

export function assertNoRawAudioPersistence(): void {
  // Contract marker: raw audio must never be persisted after transcription.
}
