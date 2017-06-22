package com.practice.photoalaram;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by leo on 10/4/16.
 */
public class AlarmUtils {
    private static final String DT24 = "k:mm" ;
    private static final String DT12 = "h:mm aa" ;
    private static final String AMPM = "aa";

    protected static String buildTime(int hours, int minutes){
        StringBuilder sb1 = new StringBuilder();
        String cursorHour = sb1.append(hours).toString();
        StringBuilder sb2 = new StringBuilder();
        String cursorMin = sb2.append(minutes).toString();
        return new StringBuilder(cursorHour).append(":").append(cursorMin).toString();
    }

    protected static String getNameFromUri(Uri uri, Context context) {
        String scheme = uri.getScheme();
        String fileName = "Default";
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            String[] proj = {MediaStore.Images.Media.TITLE};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                cursor.moveToFirst();
                fileName = cursor.getString(columnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }
    protected static String getFormattedtime(Context context, Calendar mCalendar){
        if(DateFormat.is24HourFormat(context)){
            return (String)DateFormat.format(DT24, mCalendar);
        }else{
            return (String)DateFormat.format(DT12, mCalendar);
        }

    }
    protected static String getAmPm(Context context, Calendar mCalendar){
        if(!DateFormat.is24HourFormat(context)){
            return (String)DateFormat.format(AMPM, mCalendar);
        }else{
            return new String("");
        }
    }

}
