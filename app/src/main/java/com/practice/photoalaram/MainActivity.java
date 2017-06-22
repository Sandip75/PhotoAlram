package com.practice.photoalaram;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{
	public static final String LOG_TAG = "MainScreen_MonsteR";
    ListView lv;
    AlarmAdapter mAlarmAdapter = null;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader mCursorLoader = DatabaseProvider.getCursorLoader(this);
        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAlarmAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class AlarmAdapter extends CursorAdapter{
        public AlarmAdapter(Context context, Cursor cursor){
            super(context, cursor);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.alarm_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            //Get reference to the view of alarm_row.xml
            TextView time = (TextView) view.findViewById(R.id.alarmtime);
            TextView image = (TextView) view.findViewById(R.id.imageUri);
            TextView music = (TextView) view.findViewById(R.id.musicUri);
            TextView daysOfWeek = (TextView) view.findViewById(R.id.daysOfWeek);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View)v.getParent();
                    RelativeLayout templayout = (RelativeLayout) parentRow.getParent();
                    ListView templv = (ListView) templayout.getParent();
                    final int position = templv.getPositionForView(parentRow);
                    Cursor clickedCursor = (Cursor) lv.getItemAtPosition(position);
                    int rowId = clickedCursor.getInt(Alarm.Columns.ALARM_ID);
                    Toast.makeText(getBaseContext(), "Alarm needed to delete with position " + rowId
                            , Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, "delete button clicked for " + position);
                    deleteAlarm(rowId);
                }
            });

            //Fetch a row from alarm db using cursor
            Alarm alarm = new Alarm(cursor);
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, alarm.hours);
            c.set(Calendar.MINUTE, alarm.minutes);
            String timeSet = AlarmUtils.getFormattedtime(context, c);
            //String timeSet = AlarmUtils.buildTime(alarm.hours, alarm.minutes);
            String musicUri = AlarmUtils.getNameFromUri(alarm.tone_uri, getApplicationContext());
            String imageUri = AlarmUtils.getNameFromUri(alarm.image_uri, getApplicationContext());
            String daysText = alarm.days_of_week.getString();
            //Set the value of layout from the value fetched from Alarm db
            //id.setText(alarmId);
            time.setText(timeSet);
            image.setText(imageUri);
            music.setText(musicUri);
            daysOfWeek.setText(daysText);
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
		setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        lv = (ListView) findViewById(R.id.listView);
        //TODO Use recycler view instead of normal ListView
        mAlarmAdapter = new AlarmAdapter(this, null);
        lv.setAdapter(mAlarmAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_TAG, "id is " + id + " position is " + position);
                Cursor clickedCursor = (Cursor) lv.getItemAtPosition(position);
                editAlarm(clickedCursor);
            }
        });
        this.getLoaderManager().initLoader(0, null, this);
	}

    //function correspoding to add button
	public void createAlarm(){
        Intent activity2= new Intent(this, AlarmAddScreen.class);
        activity2.putExtra("type", "CREATE");
        this.startActivity(activity2);
	}

    private void editAlarm(Cursor cursor){
        Intent activity2= new Intent(this, AlarmAddScreen.class);
        Alarm alarm = new Alarm(cursor);
        activity2.putExtra("type", "EDIT");
        activity2.putExtra(AlarmHandler.ALARM_DATA, alarm);
        this.startActivity(activity2);
    }

    protected void deleteAlarm(int rowId){
        AlarmHandler mAlarmHandler = new AlarmHandler(getApplicationContext());
        mAlarmHandler.deleteAlarm(rowId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
        //ActionBar bar = getActionBar();
        //bar.setBackgroundDrawable(new ColorDrawable(6));
		return super.onCreateOptionsMenu(menu);
	}

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addAlarm:
                createAlarm();
        }
        return super.onOptionsItemSelected(item);
    }
}
