package cat.xojan.random1.injection.module

import android.app.Activity
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.viewmodel.PodcastsViewModel
import dagger.Module
import dagger.Provides

@Module
class HomeModule(private val activity: Activity) {

    @Provides
    internal fun providePodcastsViewModel(podcastsInteractor: PodcastDataInteractor)
            : PodcastsViewModel {
        return PodcastsViewModel(podcastsInteractor)
    }
}