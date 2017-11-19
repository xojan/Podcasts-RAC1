package cat.xojan.random1.ui.browser

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.domain.entities.Section
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.injection.component.BrowseComponent
import cat.xojan.random1.ui.BaseActivity
import cat.xojan.random1.ui.BaseFragment
import cat.xojan.random1.viewmodel.PodcastsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HourByHourListFragment : BaseFragment() {

    @Inject internal lateinit var mPodcastsViewModel: PodcastsViewModel
    @Inject internal lateinit var mProgramDataInteractor: ProgramDataInteractor
    @Inject internal lateinit var mCrashReporter: CrashReporter

    private lateinit var adapter: PodcastListAdapter
    private val mCompositeDisposable = CompositeDisposable()

    private var mRecyclerView: RecyclerView? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    private var mEmptyList: TextView? = null

    companion object {
        val TAG = HourByHourListFragment::class.java.simpleName
        val ARG_PROGRAM = "program_param"

        fun newInstance(program: MediaBrowserCompat.MediaItem): HourByHourListFragment {
            val args = Bundle()
            args.putParcelable(ARG_PROGRAM, program)

            val hourByHourListFragment = HourByHourListFragment()
            hourByHourListFragment.arguments = args

            return hourByHourListFragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BrowseActivity) {
            //mHomeActivity = (HomeActivity) context;
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(BrowseComponent::class.java).inject(this)
        val view = inflater!!.inflate(R.layout.recycler_view_fragment, container, false)

        setHasOptionsMenu(true)

        mSwipeRefresh = view.findViewById(R.id.swipe_refresh)
        mRecyclerView = view.findViewById(R.id.recycler_view)
        mEmptyList = view.findViewById(R.id.empty_list)

        mSwipeRefresh!!.setColorSchemeResources(R.color.colorAccent)
        mSwipeRefresh!!.setOnRefreshListener { loadPodcasts(true) }
        mRecyclerView!!.layoutManager = LinearLayoutManager(activity)

        adapter = PodcastListAdapter()
        mRecyclerView!!.adapter = adapter

        return view
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadPodcasts(false)
    }*/

    /*override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if ((arguments.get(ARG_PROGRAM) as Program).sections.size > 1) {
            inflater!!.inflate(R.menu.hour_by_hour, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }*/

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                handleOnBackPressed()
                return true
            }
            R.id.action_sections -> {
                showSections()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mCompositeDisposable.add(mPodcastsViewModel!!.downloadedPodcastsUpdates
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.updateViewWithDownloaded(it) }))
    }

    override fun onStart() {
        super.onStart()
        // fetch browsing information to fill the recycler view
        val mediaBrowser = mediaBrowser()
        Log.d(TAG, "onStart, onConnected=" + mediaBrowser.isConnected)
        if (mediaBrowser.isConnected) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeDisposable.clear()
    }

    /*override fun handleOnBackPressed(): Boolean {
        activity.finish()
        return true
    }*/

    private fun loadPodcasts(refresh: Boolean) {
        Handler().postDelayed({
            mSwipeRefresh!!.isRefreshing = true
            val program = arguments.getParcelable<Program>(PodcastListFragment.ARG_PROGRAM)
            val section = arguments.getParcelable<Section>(PodcastListFragment.ARG_SECTION)

            mCompositeDisposable.add(mPodcastsViewModel!!.loadPodcasts(program, section, refresh)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ this.updateView(it) },
                            { this.handleError(it) }))
        }, 0)
    }

    private fun handleError(throwable: Throwable) {
        mCrashReporter!!.logException(throwable)
        mEmptyList!!.visibility = View.VISIBLE
        mSwipeRefresh!!.isRefreshing = false
        mRecyclerView!!.visibility = View.GONE
    }

    private fun updateView(podcasts: List<Podcast>) {
        mEmptyList!!.visibility = View.GONE
        mSwipeRefresh!!.isRefreshing = false
        //adapter!!.podcasts = podcasts
        mRecyclerView!!.visibility = View.VISIBLE
    }

    private fun updateViewWithDownloaded(podcasts: List<Podcast>) {
        adapter!!.updateWithDownloaded(podcasts)
    }

    private fun showSections() {
        mPodcastsViewModel!!.selectedSection(true)
        val sectionListFragment = SectionFragment.newInstance(arguments.get(ARG_PROGRAM) as Program)
        (activity as BaseActivity).addFragment(sectionListFragment, SectionFragment.TAG, true)
    }

    fun onMediaControllerConnected() {
        Log.d("joan", "on media controller connected - fragment")
        if (isDetached) {
            return
        }
        val mediaId = mediaId() ?: mediaBrowser().root

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

    private fun mediaId(): String? {
        val mediaItem = arguments.getParcelable<MediaBrowserCompat.MediaItem>(ARG_PROGRAM)
        Log.d(TAG, mediaItem.mediaId)
        return mediaItem.mediaId
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            try {
                Log.d(TAG, "onChildrenLoaded, parentId=" + parentId + "  count=" + children.size)
                adapter.podcasts = children
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
