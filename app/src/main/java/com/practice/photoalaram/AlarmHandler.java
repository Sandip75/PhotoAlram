package com.practice.photoalaram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by parul.jain on 3/27/2015.
 * This class is responsible for interacting with AlarmManager it adds pending into of PhotoAlarm
 * to AlarmManager and cancels it.
 */
public class AlarmHandler {

    public static final String ALARM_DATA = "intent.alarm_data";
    public static final String ALARM_TRIGGER = "com.practice.photoalaram.ALARM_TRIGGER";
    public static final String ALARM_ALERT = "com.practice.photoalarm.ALARM_PLAY";
    //public static final String ALARM_STOP = "com.practice.photoalarm.ALARM_STOP";
    private static final String LOG_TAG = "AlarmHandler_MonsteR";
    Calendar calendar;
    private final Context context;

    public AlarmHandler(Context _context){
       context = _context;
    }

    /*
    * Saves alarm to database and calls processAlarm for further actions.
    */
    public void saveAlarm(int hourSet, int minSet, Uri musicUri, Uri imageUri, int repeat,
                          Alarm.DaysOfWeek daysOfWeek){
        String musicPath = null;
        String imagePath = null;
        Log.i(LOG_TAG, "Saving to db hours " + hourSet + " min " + minSet + " repeat " + repeat +
                " days " + daysOfWeek.getDays(daysOfWeek));
        ContentValues newAlarm = new ContentValues();
        newAlarm.put(Alarm.Columns.ALARM_HOUR, hourSet);
        newAlarm.put(Alarm.Columns.ALARM_MINUTES, minSet);
        if(musicUri == null){
            musicPath= RingtoneManager.getActualDefaultRingtoneUri(
                        context, RingtoneManager.TYPE_ALARM
                        ).toString();
        } else{
            musicPath = musicUri.toString();
        }
        if(imagePath == null){
            imagePath = Alarm.Columns.DEFAULT_IMAGE_URI.toString();
        } else{
            imagePath = imageUri.toString();
        }
        Log.i(LOG_TAG, "musicUri " +  musicPath  + " imagepath " + imagePath);
        newAlarm.put(Alarm.Columns.ALARM_TONE, musicPath);
        newAlarm.put(Alarm.Columns.ALARM_IMAGE, imagePath);
        newAlarm.put(Alarm.Columns.REPEAT, repeat);
        newAlarm.put(Alarm.Columns.DAYS_OF_WEEK, daysOfWeek.getDays(daysOfWeek));
        Uri newAlarmUri = context.getContentResolver().insert(Alarm.Columns.CONTENT_URI, newAlarm);
        processNewAlarm(newAlarmUri);
    }

    /*
    * Update database and calls processAlarm for further actions.
    */
    public void updateAlarm(int alarmId, int hourSet, int minSet, Uri musicUri,
                            Uri imageUri, int repeat, Alarm.DaysOfWeek daysOfWeek){
        //Cancel current Alarm registered to alarm manager.
        cancelAlaram(alarmId);
        String musicPath = musicUri.toString();
        String imagePath = imageUri.toString();
        Log.i(LOG_TAG, "Saving to db hours " + hourSet + " min " + minSet + " repeat " + repeat);
        Log.i(LOG_TAG, "musicUri " +  musicPath  + " imagepath " + imagePath);
        ContentValues newAlarm = new ContentValues();
        newAlarm.put(Alarm.Columns.ALARM_HOUR, hourSet);
        newAlarm.put(Alarm.Columns.ALARM_MINUTES, minSet);
        if(musicPath == null){
            musicPath= RingtoneManager.getActualDefaultRingtoneUri(
                    context, RingtoneManager.TYPE_ALARM
            ).toString();
        }
        newAlarm.put(Alarm.Columns.ALARM_TONE, musicPath);
        if(imagePath == null){
            imagePath = Alarm.Columns.DEFAULT_IMAGE_URI.toString();
        }
        newAlarm.put(Alarm.Columns.ALARM_IMAGE, imagePath);
        newAlarm.put(Alarm.Columns.REPEAT, repeat);
        newAlarm.put(Alarm.Columns.DAYS_OF_WEEK, daysOfWeek.getDays(daysOfWeek));
        Uri alarmUri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        context.getContentResolver().update(alarmUri, newAlarm, null, null);
        processNewAlarm(alarmUri);
    }

