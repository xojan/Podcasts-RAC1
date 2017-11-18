package cat.xojan.random1.ui.mediaplayer

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.R
import cat.xojan.random1.injection.component.DaggerMediaPlaybackComponent
import cat.xojan.random1.service.MediaPlaybackService
import cat.xojan.random1.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_media_playback.*


class MediaPlaybackActivity: BaseActivity() {

    private val STATE_PAUSED = 0
    private val STATE_PLAYING = 1

    private var currentState: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_playback)

        initInjector()
        mediaBrowser = MediaBrowserCompat(this,
                ComponentName(this, MediaPlaybackService::class.java),
                mediaBrowserConnectionCallback,
                null)

        button_play.setOnClickListener {
            val supportMediaController = MediaControllerCompat.getMediaController(this@MediaPlaybackActivity)
            currentState = if( currentState == STATE_PAUSED ) {
                supportMediaController.transportControls.play()
                media_controls.isPressed = true
                STATE_PLAYING
            } else {
                if( supportMediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING ) {
                    supportMediaController.transportControls.pause()
                }
                media_controls.isPressed = false
                STATE_PAUSED
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        val controllerCompat = MediaControllerCompat.getMediaController(this)
        controllerCompat?.unregisterCallback(mediaControllerCallback)
        mediaBrowser.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        val supportMediaController = MediaControllerCompat.getMediaController(this)
        if( supportMediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING ) {
            supportMediaController.transportControls.pause()
        }

        mediaBrowser.disconnect()
    }

    private fun initInjector() {
        val component = DaggerMediaPlaybackComponent.builder()
                .appComponent(applicationComponent)
                .baseActivityModule(activityModule)
                .build()
        component.inject(this)
    }

    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                connectToSession(mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                // TODO handle error
                //hidePlaybackControls()
            }

        }
    }

    @Throws(RemoteException::class)
    private fun connectToSession(token: MediaSessionCompat.Token) {
        val mediaController = MediaControllerCompat(this, token)
        MediaControllerCompat.setMediaController(this, mediaController)

        val root = mediaBrowser.root

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mediaBrowser.unsubscribe(root)

        mediaBrowser.subscribe(root, mediaBrowserSubscriptionCallback)

        // Add MediaController callback so we can redraw the list when metadata changes:
        val controller = MediaControllerCompat.getMediaController(this)
        controller?.registerCallback(mediaControllerCallback)
    }

    // Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media
    // that is ready for playback.
    private val mediaBrowserSubscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(parentId: String,
                                      children: MutableList<MediaBrowserCompat.MediaItem>) {
            if (children.isEmpty()) {
                return
            }
            Log.d("joan", children.toString())
            val firstItem = children[0]
            // Play the first item?
            // Probably should check firstItem.isPlayable()
            MediaControllerCompat
                    .getMediaController(this@MediaPlaybackActivity)
                    .transportControls
                    .playFromMediaId(firstItem.mediaId, null)
        }

        /*override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            if (children == null || children.isEmpty()) {
                return
            }

            val mediaController = MediaControllerCompat.getMediaController(this@MediaPlaybackActivity)
            // Queue up all media items for this simple sample.
            for (mediaItem in children) {
                mediaController!!.addQueueItem(mediaItem.description)
            }

            // Call "playFromMedia" so the UI is updated.
            mediaController!!.transportControls.prepare()
        }*/
    }

    // Callback that ensures that we are showing the controls
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Log.d("joan", "state changed to: " + state)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    currentState = STATE_PLAYING
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    currentState = STATE_PAUSED
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.d("joan", "onMetadataChanged changed to: " + metadata)
        }
    }
}