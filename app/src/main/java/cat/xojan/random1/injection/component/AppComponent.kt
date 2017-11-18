package cat.xojan.random1.injection.component

import android.app.DownloadManager
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.EventLogger
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.injection.module.AppModule
import cat.xojan.random1.receiver.DownloadCompleteReceiver
import cat.xojan.random1.service.MediaPlaybackService
import cat.xojan.random1.ui.activity.BaseActivity
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
    fun eventLogger(): EventLogger
    fun crashReporter(): CrashReporter
}