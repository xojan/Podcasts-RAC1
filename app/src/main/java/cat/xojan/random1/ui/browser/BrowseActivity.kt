package cat.xojan.random1.ui.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import cat.xojan.random1.R
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.BrowseComponent
import cat.xojan.random1.injection.component.DaggerBrowseComponent
import cat.xojan.random1.injection.module.BrowseModule
import cat.xojan.random1.ui.BaseActivity
import cat.xojan.random1.viewmodel.PodcastsViewModel
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

    @Inject internal lateinit var mViewModel: PodcastsViewModel

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
        initView()
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mediaItem = intent.getParcelableExtra<MediaBrowserCompat.MediaItem>(EXTRA_PROGRAM)
        title = mediaItem.description.title
        /*if (intent.getBooleanExtra(EXTRA_IS_SECTION, false)) {
            addFragment(SectionFragment.newInstance(program), SectionFragment.TAG, true)
        } else {*/
            addFragment(HourByHourListFragment(), HourByHourListFragment.TAG, true)
        //}
    }
}