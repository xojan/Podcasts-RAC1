package cat.xojan.random1.feature.mediaplayback;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import javax.inject.Inject;

import cat.xojan.random1.R;
import cat.xojan.random1.domain.model.CrashReporter;
import cat.xojan.random1.domain.model.EventLogger;
import cat.xojan.random1.domain.model.Podcast;
import cat.xojan.random1.injection.component.DaggerRadioPlayerComponent;
import cat.xojan.random1.injection.component.RadioPlayerComponent;
import cat.xojan.random1.feature.BaseActivity;

public class RadioPlayerActivity extends BaseActivity implements MusicPlayerService.Listener {

    public static final String EXTRA_PODCAST = "PODCAST";
    private static final String TAG = RadioPlayerActivity.class.getSimpleName();

    private static final String KEY_BUTTON_DRAWABLE = "key_button_drawable";
    private static final String KEY_PLAYER_STARTED = "key_player_started";
    private static final String KEY_PLAYER_DURATION = "key_player_duration";

    @Inject EventLogger mEventLogger;
    @Inject CrashReporter mCrashReporter;

    private ProgressBar mBufferBar;
    private SeekBar mSeekBar;
    private TextView mTimer;
    private TextView mDuration;
    private ImageView mPlayer;
    private ProgressBar mLoader;

    private int mPlayerDuration = -1;
    private boolean mPlayerStarted = false;
    private int mPlayerButtonDrawable = -1;

    private MusicPlayerService mService;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            MusicPlayerService.RadioPlayerServiceBinder binder =
                    (MusicPlayerService.RadioPlayerServiceBinder) service;
            mService = binder.getServiceInstance();
            mService.registerClient(RadioPlayerActivity.this);
            mBound = true;

            if (mPlayerDuration == -1 && !mPlayerStarted) {
                Log.d(TAG, "StartService");
                startService(mServiceIntent);
                mPlayerStarted = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
    private Intent mServiceIntent;

    private Intent getMusicPlayerIntent(Podcast podcast, Notification notification) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.addCategory(MusicPlayerService.TAG);
        intent.putExtra(MusicPlayerService.EXTRA_URL, podcast.getPath());
        intent.putExtra(MusicPlayerService.EXTRA_FILE_PATH, podcast.getFilePath());
        intent.putExtra(MusicPlayerService.EXTRA_NOTIFICATION, notification);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initInjector();

        if (savedInstanceState != null) {
            mPlayerButtonDrawable = savedInstanceState.getInt(KEY_BUTTON_DRAWABLE);
            mPlayerStarted = savedInstanceState.getBoolean(KEY_PLAYER_STARTED);
            mPlayerDuration = savedInstanceState.getInt(KEY_PLAYER_DURATION);
        }

        Log.d(TAG, "onCreate");
        setContentView(R.layout.radio_player_activity);

        Podcast podcast = getIntent().getParcelableExtra(EXTRA_PODCAST);
        if (podcast == null) {
            mCrashReporter.logException("Podcast cannot be null. Started: " + mPlayerStarted +
                    ", duration: " + mPlayerDuration + ", drawable: " + mPlayerButtonDrawable);
            finish();
        } else {
            findView();
            initView();
            mEventLogger.logPlayedPodcast(podcast);

            // Bind to the service
            Log.d(TAG, "BindService");
            mServiceIntent = getMusicPlayerIntent(podcast, getNotification(podcast.getTitle()));
            bindService(new Intent(this, MusicPlayerService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void findView() {
        mBufferBar = (ProgressBar) findViewById(R.id.buffer_bar);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mTimer = (TextView) findViewById(R.id.timer);
        mDuration = (TextView) findViewById(R.id.duration);
        mPlayer = (ImageView) findViewById(R.id.player);
        mLoader = (ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_BUTTON_DRAWABLE, mPlayerButtonDrawable);
        outState.putBoolean(KEY_PLAYER_STARTED, mPlayerStarted);
        outState.putInt(KEY_PLAYER_DURATION, mPlayerDuration);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            Log.d(TAG, "UnbindService");
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        stopService(mServiceIntent);
        super.onBackPressed();
    }

    private void initView() {
        mBufferBar.setMax(100);
        mBufferBar.setProgress(0);
        mSeekBar.setProgress(0);
        mSeekBar.setMax(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());

        updateViewAfterRotation();
    }

    private void updateViewAfterRotation() {
        if (mPlayerDuration != -1) {
            mLoader.setVisibility(View.GONE);
            mPlayer.setVisibility(View.VISIBLE);
            if (mPlayerButtonDrawable != -1) {
                mPlayer.setImageResource(mPlayerButtonDrawable);
            }
            mPlayer.setOnClickListener(new PlayerButtonClickListener());
            updateDuration(mPlayerDuration);
        }
    }

    private void initInjector() {
        RadioPlayerComponent mComponent = DaggerRadioPlayerComponent.builder()
                .appComponent(getApplicationComponent())
                .baseActivityModule(getActivityModule())
                .build();
        mComponent.inject(this);
    }

    @Override
    public void onMusicPlayerPrepared(int duration) {
        mLoader.setVisibility(View.GONE);
        mPlayer.setVisibility(View.VISIBLE);
        mPlayer.setOnClickListener(new PlayerButtonClickListener());
        updateDuration(duration);
        mPlayerDuration = duration;
    }

    @Override
    public void onProgressUpdate(int progress, int currentDuration) {
        mSeekBar.setProgress(progress);
        mTimer.setText(PlayerUtil.millisToDuration(currentDuration));
    }

    @Override
    public void onBufferProgressUpdate(int percent) {
        mBufferBar.setProgress(percent);
    }

    @Override
    public void logException(Throwable throwable) {
        mCrashReporter.logException(throwable);
    }

    @Override
    public void onMusicPlayerCompletion() {
        mPlayerButtonDrawable = R.drawable.ic_play_arrow;
        mPlayer.setImageResource(mPlayerButtonDrawable);
    }

    @Override
    public void onMusicPlayerPaused() {
        mPlayerButtonDrawable = R.drawable.ic_play_arrow;
        mPlayer.setImageResource(mPlayerButtonDrawable);
    }

    @Override
    public void onMusicPlayerResumed() {
        mPlayerButtonDrawable = R.drawable.ic_pause;
        mPlayer.setImageResource(mPlayerButtonDrawable);
    }

    private void updateDuration(int duration) {
        mDuration.setText(PlayerUtil.millisToDuration(duration));
    }

    private Notification getNotification(String title) {
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        builder.setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(title);

        Intent foregroundIntent = new Intent(getApplicationContext(), RadioPlayerActivity.class);

        foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                foregroundIntent, 0);

        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    private class PlayerButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final Boolean serviceIsPlaying = mService.isPlaying();
            if (serviceIsPlaying == null) {
                onBackPressed();
            } else if (serviceIsPlaying) {
                mService.pause();
                mPlayerButtonDrawable = R.drawable.ic_play_arrow;
                mPlayer.setImageResource(mPlayerButtonDrawable);
            } else {
                mPlayerButtonDrawable = R.drawable.ic_pause;
                mPlayer.setImageResource(mPlayerButtonDrawable);
                mService.start();
            }
        }
    }

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mService.removeCallbacks();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mService.removeCallbacks();
            int totalDuration = mService.getDuration();
            int currentPosition = PlayerUtil.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            mService.seekTo(currentPosition);

            // update timer progress again
            mService.updateSeekBar();
        }
    }
}
