package com.practice.photoalaram;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by parul.jain on 3/25/2015.
 */
public class BootCompleteBroadcast extends BroadcastReceiver {

    public static final String LOG_TAG = "MonsteR:: BootComplete";


    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        AlarmHandler as = new AlarmHandler(context);
        Log.i(LOG_TAG, "on Receive of boot_completed. : " + action);
        Toast.makeText(context, "Alarm received boot completed!!", Toast.LENGTH_LONG).show();
        if (action.equals("android.intent.action.BOOT_COMPLETED")){
            Cursor cursor = DatabaseProvider.getCursor(context.getContentResolver());

            while(cursor.moveToNext()){
                Alarm alarm = new Alarm(cursor);
                as.processAlarm(alarm);
            }
        }
    }
}
