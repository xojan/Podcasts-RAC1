package cat.xojan.random1.feature.mediaplayback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

interface MetaDataUpdateListener {
    fun updateMetadata(metadata: MediaMetadataCompat?)
    fun updateQueue(title: String, queue: List<MediaSessionCompat.QueueItem>)
    fun updateQueueIndex(mediaId: String)
}