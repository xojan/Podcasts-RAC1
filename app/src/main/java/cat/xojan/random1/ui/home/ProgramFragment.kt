package cat.xojan.random1.ui.home

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.injection.component.HomeComponent
import cat.xojan.random1.ui.BaseActivity
import cat.xojan.random1.ui.BaseFragment
import cat.xojan.random1.viewmodel.ProgramsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.recycler_view_fragment.*
import javax.inject.Inject


class ProgramFragment: BaseFragment() {

    private val TAG = ProgramFragment::class.simpleName

    @Inject internal lateinit var programsViewModel: ProgramsViewModel
    @Inject internal lateinit var programInteractor: ProgramDataInteractor
    @Inject internal lateinit var crashReporter: CrashReporter

    private val mCompositeDisposable = CompositeDisposable()
    private lateinit var adapter: ProgramListAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(HomeComponent::class.java).inject(this)
        return inflater!!.inflate(R.layout.recycler_view_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh.setColorSchemeResources(R.color.colorAccent)
        swipe_refresh.setOnRefreshListener { Handler().postDelayed({ this.loadPrograms() }, 0) }
        adapter = ProgramListAdapter(programInteractor)
        recycler_view.adapter = adapter
        setLayoutManager(resources.configuration.orientation)
        //loadPrograms()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeDisposable.clear()
    }

    private fun setLayoutManager(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recycler_view.layoutManager = GridLayoutManager(activity, 3)
        } else {
            recycler_view.layoutManager = GridLayoutManager(activity, 2)
        }
    }

    private fun loadPrograms() {
        swipe_refresh.isRefreshing = true
        mCompositeDisposable.add(programsViewModel.loadPrograms()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.updateView(it) },
                        { this.handleError(it) }))
    }

    private fun updateView(programs: List<Program>) {
        swipe_refresh.isRefreshing = false
        //adapter.programs = programs
        empty_list.visibility = View.GONE
        recycler_view.visibility = View.VISIBLE
    }

    private fun handleError(e: Throwable) {
        crashReporter.logException(e)
        swipe_refresh.isRefreshing = false
        empty_list.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE
    }

    fun onMediaControllerConnected() {
        if (isDetached) {
            return
        }
        val mediaId = mediaBrowser().root

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mediaBrowser().unsubscribe(mediaId)
        mediaBrowser().subscribe(mediaId, mediaBrowserSubscriptionCallback)

        // Add MediaController callback so we can redraw the list when metadata changes:
        val controller = MediaControllerCompat.getMediaController(activity)
        controller?.registerCallback(mediaControllerCallback)
    }

    override fun onStart() {
        super.onStart()
        // fetch browsing information to fill the recycler view
        val mediaBrowser = mediaBrowser()
        Log.d(TAG, "onStart, onConnected=" + mediaBrowser.isConnected)
        if (mediaBrowser.isConnected) {
            Log.d(TAG, "onStart, mediaId=" + mediaBrowser.root)
            onMediaControllerConnected()
        }
    }

    override fun onStop() {
        super.onStop()
        val mediaBrowser = mediaBrowser()
        val mediaId = mediaBrowser().root
        if (mediaBrowser.isConnected) {
            mediaBrowser.unsubscribe(mediaId)
        }
        val controller = MediaControllerCompat.getMediaController(activity)
        controller?.unregisterCallback(mediaControllerCallback)
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                        children: List<MediaBrowserCompat.MediaItem>) {
            try {
                Log.d(TAG, "onChildrenLoaded, parentId=" + parentId + "  count=" + children.size)
                adapter.programs = children
            } catch (t: Throwable) {
                Log.e(TAG, "Error onChildrenLoaded", t)
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