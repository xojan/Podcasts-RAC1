package cat.xojan.random1.data

import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.PodcastData
import cat.xojan.random1.domain.repository.PodcastRepository
import io.reactivex.Observable
import java.io.IOException

class RemotePodcastRepository(private val service: Rac1ApiService): PodcastRepository {

    @Throws(IOException::class)
    override fun getPodcasts(programId: String, sectionId: String?): Observable<List<Podcast>> {
        sectionId?.let {
            return service.getPodcastData(programId, sectionId).map(PodcastData::podcasts)
        }
        return service.getPodcastData(programId).map(PodcastData::podcasts)
    }
}