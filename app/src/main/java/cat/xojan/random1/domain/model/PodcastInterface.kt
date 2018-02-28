package cat.xojan.random1.domain.model

interface PodcastInterface {
    fun toPodcasts(programId: String): List<Podcast>
}