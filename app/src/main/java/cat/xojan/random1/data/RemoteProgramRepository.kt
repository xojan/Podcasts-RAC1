package cat.xojan.random1.data

import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.PodcastData
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Flowable
import java.io.IOException

class RemoteProgramRepository(private val service: Rac1ApiService): ProgramRepository {

    @Throws(IOException::class)
    override fun getPrograms(): List<Program> {
        return service.getProgramData().execute().body()!!.programs
    }

    @Throws(IOException::class)
    override fun getPodcast(programId: String, sectionId: String?): Flowable<List<Podcast>> {
        sectionId?.let {
            return service.getPodcastData(programId, sectionId).map(PodcastData::podcasts)
        }
        return service.getPodcastData(programId).map(PodcastData::podcasts)
    }

    @Throws(IOException::class)
    override fun getPodcastPlainData(programId: String, sectionId: String?): List<Podcast> {
        sectionId?.let {
            return service.getPodcastDataPlainData(programId, sectionId).execute().body()!!.podcasts
        }
        return service.getPodcastDataPlainData(programId).execute().body()!!.podcasts
    }
}