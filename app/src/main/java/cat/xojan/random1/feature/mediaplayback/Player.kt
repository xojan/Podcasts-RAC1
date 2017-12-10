package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

class Player(appContext: Context, private val listener: PlayerListener) {

    private val TAG = Player::class.simpleName
    private val mediaPlayer: MediaPlayer = MediaPlayer()

    init {
        mediaPlayer.setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            //TODO find setaudtioattributes compat
            mediaPlayer.setAudioAttributes(audioAttributes)
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        mediaPlayer.setVolume(1.0f, 1.0f)
        mediaPlayer.setOnCompletionListener { mediaPlayer ->
            mediaPlayer.release()
        }
    }

    fun isPlaying() = mediaPlayer.isPlaying

    fun play(currentMedia: MediaMetadataCompat? = null) {
        if (currentMedia != null) {
            mediaPlayer.reset()
            val mediaUri = currentMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            Log.d(TAG, mediaUri)
            mediaPlayer.setDataSource(mediaUri)
            mediaPlayer.setOnPreparedListener { play() }
            mediaPlayer.prepareAsync()
            listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_BUFFERING)
        } else {
            mediaPlayer.start()
            listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    fun pause() {
        mediaPlayer.pause()
        listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_PAUSED)
    }

    fun getCurrentPosition() = mediaPlayer.currentPosition
    // or PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN

    fun release() {
        /*giveUpAudioFocus();
        unregisterAudioNoisyReceiver();*/
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.release()

        listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_STOPPED)
    }

    fun seekTo(pos: Long) {

    }
}