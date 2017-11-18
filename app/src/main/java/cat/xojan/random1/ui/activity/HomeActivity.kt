package cat.xojan.random1.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import cat.xojan.random1.R
import cat.xojan.random1.injection.HasComponent
import cat.xojan.random1.injection.component.DaggerHomeComponent
import cat.xojan.random1.injection.component.HomeComponent
import cat.xojan.random1.injection.module.HomeModule
import cat.xojan.random1.ui.adapter.HomePagerAdapter
import cat.xojan.random1.ui.fragment.DownloadsFragment
import cat.xojan.random1.ui.fragment.ProgramFragment
import cat.xojan.random1.viewmodel.PodcastsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject

class HomeActivity: BaseActivity(), HasComponent<HomeComponent> {
    companion object {
        private val PERMISSION_WRITE_EXTERNAL_STORAGE = 20
    }

    @Inject internal lateinit var mViewModel: PodcastsViewModel
    private val mCompositeDisposable = CompositeDisposable()

    override val component: HomeComponent by lazy {
        DaggerHomeComponent.builder()
                .appComponent(applicationComponent)
                .baseActivityModule(activityModule)
                .homeModule(HomeModule(this))
                .build()
    }

    private val pageAdapter: HomePagerAdapter by lazy {
        HomePagerAdapter(supportFragmentManager, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initView()
        component.inject(this)
    }

    override fun onMediaControllerConnected() {
        val mediaId = mediaBrowser.root

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mediaBrowser.unsubscribe(mediaId)
        mediaBrowser.subscribe(mediaId, mediaBrowserSubscriptionCallback)

        // Add MediaController callback so we can redraw the list when metadata changes:
        val controller = MediaControllerCompat.getMediaController(this)
        controller?.registerCallback(mediaControllerCallback)
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        pageAdapter.addFragment(ProgramFragment())
        pageAdapter.addFragment(DownloadsFragment())

        viewPager.adapter = pageAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export_podcasts -> if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestWriteExternalStoragePermission()
            } else {
                exportPodcasts()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                exportPodcasts()
            }
        }
    }

    private fun requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_EXTERNAL_STORAGE)
    }

    override fun onStop() {
        super.onStop()
        mCompositeDisposable.clear()
    }

    private fun exportPodcasts() {
        mCompositeDisposable.add(mViewModel.exportPodcasts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<Boolean> { this.notifyUser(it) }))
    }

    private fun notifyUser(b: Boolean?) {
        Toast.makeText(this, getString(R.string.podcasts_exported), Toast.LENGTH_LONG).show()
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            try {
                Log.d(TAG, "onChildrenLoaded, parentId=" + parentId +
                        "  count=" + children.size)
                //TODO add children to list adapter
            } catch (t: Throwable) {
                Log.e(TAG, "Error on childrenloaded", t)
            }

        }

        override fun onError(id: String) {
            Log.e(TAG, "browse fragment subscription onError, id=" + id)
            //TODO handle error
        }
    }

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            if (metadata == null) {
                return
            }
            Log.d(TAG, "Received metadata change to media " + metadata.description.mediaId)
            //TODO update programs adapter
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            super.onPlaybackStateChanged(state)
            Log.d(TAG, "Received state change: " + state)
            //TODO update whatever
        }
    }
}