package cat.xojan.random1.injection.component

import cat.xojan.random1.injection.PerActivity
import cat.xojan.random1.injection.module.BaseActivityModule
import cat.xojan.random1.injection.module.RadioPlayerModule
import cat.xojan.random1.feature.mediaplayback.RadioPlayerActivity
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(AppComponent::class),
        modules = arrayOf(BaseActivityModule::class, RadioPlayerModule::class))
interface RadioPlayerComponent : BaseActivityComponent {
    fun inject(radioPlayerActivity: RadioPlayerActivity)
}
