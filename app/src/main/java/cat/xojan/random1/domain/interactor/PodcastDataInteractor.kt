package cat.xojan.random1.domain.interactor

import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.repository.PodcastPreferencesRepository
import cat.xojan.random1.domain.repository.PodcastRepository
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Single
import javax.inject.Inject

class PodcastDataInteractor @Inject constructor(
        private val programRepo: ProgramRepository,
        private val podcastRepo: PodcastRepository,
        private val podcastPref: PodcastPreferencesRepository
) {
    fun getHourByHourPodcasts(programId: String): Single<List<Podcast>> {
        val program = programRepo.getProgram(programId)
        return podcastRepo.getPodcasts(programId)
                .flatMapIterable { list -> list }
                .map { podcast ->
                    program?.let {
                        podcast.programId = program.id
                        podcast.imageUrl = program.imageUrl()
                    }
                    podcast
                }
                .toList()
    }

    fun isSectionSelected(): Boolean {
        return podcastPref.isSectionSelected()
    }

    fun setSectionSelected(selected: Boolean) {
        podcastPref.setSectionSelected(selected)
    }
}