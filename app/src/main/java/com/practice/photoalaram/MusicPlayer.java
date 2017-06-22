package com.practice.photoalaram;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MusicPlayer {
	private static final String LOG_TAG = "MusicPlayer_MonsteR";
	MediaPlayer mediaPlayer;
    Context mContext;
    private AudioManager mAudioManager;
    private AudioFocusListener mAudioFocusListener;
    //public static String path = "/sdcard/Numb.mp3";

	public MusicPlayer(Context context) {
		mediaPlayer = new MediaPlayer();
        mContext = context;
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                return true;
            }
        });
	}

    public int requestAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioFocusListener = new AudioFocusListener();
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return status;
    }

    public void abandonAudioFocus(){
        if(mAudioManager == null){
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }


	public void play(Uri uri)
	{

		try {
			mediaPlayer.setDataSource(mContext, uri);
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
			mediaPlayer.prepare();

		} catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Illegal Argument Exception, please check URI passed");
			e.printStackTrace();
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Security Exception");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "Illegal State Exception");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "IO Exception");
			e.printStackTrace();
		}
        if(requestAudioFocus() != AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            mediaPlayer.start();
        }else{
            Log.i(LOG_TAG, "AudioFocus request failed");
        }
	}

	protected void stop() {
        abandonAudioFocus();
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
	}

    private class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener{

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.i(LOG_TAG, "AudioFocus change detected " + focusChange);
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaPlayer.pause();
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mediaPlayer.setVolume(0f, 0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mediaPlayer.setVolume(0.2f, 0.2f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mediaPlayer.start();
                    mediaPlayer.setVolume(1f, 1f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    mediaPlayer.setVolume(1f, 1f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    mediaPlayer.setVolume(0.2f, 0.2f);
                    break;
                default:
                    mediaPlayer.stop();

            }
        }
    }

}
