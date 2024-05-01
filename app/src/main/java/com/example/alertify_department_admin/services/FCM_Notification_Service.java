package com.example.alertify_department_admin.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCM_Notification_Service extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            String title = message.getData().get("title");
            String body = message.getData().get("body");
            String notificationType = message.getData().get("type");
            assert body != null;
            sendNotification(title, body, notificationType);
        }
    }

    private void sendNotification(String title, String messageBody, String notificationType) {

        Intent intent = new Intent(this, MainActivity.class);
        if (messageBody.contains("needs help right now.")) {
            intent.putExtra("notificationFragment", "EmergencyRequestsFragment");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "Your_Channel_ID";
        NotificationCompat.Builder notificationBuilder;

//        if (notificationType.equals("emergency")) {
            notificationBuilder = new NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.emergency).setContentTitle(title).setContentText(messageBody).setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true).setContentIntent(pendingIntent);
//        } else {
//            notificationBuilder = new NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.complaint).setContentTitle(title).setContentText(messageBody).setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true).setContentIntent(pendingIntent);
//        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Emergency Requests", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
