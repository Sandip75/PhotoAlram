package com.practice.photoalaram;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {

    private static final String WAKELOCK = "WakeLock_MonsteR";
    private static PowerManager.WakeLock mWakeLock;

	public static void acquireWL(Context context){
        if(mWakeLock != null) {
            return;
        }
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP , WAKELOCK);
        mWakeLock.acquire();
	}

	public static void releaseWL(){
        if (mWakeLock != null && mWakeLock.isHeld()){
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
