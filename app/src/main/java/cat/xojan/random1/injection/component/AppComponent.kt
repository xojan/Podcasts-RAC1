package cat.xojan.random1.injection.component

import android.app.DownloadManager
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.domain.model.EventLogger
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.domain.repository.PodcastPreferencesRepository
import cat.xojan.random1.domain.repository.PodcastRepository
import cat.xojan.random1.domain.repository.ProgramRepository
import cat.xojan.random1.injection.module.AppModule
import cat.xojan.random1.feature.downloads.DownloadCompleteReceiver
import cat.xojan.random1.feature.mediaplayback.MediaPlaybackService
import cat.xojan.random1.feature.BaseActivity
import cat.xojan.random1.feature.mediaplayback.QueueManager
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(baseActivity: BaseActivity)
    fun inject(downloadCompleteReceiver: DownloadCompleteReceiver)
    fun inject(mediaPlaybackService: MediaPlaybackService)

    fun downloadManager(): DownloadManager
    fun programDataInteractor(): ProgramDataInteractor
    fun podcastDataInteractor(): PodcastDataInteractor
    fun eventLogger(): EventLogger
    fun crashReporter(): CrashReporter
    fun programRepository(): ProgramRepository
    fun podcastRepository(): PodcastRepository
    fun podcastPreferencesRepository(): PodcastPreferencesRepository
    fun queueManager(): QueueManager
}