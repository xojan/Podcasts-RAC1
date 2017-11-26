package cat.xojan.random1.injection.module

import android.app.Activity
import dagger.Module

@Module
class MediaPlaybackModule(private val activity: Activity) {

    /*@Provides
    internal fun provideProgramsViewModel(programDataInteractor: ProgramDataInteractor): ProgramsViewModel {
        return ProgramsViewModel(programDataInteractor)
    }

    @Provides
    internal fun providePOdcastsViewModel(programDataInteractor: ProgramDataInteractor): BrowserViewModel {
        return BrowserViewModel(programDataInteractor)
    }*/
}