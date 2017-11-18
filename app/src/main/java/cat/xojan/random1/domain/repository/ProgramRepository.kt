package cat.xojan.random1.domain.repository

import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.Program
import io.reactivex.Flowable
import java.io.IOException

interface ProgramRepository {
    @Throws(IOException::class) fun getPrograms(): List<Program>

    @Throws(IOException::class)
    fun getPodcast(programId: String, sectionId: String? = null): Flowable<List<Podcast>>

    @Throws(IOException::class)
    fun getPodcastPlainData(programId: String, sectionId: String? = null): List<Podcast>
}