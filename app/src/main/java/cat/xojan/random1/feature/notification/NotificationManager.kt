package cat.xojan.random1.feature.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.RemoteException
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.R
import cat.xojan.random1.feature.mediaplayback.MediaPlaybackFullScreenActivity
import cat.xojan.random1.feature.mediaplayback.MediaPlaybackService
import cat.xojan.random1.feature.mediaplayback.QueueManager.Companion.METADATA_HAS_NEXT_OR_PREVIOUS
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class NotificationManager(private val service: MediaPlaybackService): BroadcastReceiver() {

    private val NOTIFICATION_ID = 683
    private val REQUEST_CODE = 100
    private val CHANNEL_ID = "cat.xojan.random1.feature.notification.MUSIC_CHANNEL_ID"

    private val ACTION_PAUSE = "cat.xojan.random1.feature.notification.pause"
    private val ACTION_PLAY = "cat.xojan.random1.feature.notification.play"
    private val ACTION_PREV = "cat.xojan.random1.feature.notification.prev"
    private val ACTION_NEXT = "cat.xojan.random1.feature.notification.next"
    private val ACTION_STOP = "cat.xojan.random1.feature.notification.stop"
    private val ACTION_FORWARD = "cat.xojan.random1.feature.notification.forward"
    private val ACTION_REWIND = "cat.xojan.random1.feature.notification.rewind"

    private val TAG = NotificationManager::class.simpleName

    private val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
    private val previousIntent:PendingIntent
    private val nextIntent:PendingIntent
    private val pauseIntent:PendingIntent
    private val playIntent:PendingIntent
    private val stopIntent:PendingIntent
    private val forwardIntent:PendingIntent
    private val rewindIntent:PendingIntent

    private var sessionToken: MediaSessionCompat.Token? = null
    private var mController: MediaControllerCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null
    private var mStarted = false

    private var mPlaybackState: PlaybackStateCompat? = null
    private var mMetadata: MediaMetadataCompat? = null

    init {
        updateMediaSessionToken()
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
        forwardIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_FORWARD).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
        rewindIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                Intent(ACTION_REWIND).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)

        notificationManager.cancelAll()
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private fun updateMediaSessionToken() {
        val freshToken = service.sessionToken
        if (sessionToken == null && freshToken != null || sessionToken != null
                && sessionToken != freshToken) {
            if (mController != null) {
                mController!!.unregisterCallback(mCb)
            }
            sessionToken = freshToken
            if (sessionToken != null) {
                mController = MediaControllerCompat(service, sessionToken!!)
                mTransportControls = mController!!.transportControls
                if (mStarted) {
                    mController!!.registerCallback(mCb)
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "Received intent with action " + action!!)
        when (action) {
            ACTION_PAUSE -> mTransportControls?.pause()
            ACTION_PLAY -> mTransportControls?.play()
            ACTION_NEXT -> mTransportControls?.skipToNext()
            ACTION_PREV -> mTransportControls?.skipToPrevious()
            ACTION_FORWARD -> mTransportControls?.fastForward()
            ACTION_REWIND -> mTransportControls?.rewind()
            else -> Log.w(TAG, "Unknown intent ignored. Action= " + action)
        }
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before [.stopNotification] is called.
     */
    fun startNotification() {
        if (!mStarted) {
            mStarted = true
            Log.d(TAG, "startNotification")

            mMetadata = mController?.metadata
            mPlaybackState = mController?.playbackState

            val notification = createNotification()
            if (notification != null) {
                mController?.registerCallback(mCb)
                val filter = IntentFilter()
                filter.addAction(ACTION_NEXT)
                filter.addAction(ACTION_PAUSE)
                filter.addAction(ACTION_PLAY)
                filter.addAction(ACTION_PREV)
                filter.addAction(ACTION_REWIND)
                filter.addAction(ACTION_FORWARD)
                service.registerReceiver(this, filter)
                service.startForeground(NOTIFICATION_ID, notification)
            }
        }
    }

    fun stopNotification() {
        if (mStarted) {
            Log.d(TAG, "stopNotification")
            mStarted = false
            mController?.unregisterCallback(mCb)
            try {
                notificationManager.cancel(NOTIFICATION_ID)
                service.unregisterReceiver(this)
            } catch (ex: IllegalArgumentException) {
                // ignore if the receiver is not registered.
            }
            service.stopForeground(true)
        }
    }

    private fun createNotification(): Notification? {
        Log.d(TAG, "createNotification")
        if (mMetadata == null || mPlaybackState == null) {
            return null
        }
        val description = mMetadata?.description

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationBuilder = NotificationCompat.Builder(service, CHANNEL_ID)
        addActions(notificationBuilder)

        val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopIntent)
                .setMediaSession(sessionToken)

        if (mController?.metadata?.getLong(METADATA_HAS_NEXT_OR_PREVIOUS) == 1L) {
            style.setShowActionsInCompactView(1, 2, 3)
        } else {
            style.setShowActionsInCompactView(0, 1, 2)
        }

        notificationBuilder
                .setStyle(style)
                .setDeleteIntent(stopIntent)
                .setSmallIcon(R.mipmap.ic_notification)
                .setColorized(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(service.getString(R.string.app_name))
                .setContentText(description?.title)

        setNotificationPlaybackState(notificationBuilder)

        val placeholder = BitmapFactory.decodeResource(service.resources, R.drawable.placeholder)
        Picasso.with(service)
                .load(description?.iconUri)
                .into(object: Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    }

                    override fun onBitmapFailed(errorDrawable: Drawable?) {
                        notificationBuilder.setLargeIcon(placeholder)
                        setNotificationPlaybackState(notificationBuilder)
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        notificationBuilder.setLargeIcon(bitmap)
                        setNotificationPlaybackState(notificationBuilder)
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                    }

                })

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

    private fun addActions(notificationBuilder: NotificationCompat.Builder) {
        Log.d(TAG, "addActions")
        notificationBuilder.mActions.clear()

        notificationBuilder.addAction(R.drawable.ic_replay_30_white_24px,
                service.getString(R.string.label_rewind), rewindIntent)

        if (mController?.metadata?.getLong(METADATA_HAS_NEXT_OR_PREVIOUS) == 1L) {
            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24px,
                    service.getString(R.string.label_previous), previousIntent)
        }

        val label: String
        val icon: Int
        val intent: PendingIntent
        if (mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING) {
            label = service.getString(R.string.label_pause)
            icon = R.drawable.ic_pause
            intent = pauseIntent
        } else {
            label = service.getString(R.string.label_play)
            icon = R.drawable.ic_play_arrow
            intent = playIntent
        }
        notificationBuilder.addAction(NotificationCompat.Action(icon, label, intent))

        if (mController?.metadata?.getLong(METADATA_HAS_NEXT_OR_PREVIOUS) == 1L) {
            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24px,
                    service.getString(R.string.label_next), nextIntent)
        }

        notificationBuilder.addAction(R.drawable.ic_forward_30_white_24px,
                service.getString(R.string.label_forward), forwardIntent)
    }

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(service, MediaPlaybackFullScreenActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(service, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        Log.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + mPlaybackState)
        if (mPlaybackState == null || !mStarted) {
            Log.d(TAG, "updateNotificationPlaybackState. cancelling notification!")
            service.stopForeground(true)
            return
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING)
    }

    private val mCb = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            mPlaybackState = state
            Log.d(TAG, "Received new playback state: " + state)
            if (state!!.state == PlaybackStateCompat.STATE_STOPPED ||
                    state.state == PlaybackStateCompat.STATE_NONE) {
                stopNotification()
            } else {
                val notification = createNotification()
                if (notification != null) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mMetadata = metadata
            Log.d(TAG, "Received new metadata: " + metadata)
            val notification = createNotification()
            if (notification != null) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            Log.d(TAG, "Session was destroyed, resetting to the new session token")
            try {
                updateMediaSessionToken()
            } catch (e: RemoteException) {
                Log.e(TAG, "could not connect media controller ")
            }

        }
    }
}