package cat.xojan.random1.feature.mediaplayback

import android.support.v4.media.MediaMetadataCompat

interface MetaDataUpdateListener {
    fun onMetadataChanged(metadata: MediaMetadataCompat)
}