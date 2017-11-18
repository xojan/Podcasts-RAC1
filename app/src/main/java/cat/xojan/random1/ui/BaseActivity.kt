package cat.xojan.random1.ui

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cat.xojan.random1.Application
import cat.xojan.random1.R
import cat.xojan.random1.injection.component.AppComponent
import cat.xojan.random1.injection.module.BaseActivityModule
import cat.xojan.random1.service.MediaPlaybackService


abstract class BaseActivity : AppCompatActivity() {

    private val TAG = BaseActivity::class.simpleName
    lateinit var mediaBrowser: MediaBrowserCompat

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return [AppComponent]
     */
    protected val applicationComponent: AppComponent
        get() = (application as Application).appComponent

    /**
     * Get an Activity module for dependency injection.
     *
     * @return [cat.xojan.random1.injection.component.BaseActivityComponent]
     */
    protected val activityModule: BaseActivityModule
        get() = BaseActivityModule(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationComponent.inject(this)
        mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, MediaPlaybackService::class.java),
                mediaBrowserConnectionCallback,
                null)
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        /*val controllerCompat = MediaControllerCompat.getMediaController(this)
        controllerCompat?.unregisterCallback(mMediaControllerCallback)*/
        mediaBrowser.disconnect()
    }

    /**
     * Adds a [Fragment] to this activity's layout.
     */
    fun addFragment(fragment: Fragment, tag: String,
                    addToBackSTack: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_fragment, fragment, tag)
        if (addToBackSTack) fragmentTransaction.addToBackStack(tag)
        fragmentTransaction.commit()
    }

    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d(TAG, "media browser connected")
            try {
                connectToSession(mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                Log.e(TAG, "could not connect media controller: " + e)
            }

        }
    }

    @Throws(RemoteException::class)
    private fun connectToSession(token: MediaSessionCompat.Token) {
        val mediaController = MediaControllerCompat(this, token)
        MediaControllerCompat.setMediaController(this, mediaController)
        /*mediaController.registerCallback(mMediaControllerCallback)*/

        onMediaControllerConnected()
    }

    open fun onMediaControllerConnected() {}

    /*private val mMediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Log.d(TAG, "onPlaybackStateChanged: " + state.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.d(TAG, "onMetadataChanged: " + metadata)
        }
    }*/
}