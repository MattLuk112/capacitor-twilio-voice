package com.truckersreport.capacitor.twilio.voice;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.twilio.voice.Call;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.CallInvite;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "TwilioVoice", permissions = @Permission(strings = { "android.permission.RECORD_AUDIO" }, alias = "audio"))
public class TwilioVoicePlugin extends Plugin {

    public static Bridge staticBridge = null;
    public static RemoteMessage lastMessage = null;
    public NotificationManager notificationManager;
    public MessagingService firebaseMessagingService;
    private NotificationChannelManager notificationChannelManager;
    RegistrationListener registrationListener = registrationListener();
    Call.Listener callListener = callListener();
    private static final String TAG = "TWILIO";

    private static final String EVENT_TOKEN_CHANGE = "registration";
    private static final String EVENT_TOKEN_ERROR = "registrationError";
    private String accessToken;
    private String userId;
    private String windowToken;

    @Override
    public void load() {
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        firebaseMessagingService = new MessagingService();

        staticBridge = this.bridge;
        if (lastMessage != null) {
            fireNotification(lastMessage);
            lastMessage = null;
        }

        notificationChannelManager = new NotificationChannelManager(getActivity(), notificationManager, getConfig());
    }

    /**
     * Register for Firebase Cloud Messaging (FCM) push notifications
     * @param call
     */
    @PluginMethod
    public void register(PluginCall call) {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging
            .getInstance()
            .getToken()
            .addOnCompleteListener(
                task -> {
                    if (!task.isSuccessful()) {
                        sendError(task.getException().getLocalizedMessage());
                        return;
                    }
                    sendToken(task.getResult());
                }
            );
        call.resolve();
    }

    /**
     * Register twilio for outgoing/incoming calls
     * @param call
     */
    @PluginMethod
    public void registerTwilio(PluginCall call) {
        accessToken = call.getString("accessToken");
        userId = call.getString("userId");
        windowToken = call.getString("windowToken");
        String registrationToken = call.getString("registrationToken");

        Context ctx = this.getActivity().getApplicationContext();

        if (getPermissionState("audio") != PermissionState.GRANTED) {
            requestPermissionForAlias("audio", call, "recordAudioPermsCallback");
        } else {
            Voice.register(accessToken, Voice.RegistrationChannel.FCM, registrationToken, registrationListener);
            call.resolve();
        }
    }