    public void deleteAlarm(int alarmId){
        cancelAlaram(alarmId);
        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        context.getContentResolver().delete(uri, null, null);
    }


    /*
    * Process the new Alarm after writing to database
    */
    private void processNewAlarm(Uri alarmUri){
        String segment = alarmUri.getPathSegments().get(1);
        int newId = Integer.parseInt(segment);
        Log.i(LOG_TAG, "New Alarm set URI is " + alarmUri + "id is " + newId);
        Alarm alarmDetails = fetchAlarmWithID(newId);
        Log.i(LOG_TAG, "Alarm fetched from db id " + alarmDetails.id + " hours " +
                alarmDetails.hours + " minutes " + alarmDetails.minutes);
        if(alarmDetails != null){
            long timeInMs = calculateNextAlarm(alarmDetails.hours, alarmDetails.minutes,
                    alarmDetails.days_of_week);
            setNextAlarm(timeInMs, alarmDetails);
        }else{
            Log.i(LOG_TAG, "Alarm details fetched from database is NULL");
        }
    }

    /*
    * Called after reboot of device to set all alarms again.
    */
    protected void processAlarm(Alarm alarm){
        long timeInMs = calculateNextAlarm(alarm.hours, alarm.minutes, alarm.days_of_week);
        Log.i(LOG_TAG, "processAlarm timeInMs " + timeInMs + " hour " + alarm.hours + " minutes "
                + alarm.minutes);
        setNextAlarm(timeInMs, alarm);
    }
    
    private Alarm fetchAlarmWithID(int alarmId){
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor cursor = mContentResolver.query(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI,
                alarmId), Alarm.Columns.QUERY_COLUMNS, null, null, null);
        Alarm mAlarm = null;
        if(cursor != null){
            if(cursor.moveToFirst()){
                mAlarm = new Alarm(cursor);
            }
            cursor.close();
        }else{
            Log.e(LOG_TAG, "Not able to fetch alarm with id " + alarmId);
        }
        return mAlarm;
    }

    public long calculateNextAlarm(int hour, int mins, Alarm.DaysOfWeek daysOfWeek){
        ContentResolver mContentResolver = context.getContentResolver();
        Log.i(LOG_TAG, "alarm Setup hour = " + hour + "  min = " + mins);
        calendar = Calendar.getInstance();
        //int currentTime = calendar.get(Calendar.HOUR_OF_DAY), currentMin = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE));
        long currentTimeMs = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, mins);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timeToSetMs = calendar.getTimeInMillis();

        if(timeToSetMs < currentTimeMs){
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Log.i(LOG_TAG, " timetoSetMs 1 = " + timeToSetMs + " currentTimeMs = " + currentTimeMs);
        }
        int addDays = daysOfWeek.getNextAlarm(calendar);
        if(addDays>0) calendar.add(Calendar.DAY_OF_YEAR, addDays);
        Log.i(LOG_TAG, " timetoSetMs = " + timeToSetMs  + " addDays " + addDays);
        Log.i(LOG_TAG, "hour is " + calendar.get(Calendar.HOUR_OF_DAY));
        return calendar.getTimeInMillis();
    }

    public void setNextAlarm(final long timeToSet, final Alarm mAlarm){
        Parcel alarmParcel = Parcel.obtain();
        mAlarm.writeToParcel(alarmParcel, 0);
        alarmParcel.setDataPosition(0);
        Intent intent = new Intent(ALARM_TRIGGER);
        intent.putExtra(ALARM_DATA, alarmParcel.marshall());
        PendingIntent pi = PendingIntent.getBroadcast(context, mAlarm.id, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToSet, pi);
    }

    public void cancelAlaram(final int alarmId){
        Alarm mAlarm = fetchAlarmWithID(alarmId);
        Parcel alarmParcel = Parcel.obtain();
        mAlarm.writeToParcel(alarmParcel, 0);
        alarmParcel.setDataPosition(0);
        Intent intent = new Intent(ALARM_TRIGGER);
        intent.putExtra(ALARM_DATA, alarmParcel.marshall());
        PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        //pi.cancel();
        AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
        alarmManager.cancel(pi);
        Toast.makeText(context, "Alarm cancelled!!", Toast.LENGTH_LONG).show();
    }

}
