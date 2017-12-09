package cat.xojan.random1.feature.mediaplayback

import android.os.Bundle
import cat.xojan.random1.R
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.DaggerMediaPlaybackComponent
import cat.xojan.random1.injection.component.MediaPlaybackComponent
import cat.xojan.random1.injection.module.MediaPlaybackModule
import cat.xojan.random1.feature.BaseActivity


class MediaPlaybackFullScreenActivity : BaseActivity(), HasComponent<MediaPlaybackComponent> {

    override val component: MediaPlaybackComponent by lazy {
        DaggerMediaPlaybackComponent.builder()
                .appComponent(applicationComponent)
                .baseActivityModule(activityModule)
                .mediaPlaybackModule(MediaPlaybackModule(this))
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_playback)
        component.inject(this)
    }

    override fun onMediaControllerConnected() {
        //TODO do some stuff here
    }
}