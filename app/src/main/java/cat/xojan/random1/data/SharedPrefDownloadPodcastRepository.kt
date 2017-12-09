package cat.xojan.random1.data

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.media.MediaDescriptionCompat
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_FILE_PATH
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_STATE
import cat.xojan.random1.domain.repository.DownloadPodcastRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import com.squareup.moshi.Types
import java.util.*

class SharedPrefDownloadPodcastRepository(context: Context) : DownloadPodcastRepository {

    private val DOWNLOAD_PODCASTS = "dowload_media_repo"
    private val DOWNLOADING_PODCASTS = "downloading_media"
    private val DOWNLOADED_PODCASTS = "downloaded_media"

    private val sharedPref: SharedPreferences
    private val jsonAdapter: JsonAdapter<MutableSet<MediaDescriptionCompat>>

    init {
        sharedPref = context.getSharedPreferences(DOWNLOAD_PODCASTS, Context.MODE_PRIVATE)
        val type = Types.newParameterizedType(MutableSet::class.java,
                MediaDescriptionCompat::class.java)
        val moshi = Moshi.Builder()
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .add(MediaDescriptionCompatJsonAdapter())
                .build()
        jsonAdapter = moshi.adapter(type)
    }

    override fun addDownloadingPodcast(podcast: MediaDescriptionCompat): Boolean {
        podcast.extras?.putSerializable(PODCAST_STATE, Podcast.State.DOWNLOADING)
        val podcasts = getDownloadingPodcasts()
        podcasts.add(podcast)
        return sharedPref.edit()
                .putString(DOWNLOADING_PODCASTS, setToJson(podcasts))
                .commit()
    }

    override fun deleteDownloadingPodcast(podcast: MediaDescriptionCompat): Boolean {
        val podcasts = getDownloadingPodcasts()
        podcasts.removeAll { it.mediaId == podcast.mediaId }
        return sharedPref.edit()
                .putString(DOWNLOADING_PODCASTS, setToJson(podcasts))
                .commit()
    }

    override fun setPodcastAsDownloaded(mediaId: String, filePath: String) {
        val podcast = getDownloadingPodcast(mediaId)
        if (podcast != null && deleteDownloadingPodcast(podcast)) {
            podcast.extras?.putString(PODCAST_FILE_PATH, filePath)
            podcast.extras?.putSerializable(PODCAST_STATE, Podcast.State.DOWNLOADED)
            addDownloadedPodcast(podcast)
        }
    }

    override fun getDownloadingPodcasts(): MutableSet<MediaDescriptionCompat> {
        val podcastsJson = sharedPref.getString(DOWNLOADING_PODCASTS, null)
        return jsonToSet(podcastsJson)
    }

    override fun getDownloadedPodcasts(): MutableSet<MediaDescriptionCompat> {
        val podcastsJson = sharedPref.getString(DOWNLOADED_PODCASTS, null)
        return jsonToSet(podcastsJson)
    }

    override fun deleteDownloadedPodcast(podcast: MediaDescriptionCompat) {
        val podcasts = getDownloadedPodcasts()
        podcasts.removeAll{it.mediaId == podcast.mediaId}
        sharedPref.edit()
                .putString(DOWNLOADED_PODCASTS, setToJson(podcasts))
                .apply()
    }

    override fun getDownloadedPodcastTitle(mediaId: String): String? {
        return getDownloadedPodcasts()
                .firstOrNull { it.mediaId == mediaId }
                ?.title.toString()
    }

    private fun jsonToSet(json: String?): MutableSet<MediaDescriptionCompat> {
        if (json == null) {
            return mutableSetOf()
        }
        val podcasts: MutableSet<MediaDescriptionCompat>? = jsonAdapter.fromJson(json)
        return podcasts ?: mutableSetOf()
    }

    private fun setToJson(podcasts: MutableSet<MediaDescriptionCompat>): String {
        return jsonAdapter.toJson(podcasts)
    }

    private fun getDownloadingPodcast(mediaId: String): MediaDescriptionCompat? {
        return getDownloadingPodcasts().firstOrNull { it.mediaId == mediaId }
    }

    private fun addDownloadedPodcast(podcast: MediaDescriptionCompat) {
        val podcasts = getDownloadedPodcasts()
        podcasts.add(podcast)
        sharedPref.edit()
                .putString(DOWNLOADED_PODCASTS, setToJson(podcasts))
                .apply()
    }
}