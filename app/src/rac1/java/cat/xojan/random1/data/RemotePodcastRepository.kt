package cat.xojan.random1.data

import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.PodcastData
import cat.xojan.random1.domain.repository.PodcastRepository
import io.reactivex.Single

class RemotePodcastRepository(private val service: ApiService): PodcastRepository {

    private var hourPodcasts: Single<PodcastData>? = null
    private var sectionPodcasts: Single<PodcastData>? = null
    private var programId: String? = null
    private var sectionId: String? = null

    override fun getPodcasts(programId: String, sectionId: String?, refresh: Boolean)
            : Single<List<Podcast>> {
        val podcastData: Single<PodcastData>
        if (sectionId != null) {
            if (sectionPodcasts == null || refresh || programId != this.programId ||
                    sectionId != this.sectionId) {
                sectionPodcasts = service.getPodcastData(programId, sectionId).cache()
            }
            podcastData = sectionPodcasts!!
        } else {
            if (hourPodcasts == null || refresh || programId != this.programId) {
                hourPodcasts = service.getPodcastData(programId).cache()
            }
            podcastData = hourPodcasts!!
        }
        this.programId = programId
        this.sectionId = sectionId

        return podcastData.map { pd -> pd.toPodcasts(programId) }
    }
}