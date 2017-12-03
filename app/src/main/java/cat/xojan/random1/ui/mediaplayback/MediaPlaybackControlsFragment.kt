package cat.xojan.random1.ui.mediaplayback

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R

class MediaPlaybackControlsFragment : Fragment() {

    private val TAG = MediaPlaybackControlsFragment::class.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playback_controls, container, false)
    }

    override fun onStart() {
        super.onStart()
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        if (controller != null) {
            onConnected()
        }
    }

    override fun onStop() {
        super.onStop()
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.unregisterCallback(mediaControllerCallback)
    }

    fun onConnected() {
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        if (controller != null) {
            onMetadataChanged(controller.metadata)
            onPlaybackStateChanged(controller.playbackState)
            controller.registerCallback(mediaControllerCallback)
        }
    }

    private fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        Log.d(TAG, "onMetadataChanged " + metadata.toString())
        if (activity == null || metadata == null) {
            return
        }
    }

    private fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        Log.d(TAG, "onPlaybackStateChanged " + state)
        if (activity == null || state == null) {
            return
        }
    }

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Log.d(TAG, "Received playback state change to state " + state.state)
            this@MediaPlaybackControlsFragment.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null) {
                return
            }
            Log.d(TAG, "Received metadata state change to mediaId=" +
                    metadata.description.mediaId + " song=" + metadata.description.title)
            this@MediaPlaybackControlsFragment.onMetadataChanged(metadata)
        }
    }
}