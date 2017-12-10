package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.util.Log

class Player(appContext: Context) {

    private val TAG = Player::class.simpleName
    private val mediaPlayer: MediaPlayer = MediaPlayer()

    init {
        mediaPlayer.setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setVolume(1.0f, 1.0f)
        mediaPlayer.setOnCompletionListener {
            mediaPlayer -> mediaPlayer.release()
        }
    }

    fun isPlaying() = mediaPlayer.isPlaying

    fun play(currentMedia: MediaMetadataCompat?) {
        if (currentMedia != null) {
            val mediaUri = currentMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            Log.d(TAG, mediaUri)
            mediaPlayer.setDataSource(mediaUri)
            mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
            mediaPlayer.prepareAsync()
        } else {
            mediaPlayer.start()
        }
    }

    fun pause() {
        mediaPlayer.pause()
    }
}