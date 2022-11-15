package com.truckersreport.capacitor.twilio.voice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import com.getcapacitor.*;
import com.getcapacitor.util.WebColor;
import java.util.Arrays;
import java.util.List;

public class NotificationChannelManager {

    public static final String FOREGROUND_NOTIFICATION_CHANNEL_ID = "PushDefaultForeground";

    private Context context;
    private NotificationManager notificationManager;
    private PluginConfig config;

    public NotificationChannelManager(Context context, NotificationManager manager, PluginConfig config) {
        this.context = context;
        this.notificationManager = manager;
        this.config = config;
        createForegroundNotificationChannel();
    }

    private static String CHANNEL_ID = "id";
    private static String CHANNEL_NAME = "name";
    private static String CHANNEL_DESCRIPTION = "description";
    private static String CHANNEL_IMPORTANCE = "importance";
    private static String CHANNEL_VISIBILITY = "visibility";
    private static String CHANNEL_SOUND = "sound";
    private static String CHANNEL_VIBRATE = "vibration";
    private static String CHANNEL_USE_LIGHTS = "lights";
    private static String CHANNEL_LIGHT_COLOR = "lightColor";

    /**
     * Create notification channel
     */
    public void createForegroundNotificationChannel() {
        // Create the NotificationChannel only if presentationOptions is defined
        // Because the channel can't be changed after creation
        String[] presentation = config.getArray("presentationOptions");
        if (presentation != null) {
            // And only on API 26+ because the NotificationChannel class
            // is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Push Notifications Foreground";
                String description = "Push notifications in foreground";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(FOREGROUND_NOTIFICATION_CHANNEL_ID, name, importance);
                channel.setDescription(description);
                if (Arrays.asList(presentation).contains("sound")) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                    channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
                }
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
