package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.CountDownTimer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.domain.model.EventLogger
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.OkHttpClient
import java.io.File

class Player(appContext: Context,
             private val listener: PlayerListener,
             private val audioManager: AudioManager,
             private val eventLogger: EventLogger,
             private val crashReporter: CrashReporter) : AudioManager.OnAudioFocusChangeListener {

    private val TAG = Player::class.simpleName
    private val USER_AGENT = "exoplayer-random1"

    private val exoPlayer: SimpleExoPlayer = SimpleExoPlayer.Builder(appContext).build();

    private val remoteSourceFactory = OkHttpDataSourceFactory(OkHttpClient(), USER_AGENT)
    private val remoteMediaSourceFactory: ProgressiveMediaSource.Factory =
            ProgressiveMediaSource.Factory(remoteSourceFactory)

    private val localSourceFactory = DefaultDataSourceFactory(appContext, USER_AGENT)
    private val localMediaSourceFactory: ProgressiveMediaSource.Factory =
            ProgressiveMediaSource.Factory(localSourceFactory)

    private var countDownTimer: CountDownTimer? = null
    private var timerMilliseconds: Long = 0L
    private var timerLabel: String? = null


    init {
        exoPlayer.addListener(object : com.google.android.exoplayer2.Player.EventListener {
            override fun onSeekProcessed() {}

            override fun onPlayerError(error: ExoPlaybackException) {
                crashReporter.logException(error.toString())
            }

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPositionDiscontinuity(reason: Int) {}

            override fun onRepeatModeChanged(repeatMode: Int) {}

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    com.google.android.exoplayer2.Player.STATE_BUFFERING ->
                        listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_BUFFERING)
                    com.google.android.exoplayer2.Player.STATE_READY -> {
                        notifyListener()
                    }
                    com.google.android.exoplayer2.Player.STATE_ENDED ->
                        listener.onCompletion()
                }
            }

        })
    }

    fun isPlaying() = exoPlayer.playWhenReady

    fun play(currentMedia: MediaMetadataCompat? = null) {
        @Suppress("DEPRECATION")
        val result = audioManager
                .requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        exoPlayer.playWhenReady = true

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (currentMedia != null) {
                val mediaUri: String =
                        currentMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
                Log.d(TAG, mediaUri)
                try {
                    val mediaSource = buildMediaSource(mediaUri)
                    exoPlayer.prepare(mediaSource)
                } catch (e: Exception) {
                    pause()
                }
                eventLogger.logPlayedPodcast(currentMedia)
            } else {
                startPlaying()
            }
        }
    }

    private fun startPlaying() {
        exoPlayer.playWhenReady = true
    }

    private fun buildMediaSource(path: String): MediaSource {
        return if (path.contains("http")) {
            remoteMediaSourceFactory.createMediaSource(Uri.parse(path))
        } else {
            localMediaSourceFactory.createMediaSource(Uri.fromFile(File(path)))
        }
    }

    fun pause() {
        exoPlayer.playWhenReady = false
        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(this)
    }

    fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }

    fun release() {
        exoPlayer.release()

        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(this)
        countDownTimer?.cancel()
    }

    fun seekTo(pos: Long) {
        exoPlayer.seekTo(pos)
    }

    fun rewind() {
        exoPlayer.seekTo(exoPlayer.currentPosition - 30000)
    }

    fun forward() {
        exoPlayer.seekTo(exoPlayer.currentPosition + 30000)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "resume playback")
                exoPlayer.volume = 1.0f
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "Stop playback but don't release media player")
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "keep playing at an attenuated level")
                exoPlayer.volume = 0.1f
            }
        }
    }

    private fun notifyListener() {
        if (isPlaying()) {
            listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_PLAYING)
        } else {
            listener.onPlaybackStatusChanged(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    fun setSleepTimer(milliseconds: Long?, label: String?) {
        milliseconds?.let {
            timerMilliseconds = milliseconds
            timerLabel = label
            if (milliseconds == 0L) {
                countDownTimer?.cancel()
            } else {
                countDownTimer = object : CountDownTimer(milliseconds, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        timerMilliseconds = 0
                        pause()
                    }
                }.start()
            }
            notifyListener()
        }
    }

    fun getTimerMilliseconds(): Long {
        return timerMilliseconds
    }

    fun getTimerLabel(): String? {
        return timerLabel
    }
}