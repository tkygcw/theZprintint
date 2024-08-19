package com.jby.thezprinting.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jby.thezprinting.MainActivity;
import com.jby.thezprinting.R;


import org.json.JSONException;
import org.json.JSONObject;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    private int numMessages = 0;
    JSONObject json;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            try {
                json = new JSONObject(remoteMessage.getData().toString());
                if(!json.getJSONObject("data").getString("image").equals("null")){
//                    sendNotificationWithImage(json.getJSONObject("data"));
                }
                else{
                    sendNotification(json.getJSONObject("data"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onNewToken(String token) {

    }

    private void sendNotification(final JSONObject messageBody) throws JSONException {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("channel_id", messageBody.getString("channel_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = messageBody.getString("channel_id");
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.logo_round)
                .setContentTitle(messageBody.getString("title"))
                .setContentText(messageBody.getString("message"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody.getString("message")))
                .setTicker(messageBody.getString("title"))
                .setSubText("Tap to view the detail.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{Notification.DEFAULT_VIBRATE})
                .setLights(Color.RED, 1000, 1000) //sets the color of the LED , ON-state duration , OFF-state duration
                .setContentIntent(pendingIntent)
                .setNumber(++numMessages)
                .addAction(0, "View", pendingIntent)
                .setVibrate(new long[]{Notification.DEFAULT_VIBRATE})
                .setPriority(Notification.PRIORITY_MAX);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, messageBody.getString("channel_name"), NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        assert notificationManager != null;
        notificationManager.notify(Integer.valueOf(channelId), notificationBuilder.build());
    }
}