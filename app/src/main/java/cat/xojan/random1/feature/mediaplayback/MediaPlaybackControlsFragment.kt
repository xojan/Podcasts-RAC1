package cat.xojan.random1.feature.mediaplayback

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_playback_controls.*
import java.util.*

class MediaPlaybackControlsFragment : Fragment() {

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
        media_controls_view.setOnClickListener {
            val intent = MediaPlaybackFullScreenActivity.newIntent(activity!!.baseContext)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity?.baseContext?.startActivity(intent)
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
        if (activity == null || metadata == null) {
            return
        }
        title.text = metadata.description.title

        Glide.with(this)
                .load(metadata.description.iconUri.toString() + "?w=" + getWeekOfTheYear())
                .apply(RequestOptions().placeholder(R.drawable.placeholder))
                .into(playback_icon)
    }

    private fun getWeekOfTheYear(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.WEEK_OF_YEAR)
    }

    private fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
        if (activity == null || playbackState == null) {
            return
        }
        val state = playbackState.state
        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
            buffer_icon.visibility = View.GONE
            play_pause.visibility = View.VISIBLE
            play_pause.setImageResource(R.drawable.ic_play_arrow)
        } else if (state == PlaybackStateCompat.STATE_PLAYING) {
            buffer_icon.visibility = View.GONE
            play_pause.visibility = View.VISIBLE
            play_pause.setImageResource(R.drawable.ic_pause)
        } else if (state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
            buffer_icon.visibility = View.VISIBLE
            play_pause.visibility = View.GONE
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
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            this@MediaPlaybackControlsFragment.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null) {
                return
            }
            this@MediaPlaybackControlsFragment.onMetadataChanged(metadata)
        }
    }
}