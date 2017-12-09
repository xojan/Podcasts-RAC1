package cat.xojan.random1.feature.mediaplayback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

class QueueManager {

    var items: List<MediaSessionCompat.QueueItem> = listOf()
    lateinit var listener: MetaDataUpdateListener

    fun initListener(mediaPlaybackService: MediaPlaybackService) {
        listener = mediaPlaybackService
    }

    fun getPodcastUri(mediaId: String?): String {
        val item = items.filter { it -> it.description.mediaId == mediaId }
        val itemMediaData = item[0].description

        val metaDataItem = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, itemMediaData.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, itemMediaData.title.toString())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, item[0].queueId + 1)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, items.size.toLong())
                .build()

        listener.onMetadataChanged(metaDataItem)
        return itemMediaData.mediaUri.toString()
    }
}