package com.truckersreport.capacitor.twilio.voice;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.CancelledCallInvite;
import com.twilio.voice.MessageListener;
import com.twilio.voice.Voice;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "VoiceFCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Received onMessageReceived()");
        Log.d(TAG, "Bundle data: " + remoteMessage.getData());
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            boolean valid = Voice.handleMessage(
                this,
                remoteMessage.getData(),
                new MessageListener() {
                    @Override
                    public void onCallInvite(@NonNull CallInvite callInvite) {
                        final int notificationId = (int) System.currentTimeMillis();
                        Log.d(TAG, "onCallInvite");
                        handleInvite(callInvite, notificationId);
                    }

                    @Override
                    public void onCancelledCallInvite(
                        @NonNull CancelledCallInvite cancelledCallInvite,
                        @Nullable CallException callException
                    ) {
                        Log.d(TAG, "onCancelledCallInvite");
                    }
                }
            );

            if (!valid) {
                Log.e(TAG, "The message was not a valid Twilio Voice SDK payload: " + remoteMessage.getData());
            }
        }
        TwilioVoicePlugin.sendRemoteMessage(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        TwilioVoicePlugin.onNewToken(s);
    }

    private void handleInvite(CallInvite callInvite, int notificationId) {
        Intent intent = new Intent(Constants.ACTION_INCOMING_CALL);
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite);
        intent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TwilioVoicePlugin.handleIncomingCallIntent(intent);
    }

    private void handleCanceledCallInvite(CancelledCallInvite cancelledCallInvite) {
        Intent intent = new Intent(this, IncomingCallNotificationService.class);
        intent.setAction(Constants.ACTION_CANCEL_CALL);
        intent.putExtra(Constants.CANCELLED_CALL_INVITE, cancelledCallInvite);

        Log.d(TAG, "Handle Canceled Invite");

        startService(intent);
    }
}
