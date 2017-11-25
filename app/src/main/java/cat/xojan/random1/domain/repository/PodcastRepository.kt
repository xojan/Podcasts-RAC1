package cat.xojan.random1.domain.repository

import cat.xojan.random1.domain.entities.Podcast
import io.reactivex.Observable
import java.io.IOException

interface PodcastRepository {
    @Throws(IOException::class)
    fun getPodcasts(programId: String, sectionId: String? = null): Observable<List<Podcast>>
}