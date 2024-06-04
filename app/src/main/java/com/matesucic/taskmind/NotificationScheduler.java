package com.matesucic.taskmind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class NotificationScheduler {

    public static void scheduleNotification(Context context, int id, Calendar notifyDate, boolean isExam) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.matesucic.taskmind.NOTIFICATION_ACTION");
        intent.putExtra("is_exam", isExam);
        intent.putExtra("notification_id", id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to trigger on the specified date
        long notifyTime = notifyDate.getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
            }
            else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
            }
        }
        else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent);
        }
    }

    public static boolean isNotificationScheduled(Context context, int notificationId) {
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        return pendingIntent != null;
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

}
