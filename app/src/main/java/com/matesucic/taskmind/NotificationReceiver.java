package com.matesucic.taskmind;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {


    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isExam = intent.getBooleanExtra("is_exam",false);
        int notificationId = intent.getIntExtra("notification_id",0);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!isExam) {
                channel = new NotificationChannel("task_channel_id", "Task Notifications", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            else {
                channel = new NotificationChannel("exam_channel_id", "Exam Notifications", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder;
        if(!isExam) {
            builder = new NotificationCompat.Builder(context, "task_channel_id");
            builder.setContentText("Check the app for tasks due tomorrow!");
        }
        else {
            builder = new NotificationCompat.Builder(context, "exam_channel_id");
            builder.setContentText("Don't forget to study today for your upcoming exam");
        }
        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.baseline_task_24);
        builder.setAutoCancel(true);
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);
        // Set the content intent for the notification
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(notificationId, builder.build());
    }
}
