package com.practice.photoalaram;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.Calendar;

/**
 * Created by leo on 26/3/16.
 **/
public class Alarm implements Parcelable {
    //TODO Replace it with uri of default image


    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(hours);
        dest.writeInt(minutes);
        dest.writeParcelable(tone_uri, flags);
        dest.writeParcelable(image_uri, flags);
        dest.writeString(message);
        dest.writeInt(repeat);
        dest.writeInt(is_enabled);
        dest.writeInt(days_of_week.getCodedDays());
    }


    public static class Columns implements BaseColumns{
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.practice.photoalarm/alarm");
        public static final Uri DEFAULT_IMAGE_URI=
                Uri.parse("android.resource://com.practice.photoalaram/drawable/background_add_acreen");

        //Columns of Alarm database
        public static final String ALARM_HOUR = "hour";
        public static final String ALARM_MINUTES = "minutes";
        public static final String ALARM_TONE = "tone";
        public static final String ALARM_IMAGE = "image";
        public static final String MESSAGE = "message";
        public static final String REPEAT = "repeat";
        public static final String DAYS_OF_WEEK = "daysofweek";
        public static final String IS_ENABLED = "is_enabled";

        //public static final String DEFAULT_SORT_ORDER = ALARM_HOUR+ ", " + ALARM_MINUTES+ " ASC";
        public static final String DEFAULT_SORT_ORDER = _ID + " ASC";

        public static String[] QUERY_COLUMNS = {_ID, ALARM_HOUR, ALARM_MINUTES, ALARM_TONE,
                ALARM_IMAGE, MESSAGE, REPEAT, DAYS_OF_WEEK, IS_ENABLED};

        //ID corresponding to Alarm columns above
        public static final int ALARM_ID = 0;
        public static final int HOUR_ID = 1;
        public static final int MINUTE_ID = 2;
        public static final int TONE_ID = 3;
        public static final int IMAGE_ID = 4;
        public static final int MESSAGE_ID = 5;
        public static final int REPEAT_ID = 6;
        public static final int DAYS_OF_WEEK_ID = 7;
        public static final int IS_ENABLED_ID = 8;

    }
    public int id;
    public int hours;
    public int minutes;
    public Uri tone_uri;
    public Uri image_uri;
    public String message;
    public int repeat;
    public DaysOfWeek days_of_week;
    public int is_enabled;

    Alarm(Cursor c){
        id = c.getInt(Columns.ALARM_ID);
        hours = c.getInt(Columns.HOUR_ID);
        minutes = c.getInt(Columns.MINUTE_ID);
        String toneString = c.getString(Columns.TONE_ID);
        if(toneString != null && toneString.length() != 0) {
            tone_uri = Uri.parse(toneString);
        }else{
            tone_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        String imageString = c.getString(Columns.IMAGE_ID);
        if(imageString != null && imageString.length() != 0)
            image_uri = Uri.parse(imageString);

        message = c.getString(Columns.MESSAGE_ID);
        repeat = c.getInt(Columns.REPEAT_ID);
        days_of_week = new DaysOfWeek(c.getInt(Columns.DAYS_OF_WEEK_ID));
        is_enabled = c.getInt(Columns.IS_ENABLED_ID);
    }
    protected Alarm(Parcel in) {
        id = in.readInt();
        hours = in.readInt();
        minutes = in.readInt();
        tone_uri = in.readParcelable(Uri.class.getClassLoader());
        image_uri = in.readParcelable(Uri.class.getClassLoader());
        message = in.readString();
        repeat = in.readInt();
        is_enabled = in.readInt();
        days_of_week = new DaysOfWeek(in.readInt());
    }

    static final class DaysOfWeek{
        public static int[] daysMapping = new int[]{
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY
        };
        protected static final String[] daysText = {
                "Sun",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat"
        };
        private int days;
        DaysOfWeek(int days){
            this.days = days;
        }

        public void set(int days){
            this.days = days;
        }

        public void set(int day, boolean isChecked){
            if(isChecked){
                this.days = this.days | (1<<day);
            }else{
                this.days = this.days & ~(1<<day);
            }
        }
        public int getCodedDays(){
            return days;
        }
        public int getDays(DaysOfWeek mDaysOfWeek){
            return mDaysOfWeek.days;
        }

        public int getNextAlarm(Calendar c){
            if(days == 0)
                return -1;
            int today = c.get(Calendar.DAY_OF_WEEK);
            int day = today;
            int dayCount = 0;
            for(; dayCount<7 ;dayCount++){
                day = (today + dayCount) % 7;
                if(((1 << day) & days) > 0){
                    break;
                }
            }
            return dayCount;
        }
        public String getString(){
            StringBuilder alarmDaysText = new StringBuilder();
            int daySet = 1;
            for(int i=1; i<=7 ;i++){
                if((days & (1 << i)) > 0){
                    alarmDaysText.append(daysText[i-1]);
                    alarmDaysText.append(" ");
                }
            }
            return alarmDaysText.toString();
        }
    }
}
