package com.practice.photoalaram;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by leo on 19/4/16.
 */
public final class NotificationHandler {
    public static final String LOG_TAG = "Notification_MonsteR";
    public static final String NOTIFICATION_ID = "notification_id";

    public static void creatAlarmNotification(Context context, Alarm alarm, boolean is_Locked){
        NotificationManagerCompat alarmNotification = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(context);
        mNotificationBuilder.setContentTitle("Alarm NOtification");
        mNotificationBuilder.setContentText(AlarmUtils.buildTime(alarm.hours, alarm.minutes));
        mNotificationBuilder.setSmallIcon(R.drawable.alarm);
        mNotificationBuilder.setAutoCancel(false);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setGroup("PhotoAlarmNotification");
        mNotificationBuilder.setGroupSummary(true);
        mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mNotificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM);
        mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mNotificationBuilder.setLocalOnly(true);
        if(!is_Locked) {
            mNotificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }
        Intent dismissIntent = new Intent(AlarmHandler.ALARM_TRIGGER);
        dismissIntent.setAction(AlarmBroadCastReceiver.ALARM_DISMISS_ACTION);
        dismissIntent.putExtra(NOTIFICATION_ID, alarm.id);

        Log.i(LOG_TAG, "Alarm id passed is " + alarm.id);
        dismissIntent.setClass(context, AlarmBroadCastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarm.id,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action dismissAction = new NotificationCompat.Action(
                                                        R.drawable.ic_launcher,
                                                        "DISMISS_NOW",
                                                        pi);
        mNotificationBuilder.addAction(dismissAction);
        //alarmNotification.cancel(alarm.id);
        alarmNotification.notify(alarm.id, mNotificationBuilder.build());
    }
}
