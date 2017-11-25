package cat.xojan.random1.injection.module

import android.app.DownloadManager
import android.content.Context
import cat.xojan.random1.Application
import cat.xojan.random1.BuildConfig
import cat.xojan.random1.data.SharedPrefDownloadPodcastRepository
import cat.xojan.random1.data.Rac1ApiService
import cat.xojan.random1.data.RemotePodcastRepository
import cat.xojan.random1.data.RemoteProgramRepository
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.EventLogger
import cat.xojan.random1.domain.interactor.MediaProvider
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.domain.repository.PodcastPreferencesRepository
import cat.xojan.random1.domain.repository.PodcastRepository
import cat.xojan.random1.domain.repository.ProgramRepository
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton

@Module
class AppModule(private val mApplication: Application) {

    companion object {
        private val RAC1_URL = "http://www.rac1.cat/audioteca/api/app/"
    }

    @Provides
    @Singleton
    internal fun provideDownloadManager(): DownloadManager {
        return mApplication.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    @Provides
    @Singleton
    internal fun provideRetrofitRac1Service(): Rac1ApiService {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) httpClientBuilder.addInterceptor(loggingInterceptor)

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .build()

        /*val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create()*/

        val retrofit = Retrofit.Builder()
                .baseUrl(RAC1_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClientBuilder.build())
                .build()

        return retrofit.create(Rac1ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProgramDataInteractor(
            downloadManager: DownloadManager,
            eventLogger: EventLogger,
            programRepository: ProgramRepository) : ProgramDataInteractor {
        return ProgramDataInteractor(programRepository,
                SharedPrefDownloadPodcastRepository(mApplication),
                mApplication, downloadManager, eventLogger)
    }

    @Provides
    @Singleton
    fun providePodcastDataInteractor(podcastRepository: PodcastRepository,
                                     programRepository: ProgramRepository,
                                     podcastPrefRepository: PodcastPreferencesRepository)
            : PodcastDataInteractor {
        return PodcastDataInteractor(programRepository, podcastRepository, podcastPrefRepository)
    }

    @Provides
    @Singleton
    fun provideEventLogger(): EventLogger {
        return if (BuildConfig.DEBUG) EventLogger(null) else EventLogger(Answers.getInstance())
    }

    @Provides
    @Singleton
    fun provideCrashReporter(): CrashReporter {
        return if (BuildConfig.DEBUG) CrashReporter(null) else CrashReporter(Crashlytics
                .getInstance())
    }

    @Provides
    @Singleton
    fun provideMediaProvider(programDataInteractor: ProgramDataInteractor,
                             podcastDataInteractor: PodcastDataInteractor
    ): MediaProvider {
        return MediaProvider(programDataInteractor, podcastDataInteractor)
    }

    @Provides
    @Singleton
    fun provideRemoteProgramRepository(service: Rac1ApiService): ProgramRepository {
        return RemoteProgramRepository(service)
    }

    @Provides
    @Singleton
    fun provideRemotePodcastRepository(service: Rac1ApiService): PodcastRepository {
        return RemotePodcastRepository(service)
    }
}