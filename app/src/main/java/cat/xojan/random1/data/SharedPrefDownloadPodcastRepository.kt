package cat.xojan.random1.data

import android.content.Context
import android.content.SharedPreferences
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.repository.DownloadPodcastRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import com.squareup.moshi.Types
import java.util.*

class SharedPrefDownloadPodcastRepository(context: Context) : DownloadPodcastRepository {

    private val DOWNLOAD_PODCASTS = "dowload_podcasts_repo"
    private val DOWNLOADING_PODCASTS = "downloading_podcasts"
    private val DOWNLOADED_PODCASTS = "downloaded_podcasts"

    private val sharedPref: SharedPreferences
    private val jsonAdapter: JsonAdapter<MutableSet<Podcast>>

    init {
        sharedPref = context.getSharedPreferences(DOWNLOAD_PODCASTS, Context.MODE_PRIVATE)
        val type = Types.newParameterizedType(MutableSet::class.java, Podcast::class.java)
        val moshi = Moshi.Builder()
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .build()
        jsonAdapter = moshi.adapter(type)
    }

    override fun addDownloadingPodcast(podcast: Podcast): Boolean {
        podcast.state = Podcast.State.DOWNLOADING
        val podcasts = getDownloadingPodcasts()
        podcasts.add(podcast)
        return sharedPref.edit()
                .putString(DOWNLOADING_PODCASTS, setToJson(podcasts))
                .commit()
    }

    override fun deleteDownloadingPodcast(podcast: Podcast): Boolean {
        val podcasts = getDownloadingPodcasts()
        podcasts.remove(podcast)
        return sharedPref.edit()
                .putString(DOWNLOADING_PODCASTS, setToJson(podcasts))
                .commit()
    }

    override fun setPodcastAsDownloaded(audioId: String, filePath: String) {
        val podcast = getDownloadingPodcast(audioId)
        if (podcast != null && deleteDownloadingPodcast(podcast)) {
            podcast.filePath = filePath
            podcast.state = Podcast.State.DOWNLOADED
            addDownloadedPodcast(podcast)
        }
    }

    override fun getDownloadingPodcasts(): MutableSet<Podcast> {
        val podcastsJson = sharedPref.getString(DOWNLOADING_PODCASTS, null)
        return jsonToSet(podcastsJson)
    }

    override fun getDownloadedPodcasts(): MutableSet<Podcast> {
        val podcastsJson = sharedPref.getString(DOWNLOADED_PODCASTS, null)
        return jsonToSet(podcastsJson)
    }

    override fun deleteDownloadedPodcast(podcast: Podcast) {
        val podcasts = getDownloadedPodcasts()
        podcasts.remove(podcast)
        sharedPref.edit()
                .putString(DOWNLOADED_PODCASTS, setToJson(podcasts))
                .apply()
    }

    override fun getDownloadedPodcastTitle(audioId: String): String? {
        return getDownloadedPodcasts()
                .firstOrNull { it.audioId == audioId }
                ?.title
    }

    private fun jsonToSet(json: String?): MutableSet<Podcast> {
        if (json == null) {
            return mutableSetOf()
        }
        val podcasts: MutableSet<Podcast>? = jsonAdapter.fromJson(json)
        return podcasts ?: mutableSetOf()
    }

    private fun setToJson(podcasts: MutableSet<Podcast>): String {
        return jsonAdapter.toJson(podcasts)
    }

    private fun getDownloadingPodcast(audioId: String): Podcast? {
        return getDownloadingPodcasts().firstOrNull { it.audioId == audioId }
    }

    private fun addDownloadedPodcast(podcast: Podcast) {
        val podcasts = getDownloadedPodcasts()
        podcasts.add(podcast)
        sharedPref.edit()
                .putString(DOWNLOADED_PODCASTS, setToJson(podcasts))
                .apply()
    }
}