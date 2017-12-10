package cat.xojan.random1.feature.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.R
import cat.xojan.random1.feature.mediaplayback.MediaPlaybackFullScreenActivity
import cat.xojan.random1.feature.mediaplayback.MediaPlaybackService


class NotificationManager(private val service: MediaPlaybackService,
                          private val mediaSession: MediaSessionCompat): BroadcastReceiver() {

    private val NOTIFICATION_ID = 683
    private val REQUEST_CODE = 100
    private val CHANNEL_ID = "cat.xojan.random1.feature.notification.MUSIC_CHANNEL_ID"

    private val ACTION_PAUSE = "cat.xojan.random1.feature.notification.pause"
    private val ACTION_PLAY = "cat.xojan.random1.feature.notification.play"
    private val ACTION_PREV = "cat.xojan.random1.feature.notification.prev"
    private val ACTION_NEXT = "cat.xojan.random1.feature.notification.next"
    private val ACTION_STOP = "cat.xojan.random1.feature.notification.stop"

    private val TAG = NotificationManager::class.simpleName

    private val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
    private val previousIntent:PendingIntent
    private val nextIntent:PendingIntent
    private val pauseIntent:PendingIntent
    private val playIntent:PendingIntent
    private val stopIntent:PendingIntent

    init {
        val pkg = service.packageName
        previousIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
        nextIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
        pauseIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
        playIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
        stopIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "Received intent with action " + action!!)
        val transportControls = mediaSession.controller.transportControls
        when (action) {
            ACTION_PAUSE -> transportControls.pause()
            ACTION_PLAY -> transportControls.play()
            ACTION_NEXT -> transportControls.skipToNext()
            ACTION_PREV -> transportControls.skipToPrevious()
            else -> Log.w(TAG, "Unknown intent ignored. Action= " + action)
        }
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before [.stopNotification] is called.
     */
    fun startNotification() {
        val notification = createNotification()

        val filter = IntentFilter()
        filter.addAction(ACTION_NEXT)
        filter.addAction(ACTION_PAUSE)
        filter.addAction(ACTION_PLAY)
        filter.addAction(ACTION_PREV)

        service.registerReceiver(this, filter)
        service.startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification? {
        Log.d(TAG, "createNotification")

        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata
        val description = mediaMetadata.description

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationBuilder = NotificationCompat.Builder(service, CHANNEL_ID)

        val playPauseButtonPosition = addActions(notificationBuilder, controller.playbackState)
        notificationBuilder
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // show only play/pause in compact view
                        .setShowActionsInCompactView(playPauseButtonPosition)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopIntent)
                        .setMediaSession(mediaSession.sessionToken))
                .setDeleteIntent(stopIntent)
                // TODO? .setColor(mNotificationColor)
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(service.getString(R.string.app_name))
                .setContentText(description.title)
                .setLargeIcon(BitmapFactory.decodeResource(service.resources,
                        R.drawable.default_rac1))

        setNotificationPlaybackState(notificationBuilder, controller.playbackState)
        //TODO ? fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder)

        return notificationBuilder.build()
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    service.getString(R.string.notification_channel),
                    NotificationManager.IMPORTANCE_LOW)

            notificationChannel.description = service.getString(R.string.notification_channel_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun addActions(notificationBuilder: NotificationCompat.Builder,
                           playbackState: PlaybackStateCompat): Int {
        Log.d(TAG, "updatePlayPauseAction")

        var playPauseButtonPosition = 0
        // If skip to previous action is enabled
        if (playbackState.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
            notificationBuilder.addAction(android.R.drawable.ic_media_previous,
                    service.getString(R.string.label_previous), previousIntent)

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1
        }

        // Play or pause button, depending on the current state.
        val label: String
        val icon: Int
        val intent: PendingIntent
        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            label = service.getString(R.string.label_pause)
            icon = android.R.drawable.ic_media_pause
            intent = pauseIntent
        } else {
            label = service.getString(R.string.label_play)
            icon = android.R.drawable.ic_media_play
            intent = playIntent
        }
        notificationBuilder.addAction(NotificationCompat.Action(icon, label, intent))

        // If skip to next action is enabled
        if (playbackState.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
            notificationBuilder.addAction(android.R.drawable.ic_media_next,
                    service.getString(R.string.label_next), nextIntent)
        }

        return playPauseButtonPosition
    }

    private fun createContentIntent(description: MediaDescriptionCompat?): PendingIntent {
        val openUI = Intent(service, MediaPlaybackFullScreenActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openUI.putExtra(MediaPlaybackFullScreenActivity.EXTRA_START_FULLSCREEN, true)
        if (description != null) {
            openUI.putExtra(MediaPlaybackFullScreenActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description)
        }
        return PendingIntent.getActivity(service, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder,
                                             playbackState: PlaybackStateCompat) {
        Log.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + playbackState)

        if (playbackState == null) {
            // TODO
            Log.d(TAG, "updateNotificationPlaybackState. cancelling notification!")
            service.stopForeground(true)
            return
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(playbackState.state == PlaybackStateCompat.STATE_PLAYING)
    }

    fun stopNotification() {
        try {
            notificationManager.cancel(NOTIFICATION_ID)
            service.unregisterReceiver(this)
        } catch (ex: IllegalArgumentException) {
            // ignore if the receiver is not registered.
        }

        service.stopForeground(true)
    }
}