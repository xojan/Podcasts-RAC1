package cat.xojan.random1.feature.mediaplayback

import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackStateListener {
    fun updatePlaybackState(newState: PlaybackStateCompat)
    fun onPlaybackStart()
    fun onPlaybackStop()
    fun onNotificationRequired()
}