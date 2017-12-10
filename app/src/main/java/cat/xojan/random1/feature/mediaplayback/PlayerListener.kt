package cat.xojan.random1.feature.mediaplayback

interface PlayerListener {
    /**
     * On current music completed.
     */
    fun onCompletion()

    /**
     * on Playback status changed
     * Implementations can use this callback to update
     * playback state on the media sessions.
     */
    fun onPlaybackStatusChanged(state: Int)

    /**
     * @param error to be added to the PlaybackState
     */
    fun onError(error: String)
}