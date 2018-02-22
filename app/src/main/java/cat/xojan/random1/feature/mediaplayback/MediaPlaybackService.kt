package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.Application
import cat.xojan.random1.domain.model.EventLogger
import cat.xojan.random1.feature.home.ProgramFragment.Companion.MEDIA_ID_ROOT
import cat.xojan.random1.feature.notification.NotificationManager
import javax.inject.Inject


class MediaPlaybackService: MediaBrowserServiceCompat(),
        MetaDataUpdateListener,
        PlaybackStateListener {

    private val TAG = MediaPlaybackService::class.java.simpleName

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private lateinit var playbackManager: PlaybackManager
    private lateinit var audioManager: AudioManager

    @Inject internal lateinit var mediaProvider: MediaProvider
    @Inject internal lateinit var queueManager: QueueManager
    @Inject internal lateinit var eventLogger: EventLogger

    companion object {
        // The action of the incoming Intent indicating that it contains a command
        // to be executed (see {@link #onStartCommand})
        val ACTION_CMD = "cat.xojan.random1.ACTION_CMD"
        // The key in the extras of the incoming Intent indicating the command that
        // should be executed (see {@link #onStartCommand})
        val CMD_NAME = "CMD_NAME"
        // A value of a CMD_NAME key in the extras of the incoming Intent that
        // indicates that the music playback should be paused (see {@link #onStartCommand})
        val CMD_PAUSE = "CMD_PAUSE"
    }

    override fun onCreate() {
        super.onCreate()
        initInjector()

        initPlaybackManager()
        initMediaSession()
        initNotificationController()
        initQueueManager()
    }

    private fun initInjector() {
        (applicationContext as Application).appComponent.inject(this)
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(
                applicationContext,
                MediaPlaybackService::class.java.simpleName,
                null,
                null)

        mediaSession.setCallback(playbackManager.mediaSessionCallback)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)

        sessionToken = mediaSession.sessionToken
    }

    private fun initNotificationController() {
        notificationManager = NotificationManager(this)
    }

    private fun initPlaybackManager() {
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        playbackManager = PlaybackManager(this,
                queueManager,
                this,
                audioManager,
                eventLogger)
    }

    private fun initQueueManager() {
        queueManager.initListener(this)
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        startIntent?.let {
            val action = startIntent.action
            val command = startIntent.getStringExtra(CMD_NAME)
            Log.d(TAG, "action: $action, command: $command")
            if (ACTION_CMD == action && CMD_PAUSE == command) {
                playbackManager.handlePauseRequest()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        // Service is being killed, so make sure we release our resources
        playbackManager.handleStopRequest()
        notificationManager.stopNotification()

        mediaSession.release()
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

    override fun updateMetadata(metadata: MediaMetadataCompat?) {
        metadata?.let { mediaSession.setMetadata(metadata) }
    }

    override fun updateQueue(title: String, queue: List<MediaSessionCompat.QueueItem>) {
        mediaSession.setQueue(queue)
        mediaSession.setQueueTitle(title)
    }

    override fun updateQueueIndex(mediaId: String) {
        playbackManager.handlePlayRequest(mediaId)
    }

    override fun updatePlaybackState(newState: PlaybackStateCompat) {
        Log.d(TAG, "newState: " + newState)
        mediaSession.setPlaybackState(newState)
    }

    override fun onPlaybackStart() {
        mediaSession.isActive = true
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music player will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(Intent(applicationContext, MediaPlaybackService::class.java))
    }

    override fun onPlaybackStop() {
        mediaSession.isActive = false
        stopForeground(true)
    }

    override fun onNotificationRequired() {
        notificationManager.startNotification()
    }
}