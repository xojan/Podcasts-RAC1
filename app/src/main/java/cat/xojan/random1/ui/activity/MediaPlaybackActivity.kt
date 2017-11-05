package cat.xojan.random1.ui.activity

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.R
import cat.xojan.random1.service.MediaPlaybackService
import kotlinx.android.synthetic.main.activity_media_playback.*




class MediaPlaybackActivity: BaseActivity() {

    private val STATE_PAUSED = 0
    private val STATE_PLAYING = 1

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var currentState: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The usual setContentView, etc
        setContentView(R.layout.activity_media_playback)
        // Now create the MediaBrowserCompat
        mediaBrowser = MediaBrowserCompat(
                this, // a Context
                ComponentName(this, MediaPlaybackService::class.java), // Which MediaBrowserService
                object : MediaBrowserCompat.ConnectionCallback() {

                    override fun onConnected() {
                        try {
                            // This is what gives us access to everything
                            val controller = MediaControllerCompat(this@MediaPlaybackActivity, mediaBrowser.sessionToken)
                            controller.registerCallback(object: MediaControllerCompat.Callback() {

                                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                                    super.onPlaybackStateChanged(state)
                                    if (state == null) {
                                        return
                                    }

                                    when (state.state) {
                                        PlaybackStateCompat.STATE_PLAYING -> {
                                            currentState = STATE_PLAYING
                                        }
                                        PlaybackStateCompat.STATE_PAUSED -> {
                                            currentState = STATE_PAUSED
                                        }
                                    }
                                }
                            })
                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            MediaControllerCompat.setMediaController(
                                    this@MediaPlaybackActivity, controller)
                            MediaControllerCompat.getMediaController(this@MediaPlaybackActivity)
                                    .transportControls.playFromMediaId(R.raw.warner_tautz_off_broadway.toString(), null)
                        } catch (e: RemoteException) {
                            Log.e(MediaPlaybackActivity::class.java.simpleName,
                                    "Error creating controller", e)
                        }

                    }

                    override fun onConnectionSuspended() {
                        // We were connected, but no longer :-(
                    }

                    override fun onConnectionFailed() {
                        // The attempt to connect failed completely.
                        // Check the ComponentName!
                    }
                },
                null) // optional Bundle
        mediaBrowser.connect()

        toggleButton.setOnClickListener {
            val supportMediaController = MediaControllerCompat.getMediaController(this@MediaPlaybackActivity)
            currentState = if( currentState == STATE_PAUSED ) {
                supportMediaController.transportControls.play()
                STATE_PLAYING
            } else {
                if( supportMediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING ) {
                    supportMediaController.transportControls.pause()
                }

                STATE_PAUSED
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val supportMediaController = MediaControllerCompat.getMediaController(this)
        if( supportMediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING ) {
            supportMediaController.transportControls.pause()
        }

        mediaBrowser.disconnect()
    }
}