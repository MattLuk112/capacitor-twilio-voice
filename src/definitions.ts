export interface TwilioVoicePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
