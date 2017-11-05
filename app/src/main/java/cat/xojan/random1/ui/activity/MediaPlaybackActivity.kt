package cat.xojan.random1.ui.activity

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import cat.xojan.random1.service.MediaPlaybackService


class MediaPlaybackActivity: BaseActivity() {

    private lateinit var mMediaBrowser: MediaBrowserCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The usual setContentView, etc
        // Now create the MediaBrowserCompat
        mMediaBrowser = MediaBrowserCompat(
                this, // a Context
                ComponentName(this, MediaPlaybackService::class.java), // Which MediaBrowserService
                object : MediaBrowserCompat.ConnectionCallback() {
                    override fun onConnected() {
                        try {
                            // Ah, here’s our Token again
                            val token = mMediaBrowser.sessionToken
                            // This is what gives us access to everything
                            val controller = MediaControllerCompat(this@MediaPlaybackActivity, token)
                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            MediaControllerCompat.setMediaController(
                                    this@MediaPlaybackActivity, controller)
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
        mMediaBrowser.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaBrowser.disconnect()
    }
}