import { WebPlugin } from '@capacitor/core';

import type { TwilioVoicePlugin } from './definitions';

export class TwilioVoiceWeb extends WebPlugin implements TwilioVoicePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
