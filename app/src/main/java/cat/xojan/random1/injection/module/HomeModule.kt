package cat.xojan.random1.injection.module

import android.app.Activity
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.viewmodel.PodcastsViewModel
import dagger.Module
import dagger.Provides

@Module
class HomeModule(private val mActivity: Activity) {

    @Provides
    internal fun providePOdcastsViewModel(programDataInteractor: ProgramDataInteractor): PodcastsViewModel {
        return PodcastsViewModel(programDataInteractor)
    }
}