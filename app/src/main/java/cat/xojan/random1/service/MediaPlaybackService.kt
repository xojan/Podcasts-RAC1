package cat.xojan.random1.service

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import cat.xojan.random1.R


class MediaPlaybackService: MediaBrowserServiceCompat() {

    private lateinit var mMediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        mMediaSession = MediaSessionCompat(this, MediaPlaybackService::class.java.simpleName)
        // Make sure to configure your MediaSessionCompat as per
        // https://www.youtube.com/watch?v=FBC1FgWe5X4
        sessionToken = mMediaSession.sessionToken
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // I promise we’ll get to browsing
        result.sendResult(null);
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // Returning null == no one can connect
        // so we’ll return something
        return MediaBrowserServiceCompat.BrowserRoot(
                getString(R.string.app_name), // Name visible in Android Auto
                null) // Bundle of optional extras
    }
}