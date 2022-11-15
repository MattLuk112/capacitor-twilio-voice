import type { PermissionState, PluginListenerHandle } from '@capacitor/core';

export interface TwilioVoicePlugin {
  registerTwilio(options: {
    accessToken: string;
    registrationToken: string;
    userId: string;
    windowToken: string;
  }): Promise<void>;
  /**
   * Register the app to receive push notifications.
   *
   * This method will trigger the `'registration'` event with the push token or
   * `'registrationError'` if there was a problem. It does not prompt the user for
   * notification permissions, use `requestPermissions()` first.
   *
   * @since 1.0.0
   */
  register(): Promise<void>;

  /**
   * Check permission to receive push notifications.
   *
   * On Android the status is always granted because you can always
   * receive push notifications. If you need to check if the user allows
   * to display notifications, use local-notifications plugin.
   *
   * @since 1.0.0
   */
  checkPermissions(): Promise<PermissionStatus>;

  /**
   * Request permission to receive push notifications.
   *
   * On Android it doesn't prompt for permission because you can always
   * receive push notifications.
   *
   * On iOS, the first time you use the function, it will prompt the user
   * for push notification permission and return granted or denied based
   * on the user selection. On following calls it will currect status of
   * the permission without prompting again.
   *
   * @since 1.0.0
   */
  requestPermissions(): Promise<PermissionStatus>;

