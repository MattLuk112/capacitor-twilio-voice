import { registerPlugin } from '@capacitor/core';

import type { TwilioVoicePlugin } from './definitions';

const TwilioVoice = registerPlugin<TwilioVoicePlugin>('TwilioVoice', {
  web: () => import('./web').then(m => new m.TwilioVoiceWeb()),
});

export * from './definitions';
export { TwilioVoice };
