package cat.xojan.random1.feature.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.fragment.app.Fragment
import cat.xojan.random1.R
import cat.xojan.random1.feature.MediaPlayerBaseActivity
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.BrowseComponent
import cat.xojan.random1.injection.component.DaggerBrowseComponent
import cat.xojan.random1.injection.module.BrowseModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class BrowseActivity : MediaPlayerBaseActivity(), HasComponent<BrowseComponent> {
    companion object {
        private val EXTRA_PROGRAM = "extra_program"

        fun newIntent(context: Context, mediaId: MediaBrowserCompat.MediaItem): Intent {
            val intent = Intent(context, BrowseActivity::class.java)
            intent.putExtra(EXTRA_PROGRAM, mediaId)
            return intent
        }
    }

    @Inject
    internal lateinit var viewModel: BrowserViewModel
    private val compositeDisposable = CompositeDisposable()

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
        mediaItem?.let {
            title = mediaItem.description.title
            if (viewModel.isSectionSelected()) {
                compositeDisposable.add(
                        viewModel.hasSections(mediaItem.mediaId)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        { b ->
                                            if (b) {
                                                addFragment(SectionFragment.newInstance(mediaItem.mediaId),
                                                        SectionFragment.TAG, false)
                                            } else {
                                                addFragment(HourByHourListFragment.newInstance(mediaItem.mediaId),
                                                        HourByHourListFragment.TAG, false)
                                            }
                                        },
                                        {}
                                ))
            } else {
                addFragment(HourByHourListFragment.newInstance(mediaItem.mediaId),
                        HourByHourListFragment.TAG, false)
            }
        }
    }

    override fun onMediaControllerConnected() {
        val frag: Fragment = supportFragmentManager.findFragmentById(R.id.container_fragment)!!
        when (frag) {
            is SectionPodcastListFragment -> frag.onMediaControllerConnected()
            is SectionFragment -> frag.onMediaControllerConnected()
            is HourByHourListFragment -> frag.onMediaControllerConnected()
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}