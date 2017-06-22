package com.practice.photoalaram;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;


public class AlarmBroadCastReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = "Receiver_MonsteR";
    public static final String ALARM_DISMISS_ACTION = "Dismiss_Alarm";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onReceive");
        if(intent.getAction() == ALARM_DISMISS_ACTION){
            Intent mAlarmServiceIntent = new Intent(context, AlarmService.class);
            context.stopService(mAlarmServiceIntent);
            final int id = intent.getIntExtra(NotificationHandler.NOTIFICATION_ID, -1);
            Log.i(LOG_TAG, "Alarm id is " + id);
            //NotificationManagerCompat.from(context).cancelAll();
            NotificationManagerCompat.from(context).cancel(id);
            return;
        }
        final byte[] alarmData = intent.getByteArrayExtra(AlarmHandler.ALARM_DATA);
        Alarm mAlarm = null;
        if(alarmData != null){
            Parcel input = Parcel.obtain();
            input.unmarshall(alarmData, 0, alarmData.length);
            input.setDataPosition(0);
            mAlarm = Alarm.CREATOR.createFromParcel(input);
        }else{
            Log.e(LOG_TAG, "Not able to retain alarm details from Parcel");
        }
		WakeLockUtil.acquireWL(context);
		Toast.makeText(context,"Rise and Shine!", Toast.LENGTH_LONG).show();
        KeyguardManager mKeyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean is_Locked = false;
        if(mKeyguardManager.isKeyguardSecure()
                && mKeyguardManager.inKeyguardRestrictedInputMode()){
            Intent alarmScreen= new Intent(context, AlarmScreen.class);
            alarmScreen.putExtra(AlarmHandler.ALARM_DATA, mAlarm);
            alarmScreen.setAction(Intent.ACTION_MAIN);
            alarmScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            context.startActivity(alarmScreen);
            is_Locked = true;
        }

        AlarmHandler mAlarmHandler = new AlarmHandler(context);
        mAlarmHandler.processAlarm(mAlarm);
        //Start service to handle music playback
        Intent playBackIntent = new Intent(context, AlarmService.class);
        playBackIntent.putExtra(AlarmHandler.ALARM_DATA, mAlarm);
        context.startService(playBackIntent);
        NotificationHandler.creatAlarmNotification(context, mAlarm, is_Locked);

	}

}
