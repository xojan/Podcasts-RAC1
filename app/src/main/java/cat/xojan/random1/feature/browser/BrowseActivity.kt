package cat.xojan.random1.feature.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import cat.xojan.random1.R
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.BrowseComponent
import cat.xojan.random1.injection.component.DaggerBrowseComponent
import cat.xojan.random1.injection.module.BrowseModule
import cat.xojan.random1.feature.BaseActivity
import javax.inject.Inject


class BrowseActivity: BaseActivity(), HasComponent<BrowseComponent> {
    companion object {
        private val EXTRA_PROGRAM = "extra_program"

        fun newIntent(context: Context, mediaId: MediaBrowserCompat.MediaItem): Intent {
            val intent = Intent(context, BrowseActivity::class.java)
            intent.putExtra(EXTRA_PROGRAM, mediaId)
            return intent
        }
    }

    @Inject internal lateinit var viewModel: BrowserViewModel

    override val component: BrowseComponent by lazy {
        DaggerBrowseComponent.builder()
                .appComponent(applicationComponent)
                .baseActivityModule(activityModule)
                .browseModule(BrowseModule(this))
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        component.inject(this)

        if (savedInstanceState == null) {
            initView()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView() {
        val mediaItem = intent.getParcelableExtra<MediaBrowserCompat.MediaItem>(EXTRA_PROGRAM)
        title = mediaItem.description.title
        if (viewModel.isSectionSelected() && viewModel.hasSections(mediaItem.mediaId)) {
            addFragment(SectionFragment.newInstance(mediaItem.mediaId),
                    SectionFragment.TAG, false)
        } else {
            addFragment(HourByHourListFragment.newInstance(mediaItem.mediaId),
                    HourByHourListFragment.TAG, false)
        }
    }

    override fun onMediaControllerConnected() {
        val frag: Fragment = supportFragmentManager.findFragmentById(R.id.container_fragment)
        when (frag) {
            is SectionPodcastListFragment -> frag.onMediaControllerConnected()
            is SectionFragment -> frag.onMediaControllerConnected()
            is HourByHourListFragment -> frag.onMediaControllerConnected()
        }
    }
}