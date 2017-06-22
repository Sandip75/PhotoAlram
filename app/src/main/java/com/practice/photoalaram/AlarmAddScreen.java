package com.practice.photoalaram;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import android.net.Uri;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;


public class AlarmAddScreen extends Activity implements CompoundButton.OnCheckedChangeListener{
    protected static final String LOG_TAG = "AddScreen_MonsteR";
    LinearLayout selectMusicButton,selectImageButton, week;
    TextView alarmTime, imageName, musicName;
    RelativeLayout timePickerLayout;
    Switch repeat;
    public static final int SELECT_PHOTO=15;
    public static final int SELECT_MUSIC=16;
    private int isRepeated;
    private static int hourSet, minSet;
    private Uri imageUri, musicUri;
    AlarmHandler  mAlarmHandler;
    Alarm.DaysOfWeek mDaysOfWeek;
    private CompoundButton[] mCheckBox;
    private int alarmID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.alarm_add_screen);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        selectImageButton = (LinearLayout)findViewById(R.id.imageAddLayout);
        selectMusicButton = (LinearLayout) findViewById(R.id.musicAddLayout);
        alarmTime = (TextView)findViewById(R.id.alarmTime);
        imageName = (TextView)findViewById(R.id.imageName);
        musicName = (TextView)findViewById(R.id.musicName);
        repeat = (Switch)findViewById(R.id.repeatAlarm);
        timePickerLayout =  (RelativeLayout)findViewById(R.id.timePickerlayout);
        week = (LinearLayout) findViewById(R.id.weeklayout);
        mCheckBox = new CompoundButton[7];
        LayoutInflater mInflater = LayoutInflater.from(this);
        for(int i=0; i<7 ;i++) {
            final CompoundButton daysOFWeek = (CompoundButton) mInflater.inflate(R.layout.week_button, week, false);
            String day = Alarm.DaysOfWeek.daysText[i];
            daysOFWeek.setText(day.substring(0,1));
            week.addView(daysOFWeek);
            week.setContentDescription(Alarm.DaysOfWeek.daysText[i]);
            mCheckBox[i] = daysOFWeek;
        }
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if(type.equals("EDIT")){
            Alarm alarm = intent.getParcelableExtra(AlarmHandler.ALARM_DATA);
            hourSet = alarm.hours;
            minSet = alarm.minutes;
            alarmID = alarm.id;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourSet);
            c.set(Calendar.MINUTE, minSet);
            alarmTime.setText(AlarmUtils.getFormattedtime(this, c));
            //alarmTime.setText(AlarmUtils.buildTime(alarm.hours, alarm.minutes));
            musicUri = alarm.tone_uri;
            imageUri = alarm.image_uri;
            musicName.setText(AlarmUtils.getNameFromUri(alarm.tone_uri, getApplicationContext()));
            imageName.setText(AlarmUtils.getNameFromUri(alarm.image_uri, getApplicationContext()));
            week.setVisibility(View.VISIBLE);
            mDaysOfWeek = new Alarm.DaysOfWeek(alarm.days_of_week.getCodedDays());
            Log.i(LOG_TAG, "Editing alarm mdaysofweek is " + mDaysOfWeek.getCodedDays() + " repeat is " + alarm.repeat);
            isRepeated = alarm.repeat;
            if(isRepeated > 0) {
                repeat.setChecked(true);
                week.setVisibility(View.VISIBLE);
                int days = mDaysOfWeek.getCodedDays();
                for (int i = 0; i < 7; i++) {
                    if (((1 << Alarm.DaysOfWeek.daysMapping[i]) & days) > 1) {
                        setWeekButtonState(mCheckBox[i], true);
                    }
                }
            }else{
                week.setVisibility(View.INVISIBLE);
            }
        }else{
            Calendar c = Calendar.getInstance();
            alarmTime.setText(AlarmUtils.getFormattedtime(this, c));
            hourSet = c.get(Calendar.HOUR_OF_DAY);
            minSet = c.get(Calendar.MINUTE);
            week.setVisibility(View.INVISIBLE);
            mDaysOfWeek = new Alarm.DaysOfWeek(0);
        }
        for(int i=0; i<7; i++){
            final int dayIndex = i;
            mCheckBox[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isActivated = mCheckBox[dayIndex].isActivated();
                    if(!isActivated){
                        setWeekButtonState(mCheckBox[dayIndex], true);
                        mDaysOfWeek.set(Alarm.DaysOfWeek.daysMapping[dayIndex], true);
                    }else{
                        setWeekButtonState(mCheckBox[dayIndex], true);
                        mDaysOfWeek.set(Alarm.DaysOfWeek.daysMapping[dayIndex], true);
                    }
                }
            });
        }
        repeat.setOnCheckedChangeListener(this);
        timePickerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(v);
            }
        });
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(v);
            }
        });
        selectMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMusic(v);
            }
        });
        DialogFragment timeDialogFragment = new TimePickerFragment(hourSet, minSet);
        timeDialogFragment.show(getFragmentManager(), "timePicker");
    }


    /*
     * This is called when we click to change time of Alarm
     */
    public void showTimePicker(View v){
        DialogFragment timeDialogFragment = new TimePickerFragment(hourSet, minSet);
        timeDialogFragment.show(getFragmentManager(), "timePicker");
    }


    public void selectImage(View v) {
        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, SELECT_PHOTO);
    }

    public void selectMusic(View v) {
        final Uri currenturi = RingtoneManager.
                getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        Intent musicPickerIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        musicPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        musicPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currenturi);
        musicPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        musicPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(musicPickerIntent, SELECT_MUSIC);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case SELECT_PHOTO:
                if(resultCode== RESULT_OK) {
                    imageUri = data.getData();
                    Log.d(LOG_TAG,"image uri is" + imageUri);
                    InputStream in;
                    String imageUrl = imageUri.toString();
                    if (imageUrl.startsWith("content://com.google.android.apps.photos.content")){
                        try {
                            in = getContentResolver().openInputStream(Uri.parse(imageUrl));
                            bitmap = BitmapFactory.decodeStream(in);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                    String image = AlarmUtils.getNameFromUri(imageUri, getApplicationContext());
                    if(image.length() > 0) {
                        imageName.setText(image);
                    }else{
                        imageUri = null;
                    }
                }
                break;
            case SELECT_MUSIC:
                if(resultCode == RESULT_OK){
                    musicUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if(musicUri != null) {
                        Log.d(LOG_TAG, "music uri is " + musicUri);
                        String music = AlarmUtils.getNameFromUri(musicUri, getApplicationContext());
                        if (music.length() > 0) {
                            musicName.setText(music);
                        } else {
                            musicUri = null;
                        }
                    }
                }
                break;
        }
    }


    /*
   * This function is called when save button is pressed
   */
    public void saveAlarm() {
        Log.i(LOG_TAG, "alarm Save");
        Intent intent = getIntent();
        mAlarmHandler = new AlarmHandler(this);
        if (mDaysOfWeek.getCodedDays() <= 1) {
            isRepeated = 0;
        }
        if(intent.getStringExtra("type").equals("EDIT")){
            mAlarmHandler.updateAlarm(alarmID, hourSet, minSet, musicUri, imageUri,
                    isRepeated, mDaysOfWeek);
        }else{
            mAlarmHandler.saveAlarm(hourSet, minSet, musicUri, imageUri,
                    isRepeated, mDaysOfWeek);
        }
        finish();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(LOG_TAG, "Checked status " + isChecked + " button id is " + buttonView.getId());
        //buttonView.getContentDescription()
        switch (buttonView.getId()) {
            case R.id.repeatAlarm:
                if (isChecked) {
                    week.setVisibility(View.VISIBLE);
                    isRepeated = 1;
                } else {
                    week.setVisibility(View.INVISIBLE);
                    mDaysOfWeek.set(0);
                    resetWeekLayout();
                    isRepeated = 0;
                }
                break;

        }
    }
    private void resetWeekLayout(){
        for(int i=0; i<7; i++){
            setWeekButtonState(mCheckBox[i], false);
        }
    }
    private void setWeekButtonState(CompoundButton checkBox, boolean checked){
        if(checked){
            checkBox.setActivated(true);
            checkBox.setChecked(true);
            checkBox.getBackground().setColorFilter(Color.parseColor("#FF01698C"),
                    PorterDuff.Mode.DARKEN);
        }else{
            checkBox.setActivated(false);
            checkBox.setChecked(false);
            checkBox.getBackground().setColorFilter(Color.parseColor("#FFFFFFFF"),
                    PorterDuff.Mode.DARKEN);
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_alarm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_alarm:
                saveAlarm();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        int hour;
        int minute;
        public TimePickerFragment(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            hourSet = hourOfDay;
            minSet = minute;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minSet);
            /*StringBuilder time = new StringBuilder();
            time.append(hourSet);
            time.append(":");
            time.append(minSet);*/
            alarmTime.setText(AlarmUtils.getFormattedtime(getApplicationContext(), c));
            //alarmTime.setText(AlarmUtils.getAmPm(getApplicationContext(), c));
            Log.i(LOG_TAG,"hour set is " + hourSet + "  min Set is " + minSet);
        }
    }

}
