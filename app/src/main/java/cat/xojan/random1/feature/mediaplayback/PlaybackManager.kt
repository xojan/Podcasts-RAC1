package cat.xojan.random1.feature.mediaplayback

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log

class PlaybackManager(val mediaPlayer: MediaPlayer, val queueManager: QueueManager) {

    private val TAG = PlaybackManager::class.simpleName

    val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            Log.d(TAG, "onPlay: ")
            mediaPlayer.start()
        }

        override fun onPause() {
            super.onPause()
            Log.d(TAG, "onPause: ")
            if( mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.d(TAG, "onPlayFromMediaId: " + mediaId)
            mediaPlayer.setDataSource(queueManager.getPodcastUri(mediaId))
            mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
            mediaPlayer.prepareAsync()
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            /*if( COMMAND_EXAMPLE.equalsIgnoreCase(command) ) {
                //Custom command here
            }*/
            Log.d(TAG, "onCommand " + command)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            Log.d(TAG, "onSeekTo " + pos.toString())
        }
    }

    /*private fun successfullyRetrievedAudioFocus(): Boolean {
        val audioManager = mediaPlaybackService.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager.requestAudioFocus(mediaPlaybackService,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    private fun setMediaPlaybackState(state: Int) {
        val playbackstateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        mediaSession.setPlaybackState(playbackstateBuilder.build())
    }*/
}