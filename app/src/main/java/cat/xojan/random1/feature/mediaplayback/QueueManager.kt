package cat.xojan.random1.feature.mediaplayback

import android.support.v4.media.session.MediaSessionCompat

class QueueManager {

    var items: List<MediaSessionCompat.QueueItem> = listOf()

    fun getPodcastUri(mediaId: String?): String {
        val item = items.filter { it -> it.description.mediaId == mediaId }
        return item[0].description.mediaUri.toString()
    }
}