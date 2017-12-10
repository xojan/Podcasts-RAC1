package cat.xojan.random1.feature.mediaplayback

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import cat.xojan.random1.Application
import cat.xojan.random1.feature.home.ProgramFragment.Companion.MEDIA_ID_ROOT
import cat.xojan.random1.feature.notification.NotificationController
import java.lang.ref.WeakReference
import javax.inject.Inject


class MediaPlaybackService: MediaBrowserServiceCompat(),
        //AudioManager.OnAudioFocusChangeListener,
        MetaDataUpdateListener,
        PlaybackStateListener{

    private val TAG = MediaPlaybackService::class.java.simpleName

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationController: NotificationController
    private lateinit var playbackManager: PlaybackManager
    private val delayedStopHandler = DelayedStopHandler(this)

    @Inject internal lateinit var mediaProvider: MediaProvider
    @Inject internal lateinit var queueManager: QueueManager

    override fun onCreate() {
        super.onCreate()
        initInjector()

        //initNoisyReceiver()
        initPlaybackManager()
        initMediaSession()
        initNotificationController()
        initQueueManager()
    }

    private fun initInjector() {
        (applicationContext as Application).appComponent.inject(this)
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        mediaSession = MediaSessionCompat(applicationContext, " MediaPlaybackService",
                mediaButtonReceiver, null)

        mediaSession.setCallback(playbackManager.mediaSessionCallback)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        mediaSession.setMediaButtonReceiver(pendingIntent)

        sessionToken = mediaSession.sessionToken
    }

    /*private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
    }*/

    private fun initNotificationController() {
        notificationController = NotificationController(this, mediaSession)
    }

    private fun initPlaybackManager() {
        playbackManager = PlaybackManager(this, queueManager, this)
    }

    private fun initQueueManager() {
        queueManager.initListener(this)
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
        /*val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.abandonAudioFocus(this)*/
        //unregisterReceiver(noisyReceiver)
        mediaSession.release()
        notificationController.release()

       /* if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.release()*/
        mediaProvider.clear()
    }

    override fun onLoadChildren(
            parentId: String,
            result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "onLoadChildren: " + parentId)
        result.detach()
        mediaProvider.retrieveMedia(result, parentId)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        Log.d(TAG, "onGetRoot: " + MEDIA_ID_ROOT)
        return MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, null)
    }

    /*override fun onAudioFocusChange(focusChange: Int) {
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

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }*/

    override fun updateMetadata(metadata: MediaMetadataCompat?) {
        metadata?.let { mediaSession.setMetadata(metadata) }
        /*val state = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,
                        PLAYBACK_POSITION_UNKNOWN,
                        1.0F)
                .build()
        mediaSession.setPlaybackState(state)*/
    }

    override fun updateQueue(title: String, queue: List<MediaSessionCompat.QueueItem>) {
        mediaSession.setQueue(queue)
        mediaSession.setQueueTitle(title)
    }

    override fun updateQueueIndex(queueIndex: Int) {
        // handle play request
    }

    override fun updatePlaybackState() {

    }

    override fun onPlaybackStart() {
        mediaSession.isActive = true
        delayedStopHandler.removeCallbacksAndMessages(null)
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music player will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(Intent(applicationContext, MediaPlaybackService::class.java))
    }

    /**
     * A simple handler that stops the service if player is not active (playing)
     */
    private class DelayedStopHandler constructor(service: MediaPlaybackService) : Handler() {
        private val mWeakReference: WeakReference<MediaPlaybackService> = WeakReference(service)

        override fun handleMessage(msg: Message) {
            val service = mWeakReference.get()
            if (service != null) {
                if (service.playbackManager.player.isPlaying()) {
                    Log.d("DelayedStopHandler", "Ignoring delayed stop since the media player is in use.")
                    return
                }
                Log.d("DelayedStopHandler", "Stopping service with delay handler.")
                service.stopSelf()
            }
        }
    }
}