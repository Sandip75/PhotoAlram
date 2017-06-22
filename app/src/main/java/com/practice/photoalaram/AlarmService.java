package com.practice.photoalaram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AlarmService extends Service {
	private final String LOG_TAG = "AlarmService_MonsteR";
	MusicPlayer musicPlayer;
    Uri musicUri;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");
        if(intent == null){
            Log.e(LOG_TAG, "Empty intent Cannot do anything");
            stopSelf();
            return START_NOT_STICKY;
        }
        Alarm mAlarm = intent.getParcelableExtra(AlarmHandler.ALARM_DATA);
        // TODO Auto-generated method stub
        if(mAlarm == null){
            Log.e(LOG_TAG, "Unable to parse alarm object");
            stopSelf();
        }
        Toast.makeText(this, "Alarm Service is started", Toast.LENGTH_LONG).show();
        musicUri = mAlarm.tone_uri;
        Log.i(LOG_TAG, "music path is :" + musicUri);
        musicPlayer = new MusicPlayer(getApplicationContext());
        musicPlayer.play(musicUri);
        return START_STICKY;
    }

	 public void onDestroy(){
		 musicPlayer.stop();
         super.onDestroy();
	 }

}
