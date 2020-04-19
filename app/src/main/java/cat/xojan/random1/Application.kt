package cat.xojan.random1

import androidx.multidex.MultiDexApplication
import cat.xojan.random1.injection.component.AppComponent
import cat.xojan.random1.injection.component.DaggerAppComponent
import cat.xojan.random1.injection.module.AppModule
import com.squareup.leakcanary.LeakCanary

class Application : MultiDexApplication() {

     lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initInjector()
        initLeakDetection()
    }

    private fun initInjector() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }

    private fun initLeakDetection() {
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this)
        }
    }
}