    /**
     * Start outgoing call to applicant
     * @param call
     */
    @PluginMethod
    public void callApplicant(PluginCall call) {
        String applicantId = call.getString("applicantId");
        String applicantName = call.getString("applicantName");
        Context ctx = this.getActivity().getApplicationContext();
        HashMap<String, String> params = new HashMap<>();
        params.put("to", "");
        params.put("userId", userId);
        params.put("applicantId", applicantId);
        params.put("windowToken", windowToken);
        ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken).params(params).build();
        Voice.connect(ctx, connectOptions, callListener);
        JSObject ret = new JSObject();
        call.resolve(ret);
    }

    /**
     * Permission callback when audio permission is not granted
     * @param call
     */
    @PermissionCallback
    private void recordAudioPermsCallback(PluginCall call) {
        String accessToken = call.getString("accessToken");
        String registrationToken = call.getString("registrationToken");
        Context ctx = this.getActivity().getApplicationContext();
        if (getPermissionState("audio") == PermissionState.GRANTED) {
            Voice.register(accessToken, Voice.RegistrationChannel.FCM, registrationToken, registrationListener);
            call.resolve();
        } else {
            call.reject("Audio permission is required");
        }
    }

    @Override
    protected void handleOnNewIntent(Intent data) {
        super.handleOnNewIntent(data);
        Bundle bundle = data.getExtras();
        if (bundle != null && bundle.containsKey("google.message_id")) {
            JSObject notificationJson = new JSObject();
            JSObject dataObject = new JSObject();
            for (String key : bundle.keySet()) {
                if (key.equals("google.message_id")) {
                    notificationJson.put("id", bundle.get(key));
                } else {
                    Object value = bundle.get(key);
                    String valueStr = (value != null) ? value.toString() : null;
                    dataObject.put(key, valueStr);
                }
            }
            notificationJson.put("data", dataObject);
            JSObject actionJson = new JSObject();
            actionJson.put("actionId", "tap");
            actionJson.put("notification", notificationJson);
            notifyListeners("pushNotificationActionPerformed", actionJson, true);
        }
    }

    public void sendToken(String token) {
        JSObject data = new JSObject();
        data.put("value", token);
        notifyListeners(EVENT_TOKEN_CHANGE, data, true);
    }

    public void sendError(String error) {
        JSObject data = new JSObject();
        data.put("error", error);
        notifyListeners(EVENT_TOKEN_ERROR, data, true);
    }

    public static void onNewToken(String newToken) {
        TwilioVoicePlugin pushPlugin = TwilioVoicePlugin.getPushNotificationsInstance();
        if (pushPlugin != null) {
            pushPlugin.sendToken(newToken);
        }
    }

    public static void sendRemoteMessage(RemoteMessage remoteMessage) {
        TwilioVoicePlugin pushPlugin = TwilioVoicePlugin.getPushNotificationsInstance();
        if (pushPlugin != null) {
            pushPlugin.fireNotification(remoteMessage);
        } else {
            lastMessage = remoteMessage;
        }
    }

    public static void handleIncomingCallIntent(Intent intent) {
        TwilioVoicePlugin pushPlugin = TwilioVoicePlugin.getPushNotificationsInstance();
        if (pushPlugin != null) {
            pushPlugin.onIncomingCallIntent(intent);
        }
    }

    public void onIncomingCallIntent(Intent intent) {
        Log.d(TAG, "onIncomingCallIntent");
        CallInvite mCallInvite = intent.getParcelableExtra("INCOMING_CALL_INVITE");
        mCallInvite.accept(getContext(), callListener);
    }

    public void fireNotification(RemoteMessage remoteMessage) {
        JSObject remoteMessageData = new JSObject();

        JSObject data = new JSObject();
        remoteMessageData.put("id", remoteMessage.getMessageId());
        for (String key : remoteMessage.getData().keySet()) {
            Object value = remoteMessage.getData().get(key);
            data.put(key, value);
        }
        remoteMessageData.put("data", data);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            String title = notification.getTitle();
            String body = notification.getBody();
            String[] presentation = getConfig().getArray("presentationOptions");
            if (presentation != null) {
                if (Arrays.asList(presentation).contains("alert")) {
                    Bundle bundle = null;
                    try {
                        ApplicationInfo applicationInfo = getContext()
                            .getPackageManager()
                            .getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
                        bundle = applicationInfo.metaData;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    int pushIcon = android.R.drawable.ic_dialog_info;

                    if (bundle != null && bundle.getInt("com.google.firebase.messaging.default_notification_icon") != 0) {
                        pushIcon = bundle.getInt("com.google.firebase.messaging.default_notification_icon");
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        getContext(),
                        NotificationChannelManager.FOREGROUND_NOTIFICATION_CHANNEL_ID
                    )
                        .setSmallIcon(pushIcon)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    notificationManager.notify(0, builder.build());
                }
            }
            remoteMessageData.put("title", title);
            remoteMessageData.put("body", body);
            remoteMessageData.put("click_action", notification.getClickAction());

            Uri link = notification.getLink();
            if (link != null) {
                remoteMessageData.put("link", link.toString());
            }
        }

        notifyListeners("pushNotificationReceived", remoteMessageData, true);
    }

    private RegistrationListener registrationListener() {
        return new RegistrationListener() {
            @Override
            public void onRegistered(@NonNull String accessToken, @NonNull String fcmToken) {
                Log.d(TAG, "Successfully registered FCM " + fcmToken);
                JSObject data = new JSObject();
                data.put("token", fcmToken);
                notifyListeners("twilioRegistration", data, true);
            }

            @Override
            public void onError(@NonNull RegistrationException error, @NonNull String accessToken, @NonNull String fcmToken) {
                String message = String.format(Locale.US, "Registration Error: %d, %s", error.getErrorCode(), error.getMessage());
                Log.e(TAG, message);
                JSObject data = new JSObject();
                data.put("error", message);
                notifyListeners("twilioRegistrationError", data, true);
            }
        };
    }

    private Call.Listener callListener() {
        return new Call.Listener() {
            /*
             * This callback is emitted once before the Call.Listener.onConnected() callback when
             * the callee is being alerted of a Call. The behavior of this callback is determined by
             * the answerOnBridge flag provided in the Dial verb of your TwiML application
             * associated with this client. If the answerOnBridge flag is false, which is the
             * default, the Call.Listener.onConnected() callback will be emitted immediately after
             * Call.Listener.onRinging(). If the answerOnBridge flag is true, this will cause the
             * call to emit the onConnected callback only after the call is answered.
             * See answeronbridge for more details on how to use it with the Dial TwiML verb. If the
             * twiML response contains a Say verb, then the call will emit the
             * Call.Listener.onConnected callback immediately after Call.Listener.onRinging() is
             * raised, irrespective of the value of answerOnBridge being set to true or false
             */
            @Override
            public void onRinging(@NonNull Call call) {
                Log.d(TAG, "Ringing");
            }

            @Override
            public void onConnectFailure(@NonNull Call call, @NonNull CallException error) {
                Log.d(TAG, "Connect failure");
                String message = String.format(Locale.US, "Call Error: %d, %s", error.getErrorCode(), error.getMessage());
                Log.e(TAG, message);
            }

            @Override
            public void onConnected(@NonNull Call call) {
                Log.d(TAG, "Connected");
            }

            @Override
            public void onReconnecting(@NonNull Call call, @NonNull CallException callException) {
                Log.d(TAG, "onReconnecting");
            }

            @Override
            public void onReconnected(@NonNull Call call) {
                Log.d(TAG, "onReconnected");
            }

            @Override
            public void onDisconnected(@NonNull Call call, CallException error) {
                Log.d(TAG, "Disconnected");
                if (error != null) {
                    String message = String.format(Locale.US, "Call Error: %d, %s", error.getErrorCode(), error.getMessage());
                    Log.e(TAG, message);
                }
            }
        };
    }

    public static TwilioVoicePlugin getPushNotificationsInstance() {
        if (staticBridge != null && staticBridge.getWebView() != null) {
            PluginHandle handle = staticBridge.getPlugin("TwilioVoice");
            if (handle == null) {
                return null;
            }
            return (TwilioVoicePlugin) handle.getInstance();
        }
        return null;
    }
}
