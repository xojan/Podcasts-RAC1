package cat.xojan.random1.feature.mediaplayback

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
import kotlinx.android.synthetic.main.fragment_playback_controls.*

class MediaPlaybackControlsFragment : Fragment() {

    private val TAG = MediaPlaybackControlsFragment::class.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playback_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        play_pause.setOnClickListener {
            val controller = MediaControllerCompat.getMediaController(activity as Activity)
            val playbackState = controller.playbackState
            val state = playbackState?.state ?: PlaybackStateCompat.STATE_NONE
            if (state == PlaybackStateCompat.STATE_PAUSED ||
                    state == PlaybackStateCompat.STATE_STOPPED ||
                    state == PlaybackStateCompat.STATE_NONE) {
                playMedia()
            } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                    state == PlaybackStateCompat.STATE_BUFFERING ||
                    state == PlaybackStateCompat.STATE_CONNECTING) {
                pauseMedia()
            }
        }
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
        title.text = metadata.description.title
    }

    private fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
        Log.d(TAG, "onPlaybackStateChanged " + playbackState)
        if (activity == null || playbackState == null) {
            return
        }
        val state = playbackState.state
        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
            play_pause.setImageResource(R.drawable.ic_play_arrow)
        } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
            play_pause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun playMedia() {
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.transportControls?.play()
    }

    private fun pauseMedia() {
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.transportControls?.pause()
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