  /**
   * Called when the push notification registration finishes without problems.
   *
   * Provides the push notification token.
   *
   * @since 1.0.0
   */
  addListener(
    eventName: 'registration',
    listenerFunc: (token: Token) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when the push notification registration finished with problems.
   *
   * Provides an error with the registration problem.
   *
   * @since 1.0.0
   */
  addListener(
    eventName: 'registrationError',
    listenerFunc: (error: RegistrationError) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when the device receives a push notification.
   *
   * @since 1.0.0
   */
  addListener(
    eventName: 'pushNotificationReceived',
    listenerFunc: (notification: PushNotificationSchema) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when an action is performed on a push notification.
   *
   * @since 1.0.0
   */
  addListener(
    eventName: 'pushNotificationActionPerformed',
    listenerFunc: (notification: ActionPerformed) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when the twilio registration finishes without problems.
   */
  addListener(
    eventName: 'twilioRegistration',
    listenerFunc: (token: Token) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when the twilio registration finishes without problems.
   *
   * Provides the fcm notification token.
   */
  addListener(
    eventName: 'twilioRegistration',
    listenerFunc: (error: RegistrationError) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Called when the twilio registration finishes with problems.
   *
   * Provides an error with the registration problem.
   */
  addListener(
    eventName: 'twilioRegistrationError',
    listenerFunc: (token: Token) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Remove all native listeners for this plugin.
   *
   * @since 1.0.0
   */
  removeAllListeners(): Promise<void>;
}

export interface PushNotificationSchema {
  /**
   * The notification title.
   *
   * @since 1.0.0
   */
  title?: string;

  /**
   * The notification subtitle.
   *
   * @since 1.0.0
   */
  subtitle?: string;

  /**
   * The main text payload for the notification.
   *
   * @since 1.0.0
   */
  body?: string;

  /**
   * The notification identifier.
   *
   * @since 1.0.0
   */
  id: string;

  /**
   * The notification tag.
   *
   * Only available on Android (from push notifications).
   *
   * @since 4.0.0
   */
  tag?: string;

  /**
   * The number to display for the app icon badge.
   *
   * @since 1.0.0
   */
  badge?: number;

  /**
   * It's not being returned.
   *
   * @deprecated will be removed in next major version.
   * @since 1.0.0
   */
  notification?: any;

  /**
   * Any additional data that was included in the
   * push notification payload.
   *
   * @since 1.0.0
   */
  data: any;

  /**
   * The action to be performed on the user opening the notification.
   *
   * Only available on Android.
   *
   * @since 1.0.0
   */
  click_action?: string;

  /**
   * Deep link from the notification.
   *
   * Only available on Android.
   *
   * @since 1.0.0
   */
  link?: string;

  /**
   * Set the group identifier for notification grouping.
   *
   * Only available on Android. Works like `threadIdentifier` on iOS.
   *
   * @since 1.0.0
   */
  group?: string;

  /**
   * Designate this notification as the summary for an associated `group`.
   *
   * Only available on Android.
   *
   * @since 1.0.0
   */
  groupSummary?: boolean;
}

export interface ActionPerformed {
  /**
   * The action performed on the notification.
   *
   * @since 1.0.0
   */
  actionId: string;

  /**
   * Text entered on the notification action.
   *
   * Only available on iOS.
   *
   * @since 1.0.0
   */
  inputValue?: string;

  /**
   * The notification in which the action was performed.
   *
   * @since 1.0.0
   */
  notification: PushNotificationSchema;
}

export interface Token {
  /**
   * On iOS it contains the APNS token.
   * On Android it contains the FCM token.
   *
   * @since 1.0.0
   */
  value: string;
}

export interface RegistrationError {
  /**
   * Error message describing the registration failure.
   *
   * @since 4.0.0
   */
  error: string;
}

export interface DeliveredNotifications {
  /**
   * List of notifications that are visible on the
   * notifications screen.
   *
   * @since 1.0.0
   */
  notifications: PushNotificationSchema[];
}

export interface Channel {
  /**
   * The channel identifier.
   *
   * @since 1.0.0
   */
  id: string;

  /**
   * The human-friendly name of this channel (presented to the user).
   *
   * @since 1.0.0
   */
  name: string;

  /**
   * The description of this channel (presented to the user).
   *
   * @since 1.0.0
   */
  description?: string;

  /**
   * The sound that should be played for notifications posted to this channel.
   *
   * Notification channels with an importance of at least `3` should have a
   * sound.
   *
   * The file name of a sound file should be specified relative to the android
   * app `res/raw` directory.
   *
   * @since 1.0.0
   * @example "jingle.wav"
   */
  sound?: string;

  /**
   * The level of interruption for notifications posted to this channel.
   *
   * @default `3`
   * @since 1.0.0
   */
  importance?: Importance;

  /**
   * The visibility of notifications posted to this channel.
   *
   * This setting is for whether notifications posted to this channel appear on
   * the lockscreen or not, and if so, whether they appear in a redacted form.
   *
   * @since 1.0.0
   */
  visibility?: Visibility;

  /**
   * Whether notifications posted to this channel should display notification
   * lights, on devices that support it.
   *
   * @since 1.0.0
   */
  lights?: boolean;

  /**
   * The light color for notifications posted to this channel.
   *
   * Only supported if lights are enabled on this channel and the device
   * supports it.
   *
   * Supported color formats are `#RRGGBB` and `#RRGGBBAA`.
   *
   * @since 1.0.0
   */
  lightColor?: string;

  /**
   * Whether notifications posted to this channel should vibrate.
   *
   * @since 1.0.0
   */
  vibration?: boolean;
}

/**
 * The importance level. For more details, see the [Android Developer Docs](https://developer.android.com/reference/android/app/NotificationManager#IMPORTANCE_DEFAULT)
 * @since 1.0.0
 */
export type Importance = 1 | 2 | 3 | 4 | 5;

/**
 * The notification visibility. For more details, see the [Android Developer Docs](https://developer.android.com/reference/androidx/core/app/NotificationCompat#VISIBILITY_PRIVATE)
 * @since 1.0.0
 */
export type Visibility = -1 | 0 | 1;

export interface ListChannelsResult {
  /**
   * List of all the Channels created by your app.
   *
   * @since 1.0.0
   */
  channels: Channel[];
}

export interface PermissionStatus {
  /**
   * Permission state of receiving notifications.
   *
   * @since 1.0.0
   */
  receive: PermissionState;
}

/**
 * @deprecated Use 'Channel`.
 * @since 1.0.0
 */
export type NotificationChannel = Channel;

/**
 * @deprecated Use `ListChannelsResult`.
 * @since 1.0.0
 */
export type NotificationChannelList = ListChannelsResult;

/**
 * @deprecated Use `PushNotificationSchema`.
 * @since 1.0.0
 */
export type PushNotification = PushNotificationSchema;

/**
 * @deprecated Use `ActionPerformed`.
 * @since 1.0.0
 */
export type PushNotificationActionPerformed = ActionPerformed;

/**
 * @deprecated Use `DeliveredNotifications`.
 * @since 1.0.0
 */
export type PushNotificationDeliveredList = DeliveredNotifications;

/**
 * @deprecated Use `Token`.
 * @since 1.0.0
 */
export type PushNotificationToken = Token;
