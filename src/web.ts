import { WebPlugin } from '@capacitor/core';

import type { DeliveredNotifications, TwilioVoicePlugin } from './definitions';

export class TwilioVoiceWeb extends WebPlugin implements TwilioVoicePlugin {
  async registerTwilio(options: {
    accessToken: string;
    registrationToken: string;
  }): Promise<any> {
    console.log('REGISTER', options);
    return options;
  }

  async register(): Promise<void> {
    console.log('Register push');
    return;
  }
}
