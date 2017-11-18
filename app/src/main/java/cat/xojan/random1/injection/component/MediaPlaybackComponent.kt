package cat.xojan.random1.injection.component

import cat.xojan.random1.injection.PerActivity
import cat.xojan.random1.injection.module.BaseActivityModule
import cat.xojan.random1.injection.module.HomeModule
import cat.xojan.random1.injection.module.MediaPlaybackModule
import cat.xojan.random1.ui.activity.HomeActivity
import cat.xojan.random1.ui.activity.MediaPlaybackActivity
import cat.xojan.random1.ui.fragment.DownloadsFragment
import cat.xojan.random1.ui.fragment.ProgramFragment
import dagger.Component

@PerActivity
@Component(
        dependencies = arrayOf(AppComponent::class),
        modules = arrayOf(BaseActivityModule::class, MediaPlaybackModule::class))
interface MediaPlaybackComponent : BaseActivityComponent {
    fun inject(mediaPlaybackActivity: MediaPlaybackActivity)
}