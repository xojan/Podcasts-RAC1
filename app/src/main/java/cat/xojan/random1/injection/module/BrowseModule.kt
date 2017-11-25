package cat.xojan.random1.injection.module

import android.app.Activity
import cat.xojan.random1.data.SharedPrefPodcastPreferencesRepository
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.domain.repository.PodcastPreferencesRepository
import cat.xojan.random1.viewmodel.PodcastsViewModel
import cat.xojan.random1.viewmodel.SectionsViewModel
import dagger.Module
import dagger.Provides

@Module
class BrowseModule(private val activity: Activity) {

    @Provides
    fun providePodcastsViewModel(
            podcastInteractor: PodcastDataInteractor): PodcastsViewModel {
        return PodcastsViewModel(podcastInteractor)
    }

    @Provides
    fun provideSectionsViewModel(programDataInteractor: ProgramDataInteractor): SectionsViewModel {
        return SectionsViewModel(programDataInteractor)
    }
}