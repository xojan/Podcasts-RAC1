package cat.xojan.random1.data

import android.content.Context
import android.content.SharedPreferences
import cat.xojan.random1.domain.repository.PodcastPreferencesRepository
import javax.inject.Inject

class SharedPrefPodcastPreferencesRepository @Inject constructor(context: Context)
    : PodcastPreferencesRepository {

    private val sharedPref: SharedPreferences
    private val podcastsPref = "podcasts_preferences"
    private val sectionSelected = "section_selected"

    init {
        sharedPref = context.getSharedPreferences(podcastsPref, Context.MODE_PRIVATE)
    }

    override fun isSectionSelected(): Boolean {
        return sharedPref.getBoolean(sectionSelected, false)
    }

    override fun setSectionSelected(boolean: Boolean) {
        sharedPref.edit().putBoolean(sectionSelected, boolean).apply()
    }
}