package cat.xojan.random1.feature.mediaplayback;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

public class MusicPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = MusicPlayerService.class.getSimpleName();
    public static final String EXTRA_URL = "remote_url";
    public static final String EXTRA_FILE_PATH = "local_file_path";
    public static final String EXTRA_NOTIFICATION = "top_bar_notification";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private IBinder mBinder;
    private Listener mListener;
    private AudioManager mAudioManager;

    public interface Listener {

        void onMusicPlayerPrepared(int duration);

        void onProgressUpdate(int progress, int currentDuration);

        void onBufferProgressUpdate(int percent);

        void logException(Throwable throwable);

        void onMusicPlayerCompletion();

        void onMusicPlayerPaused();

        void onMusicPlayerResumed();
    }

    public void registerClient(Listener listener) {
        mListener = listener;
    }

    /**
     * For some reason I still don't know, I get some crash reports where
     * the media player is null in this method.
     */
    @Nullable
    public Boolean isPlaying() {
        return mMediaPlayer == null ? null : mMediaPlayer.isPlaying();
    }

    public void pause() {
        if (mMediaPlayer == null) {
            onDestroy();
        }
        mMediaPlayer.pause();
    }

    public void start() {
        mMediaPlayer.start();
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    public void removeCallbacks() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seekTo(int currentPosition) {
        mMediaPlayer.seekTo(currentPosition);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d("onAudioFocusChange()", "resume playback");
                setVolume(1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d("onAudioFocusChange()", "Stop playback but don't release media player");
                pause();
                mListener.onMusicPlayerPaused();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d("onAudioFocusChange()", "keep playing at an attenuated level");
                setVolume(0.1f);
                break;
        }
    }

    public class RadioPlayerServiceBinder extends Binder {
        public MusicPlayerService getServiceInstance(){
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        // The service is being created
        mHandler = new Handler();
        mBinder = new RadioPlayerServiceBinder();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Got audio focus");
        } else {
            Log.d(TAG, "Could not get audio focus");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mListener == null || intent == null || intent.getExtras() == null) {
            stopSelf();
        } else {
            String remoteUrl = intent.getStringExtra(EXTRA_URL);
            String localFilePath = intent.getStringExtra(EXTRA_FILE_PATH);
            Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);

            startForeground(NOTIFICATION_ID, notification);
            startMediaPlayer(remoteUrl, localFilePath);
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder ;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopMediaPlayer();
        stopForeground(true);
        mListener = null;
        mAudioManager.abandonAudioFocus(this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mListener != null) {
            onUnbind(rootIntent);
            onDestroy();
        }
    }

    /**
     * Update timer on seek bar.
     */
    public void updateSeekBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void startMediaPlayer(String fileUrl, String filePath) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            if (filePath != null) {
                Log.d(TAG, "setDataSource: " + filePath);
                FileInputStream fileInputStream;
                fileInputStream = new FileInputStream(filePath);
                mMediaPlayer.setDataSource(fileInputStream.getFD());
                fileInputStream.close();
            } else {
                Log.d(TAG, "setDataSource: " + fileUrl);
                mMediaPlayer.setDataSource(fileUrl);
                mMediaPlayer.setOnBufferingUpdateListener(new BufferingUpdateListener());
            }

            mMediaPlayer.setOnPreparedListener(new MediaPlayerPreparedListener());
            mMediaPlayer.setOnCompletionListener(new MediaPlayerCompletionListener());

            mMediaPlayer.prepareAsync();
            Log.d(TAG, "prepareAsync()");

        } catch (IOException e) {
            if (mListener != null) mListener.logException(e);
        }
    }

    private void stopMediaPlayer() {
        Log.d(TAG, "stopMediaPlayer");
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    private void setVolume(float volume) {
        if (mMediaPlayer == null) {
            onDestroy();
        }
        mMediaPlayer.setVolume(volume, volume);
    }

    /**
     * Background Runnable thread.
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            int currentDuration = mMediaPlayer.getCurrentPosition();
            int progress = PlayerUtil.getProgressPercentage(currentDuration, totalDuration);
            mListener.onProgressUpdate(progress, currentDuration);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private class MediaPlayerPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onMusicPlayerPrepared");
            if (mListener == null) {
                stopSelf();
            } else {
                mListener.onMusicPlayerPrepared(mMediaPlayer.getDuration());
                mMediaPlayer.start();
                updateSeekBar();
            }
        }
    }

    private class BufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mListener.onBufferProgressUpdate(percent);
            }
        }
    }

    private class MediaPlayerCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mListener != null) {
                mListener.onMusicPlayerCompletion();
            }
            mMediaPlayer.seekTo(0);
        }
    }
}
