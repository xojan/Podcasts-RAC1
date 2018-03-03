package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import cat.xojan.random1.domain.model.EventLogger

class PlaybackManager(appContext: Context,
                      val queueManager: QueueManager,
                      private val listener: PlaybackStateListener,
                      audioManager: AudioManager,
                      eventLogger: EventLogger): PlayerListener {

    companion object {
        const val SET_SLEEP_TIMER = "set_sleep_timer"
        const val SLEEP_TIMER_MILLISECONDS = "sleep_timer_milliseconds"
        const val SLEEP_TIMER_LABEL = "sleep_timer_label"
    }

    private val TAG = PlaybackManager::class.simpleName
    val player = Player(appContext, this, audioManager, eventLogger)

    val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            Log.d(TAG, "onPlay")
            handlePlayRequest()
        }

        override fun onSkipToNext() {
            val nextMediaId = queueManager.getNextMediaId()
            Log.d(TAG, "skipToNext: $nextMediaId")
            nextMediaId?.let {
                handlePlayRequest(nextMediaId)
                queueManager.updateMetadata(nextMediaId)
            }
        }

        override fun onSkipToPrevious() {
            val previousMediaId = queueManager.getPreviousMediaId()
            Log.d(TAG, "skipToPrevious: $previousMediaId")
            previousMediaId?.let {
                handlePlayRequest(previousMediaId)
                queueManager.updateMetadata(previousMediaId)
            }
        }

        override fun onPause() {
            Log.d(TAG, "onPause")
            handlePauseRequest()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.d(TAG, "onPlayFromMediaId: $mediaId")
            queueManager.setQueue(mediaId)
            handlePlayRequest(mediaId)
        }

        override fun onSeekTo(pos: Long) {
            Log.d(TAG, "onSeekTo: $pos")
            player.seekTo(pos)
        }

        override fun onRewind() {
            Log.d(TAG, "onRewind")
            player.rewind()
        }

        override fun onFastForward() {
            Log.d(TAG, "onFastForward")
            player.forward()
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            Log.d(TAG, "onCustomAction: $action")
            when (action) {
                SET_SLEEP_TIMER -> player.setSleepTimer(
                        extras?.getLong(SLEEP_TIMER_MILLISECONDS),
                        extras?.getString(SLEEP_TIMER_LABEL))
            }
        }
    }

    override fun onCompletion() {
        val nextMediaId = queueManager.getNextMediaId()
        if (nextMediaId != null) {
            handlePlayRequest(nextMediaId)
            queueManager.updateMetadata(nextMediaId)
        } else {
            handlePauseRequest()
        }
    }

    override fun onPlaybackStatusChanged(state: Int) {
        if (state != PlaybackStateCompat.STATE_STOPPED) {
            val position: Long = player.getCurrentPosition()
            val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
                    .setActions(getAvailableActions())

            // Set the activeQueueItemId if the current index is valid.
            val currentMediaId = queueManager.getCurrentMediaId()
            if (currentMediaId != -1L) {
                stateBuilder.setActiveQueueItemId(currentMediaId)
            }

            // Set the sleep time if exists
            val bundle = Bundle()
            bundle.putLong(SLEEP_TIMER_MILLISECONDS, player.getTimerMilliseconds())
            bundle.putString(SLEEP_TIMER_LABEL, player.getTimerLabel())
            stateBuilder.setExtras(bundle)

            stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime())
            listener.updatePlaybackState(stateBuilder.build())
        }

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            listener.onNotificationRequired()
        }
    }

    private fun getAvailableActions(): Long {
        var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT

        actions = if (player.isPlaying()) {
            actions or PlaybackStateCompat.ACTION_PAUSE
        } else {
            actions or PlaybackStateCompat.ACTION_PLAY
        }
        return actions
    }

    fun handlePlayRequest(mediaId: String? = null) {
        Log.d(TAG, "handlePlayRequest: mediaId= $mediaId")
        val currentMedia = queueManager.getMediaItem(mediaId)
        listener.onPlaybackStart()
        player.play(currentMedia, hasNext())
    }

    private fun hasNext(): Boolean {
        return queueManager.hasNextOrPrevious() == 1L
    }

    fun handlePauseRequest() {
        Log.d(TAG, "handlePauseRequest")
        player.pause()
        listener.onPlaybackStop()
    }

    fun handleStopRequest() {
        Log.d(TAG, "handleStopRequest")
        player.release()
        listener.onPlaybackStop()
    }
}