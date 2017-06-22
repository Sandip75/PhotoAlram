package com.practice.photoalaram;

import java.io.FileNotFoundException;
import java.io.InputStream;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class AlarmScreen extends Activity  {

    public static final String LOG_TAG = "AlarmScreenMonsteR";
    Uri musicUri,imageUri;
    RelativeLayout relativeLayout;
    Bitmap image,scaledImage ;
    InputStream inputStream;
    private ComponentName mComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private static final String description = "Photo Alarm Administrator";
    private int alarm_Id = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate AlarmScreenMonsteR");
		final Alarm alarm = getIntent().getParcelableExtra(AlarmHandler.ALARM_DATA);
        alarm_Id = alarm.id;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imageUri = alarm.image_uri;
		Log.i(LOG_TAG, "music path is :" + musicUri);
		setContentView(R.layout.activity_alarm_screen);
        /* Getting admin permission so that we can lock screen after user has stopped the alarm */
        mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, AlarmAdminReceiver.class);
        Intent adminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        adminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        adminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, description);
        startActivityForResult(adminIntent, 1);
		setImage();
     }

	public void setImage(){
		relativeLayout = (RelativeLayout)findViewById(R.id.relativeLayout);

		try {
			//inputStream = getContentResolver().openInputStream(AlarmAddScreen.imageUri);

			Log.i(LOG_TAG, "downScaling image imageUri is"+ imageUri);
			inputStream = getApplicationContext().getContentResolver().openInputStream(imageUri);
					 if(inputStream == null)
						 Log.i(LOG_TAG,"inputStream is null");
					 image = BitmapFactory.decodeStream(inputStream);
					 if(image == null)
						 Log.i(LOG_TAG,"image is null");
					 WindowManager wm = (WindowManager)getApplicationContext().getSystemService(getApplicationContext().WINDOW_SERVICE);
					 Display dp = wm.getDefaultDisplay();
					 Point p = new Point();
					 dp.getSize(p);
					 scaledImage = Bitmap.createScaledBitmap(image,p.x, p.y, true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(NullPointerException i)
				{
					i.printStackTrace();
				}
		Log.i(LOG_TAG, "setImage");
		BitmapDrawable bmpDrawable = new BitmapDrawable(scaledImage);
		relativeLayout.setBackground(bmpDrawable);
	}

	public void stopAlarm(View view){
		WakeLockUtil.releaseWL();
		Intent mAlarmServiceIntent = new Intent(getApplicationContext(), AlarmService.class);
		stopService(mAlarmServiceIntent);
		Log.i(LOG_TAG, "Alarm id is " + alarm_Id);
		//NotificationManagerCompat.from(context).cancelAll();
		NotificationManagerCompat.from(getApplicationContext()).cancel(alarm_Id);
		finish();
	}


		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.alarm_screen, menu);
			return true;
		}

		class BitmapWorkerClass extends AsyncTask<Integer, Void, Bitmap>{

			@Override
			protected Bitmap doInBackground(Integer... params) {
				// TODO Auto-generated method stub
				return null;
			}

		}

}
