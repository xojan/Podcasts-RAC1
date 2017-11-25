package cat.xojan.random1.domain.repository

interface PodcastPreferencesRepository {
    fun isSectionSelected(): Boolean

    fun setSectionSelected(boolean: Boolean)
}