package cat.xojan.random1.service

import android.app.PendingIntent
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.Application
import cat.xojan.random1.domain.interactor.MediaProvider
import cat.xojan.random1.other.MediaIDHelper.MEDIA_ID_ROOT
import cat.xojan.random1.ui.notification.NotificationController
import javax.inject.Inject


class MediaPlaybackService: MediaBrowserServiceCompat(),  AudioManager.OnAudioFocusChangeListener {

    private val TAG = MediaPlaybackService::class.java.simpleName

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationController: NotificationController

    @Inject internal lateinit var musicProvider: MediaProvider

    override fun onCreate() {
        super.onCreate()
        initInjector()

        initMediaPlayer()
        initMediaSession()
        initNoisyReceiver()
        initNotificationController()
    }

    private fun initInjector() {
        (applicationContext as Application).appComponent.inject(this)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setVolume(1.0f, 1.0f)
        mediaPlayer.setOnCompletionListener {
            mediaPlayer -> mediaPlayer.release()
        }
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        mediaSession = MediaSessionCompat(applicationContext, " MediaPlaybackService",
                mediaButtonReceiver, null)

        mediaSession.setCallback(mediaSessionCallback)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        mediaSession.setMediaButtonReceiver(pendingIntent)

        sessionToken = mediaSession.sessionToken
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
    }

    private fun initNotificationController() {
        notificationController = NotificationController(this, mediaSession)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)
        unregisterReceiver(noisyReceiver)
        mediaSession.release()
        notificationController.release()

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    override fun onLoadChildren(parentId: String, result
    : Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "onloadChildren: " + parentId)
        result.detach()
        musicProvider.retrieveMedia(result, parentId, resources)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        Log.d(TAG, "ongetRoot: " + MEDIA_ID_ROOT)
        return MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, null)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer.setVolume(0.3f, 0.3f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
                mediaPlayer.setVolume(1.0f, 1.0f)
            }
        }
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            if( !successfullyRetrievedAudioFocus() ) {
                return
            }
            mediaSession.isActive = true
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            notificationController.showPlaying()
            mediaPlayer.start()
        }

        override fun onPause() {
            super.onPause()
            if( mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                notificationController.showPaused()
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.d(TAG, "onPlayFromMediaId: " + mediaId)

            /*// Measures bandwidth during playback. Can be null if not required.
            val bandwidthMeter = DefaultBandwidthMeter()
            val audioTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(audioTrackSelectionFactory)
            exoPlayer = ExoPlayerFactory.newSimpleInstance(baseContext, trackSelector)

            // Produces DataSource instances through which media data is loaded.
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(baseContext,
                Util.getUserAgent(baseContext, "yourApplicationName"), bandwidthMeter)
            // Produces Extractor instances for parsing the media data.
            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
            // This is the MediaSource representing the media to be played.
            val uri: Uri? = extras?.getParcelable("mediaUrl")
            val videoSource: MediaSource = ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null)
            // Prepare the player with the source.
            exoPlayer.prepare(videoSource)
            exoPlayer.playWhenReady = true*/
            val uri: Uri? = extras?.getParcelable("mediaUrl")
            mediaPlayer.setDataSource(uri.toString())
            mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
            mediaPlayer.prepareAsync()
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            /*if( COMMAND_EXAMPLE.equalsIgnoreCase(command) ) {
                //Custom command here
            }*/
            Log.d("onCommand", command)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d("onSeekTo", pos.toString())
        }
    }

    private fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    private fun setMediaPlaybackState(state: Int) {
        val playbackstateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        mediaSession.setPlaybackState(playbackstateBuilder.build())
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }
}