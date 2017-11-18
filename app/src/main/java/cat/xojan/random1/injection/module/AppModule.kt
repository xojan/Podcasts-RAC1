package cat.xojan.random1.injection.module

import android.app.DownloadManager
import android.content.Context
import cat.xojan.random1.Application
import cat.xojan.random1.BuildConfig
import cat.xojan.random1.data.PreferencesDownloadPodcastRepository
import cat.xojan.random1.data.Rac1ApiService
import cat.xojan.random1.data.RemoteProgramRepository
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.EventLogger
import cat.xojan.random1.domain.interactor.MusicProvider
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
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
    internal fun provideProgramDataInteractor(service: Rac1ApiService,
                                              downloadManager: DownloadManager,
                                              eventLogger: EventLogger): ProgramDataInteractor {
        return ProgramDataInteractor(RemoteProgramRepository(service),
                PreferencesDownloadPodcastRepository(mApplication),
                mApplication, downloadManager, eventLogger)
    }

    @Provides
    @Singleton
    internal fun provideEventLogger(): EventLogger {
        return if (BuildConfig.DEBUG) EventLogger(null) else EventLogger(Answers.getInstance())
    }

    @Provides
    @Singleton
    internal fun provideCrashReporter(): CrashReporter {
        return if (BuildConfig.DEBUG) CrashReporter(null) else CrashReporter(Crashlytics
                .getInstance())
    }

    @Provides
    @Singleton
    internal fun provideMusicProvider(programDataInteractor: ProgramDataInteractor): MusicProvider {
        return MusicProvider(programDataInteractor)
    }
}