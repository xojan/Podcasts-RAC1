package cat.xojan.random1.domain.repository

import android.support.v4.media.MediaDescriptionCompat

interface DownloadPodcastRepository {

    fun addDownloadingPodcast(podcast: MediaDescriptionCompat): Boolean

    fun deleteDownloadingPodcast(podcast: MediaDescriptionCompat): Boolean

    fun setPodcastAsDownloaded(mediaId: String, filePath: String)

    fun getDownloadingPodcasts(): MutableSet<MediaDescriptionCompat>

    fun getDownloadedPodcasts(): Set<MediaDescriptionCompat>

    fun deleteDownloadedPodcast(podcast: MediaDescriptionCompat)

    fun getDownloadedPodcastTitle(mediaId: String): String?
}