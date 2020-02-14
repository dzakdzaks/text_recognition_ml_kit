package com.dzakdzaks.firebasemessaging;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

public class FirebaseMessagingServices extends FirebaseMessagingService {

    private Intent intent;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            String deviceToken = instanceIdResult.getToken();
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
//                .setContentTitle(remoteMessage.getNotification().getTitle())
//                .setContentText(remoteMessage.getNotification().getBody())
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setStyle(new NotificationCompat.BigTextStyle())
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setAutoCancel(true);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0, notificationBuilder.build());
        sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }

    private static int count = 0;

    //This method is only generating push notification
    private void sendNotification(String messageTitle, String messageBody) {

        intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", messageTitle);
        intent.putExtra("body", messageBody);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, count, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_1")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        /*if(!isAppOnForeground(this)){
            notificationBuilder.setContentIntent(pendingIntent);
        }*/

        Log.d("wololo", "is app active: " +

                isAppOnForeground(this));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = messageTitle;
            String description = messageBody;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel_1", name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        /*Intent intentBrowser = new Intent(Intent.ACTION_VIEW);
        intentBrowser.setData(Uri.parse("http://google.com"));
        PendingIntent pendingIntentLink = PendingIntent.getActivity(this, count, intent, PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder.addAction(0, "Open Link", pendingIntentLink);*/

        notificationManager.notify(count, notificationBuilder.build());
        count++;

        //todo intent
        //todo in progras --> ke activiy in progress
        // kirim sinyal broadcast
       /* Intent broadcastIntent = new Intent(GlobalConfig.UPDATE_ASSIGNMENT_RECEIVER);
        this.sendBroadcast(broadcastIntent);*/
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
