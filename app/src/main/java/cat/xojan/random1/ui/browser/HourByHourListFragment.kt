package cat.xojan.random1.ui.browser

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.injection.component.BrowseComponent
import cat.xojan.random1.ui.BaseActivity
import cat.xojan.random1.ui.BaseFragment
import cat.xojan.random1.ui.IsMediaBrowserFragment
import cat.xojan.random1.ui.MediaBrowserProvider
import cat.xojan.random1.ui.home.ProgramFragment
import cat.xojan.random1.ui.home.ProgramFragment.Companion.MEDIA_ID_ROOT
import cat.xojan.random1.viewmodel.PodcastsViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.recycler_view_fragment.*
import javax.inject.Inject

class HourByHourListFragment : BaseFragment(), IsMediaBrowserFragment {

    @Inject internal lateinit var mPodcastsViewModel: PodcastsViewModel
    @Inject internal lateinit var crashReporter: CrashReporter

    private lateinit var adapter: PodcastListAdapter
    private val mCompositeDisposable = CompositeDisposable()

    private var mediaBrowserProvider: MediaBrowserProvider? = null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaBrowserProvider = context as BaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(BrowseComponent::class.java).inject(this)
        val view = inflater.inflate(R.layout.recycler_view_fragment, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh.setColorSchemeResources(R.color.colorAccent)
        swipe_refresh.setOnRefreshListener { onMediaControllerConnected() }
        recycler_view.layoutManager = LinearLayoutManager(activity)

        adapter = PodcastListAdapter()
        recycler_view.adapter = adapter
    }

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

    override fun onStart() {
        super.onStart()
        // fetch browsing information to fill the recycler view
        val mediaBrowser = mediaBrowserProvider?.getMediaBrowser()
        mediaBrowser?.let {
            Log.d(TAG, "onStart, onConnected=" + mediaBrowser.isConnected)
            if (mediaBrowser.isConnected) {
                Log.d(TAG, "onStart, mediaId=" + mediaBrowser.root)
                onMediaControllerConnected()
            }
        }
    }

    /* override fun onResume() {
        super.onResume()
        mCompositeDisposable.add(mPodcastsViewModel.downloadedPodcastsUpdates
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.updateViewWithDownloaded(it) }))
    } */

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeDisposable.clear()
    }

    override fun onStop() {
        super.onStop()
        val mediaBrowser = mediaBrowserProvider?.getMediaBrowser()
        mediaBrowser?.let {
            val mediaId = mediaId()
            if (mediaBrowser.isConnected && mediaId != null) {
                mediaBrowser.unsubscribe(mediaId)
            }
        }
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.unregisterCallback(mediaControllerCallback)
    }

    override fun onDetach() {
        super.onDetach()
        mediaBrowserProvider = null
    }

    override fun handleOnBackPressed(): Boolean {
        activity.finish()
        return true
    }

    private fun handleError(d: MediaDescriptionCompat) {
        crashReporter.logException(d.description.toString())
        empty_list.visibility = VISIBLE
        swipe_refresh.isRefreshing = false
        recycler_view.visibility = GONE
    }

    private fun showPodcasts() {
        empty_list.visibility = GONE
        swipe_refresh.isRefreshing = false
        recycler_view.visibility = VISIBLE
    }

    private fun updateViewWithDownloaded(podcasts: List<Podcast>) {
        adapter.updateWithDownloaded(podcasts)
    }

    private fun showSections() {
       /* mPodcastsViewModel.selectedSection(true)
        val sectionListFragment = SectionFragment.newInstance(arguments?.get(ARG_PROGRAM) as Program)
        (activity as BaseActivity).addFragment(sectionListFragment, SectionFragment.TAG, true)*/
    }

    override fun onMediaControllerConnected() {
        if (isDetached) {
            return
        }
        swipe_refresh.isRefreshing = true
        val mediaBrowser = mediaBrowserProvider?.getMediaBrowser()

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mediaBrowser?.let {
            val mediaId = mediaId() ?: MEDIA_ID_ROOT
            mediaBrowser.unsubscribe(mediaId)
            mediaBrowser.subscribe(mediaId, mediaBrowserSubscriptionCallback)
        }

        // Add MediaController callback so we can redraw the list when metadata changes:
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.registerCallback(mediaControllerCallback)
    }

    private fun mediaId(): String? {
        val mediaItem = arguments?.getParcelable<MediaBrowserCompat.MediaItem>(ARG_PROGRAM)
        mediaItem?.let {
            return mediaItem.mediaId
        }
        return null
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            if (isChildrenError(children)) {
                handleError(children[0].description)
            } else {
                Log.d(TAG, "onChildrenLoaded, parentId=" + parentId + "  count=" + children.size)
                adapter.podcasts = children
                showPodcasts()
            }
        }

        override fun onError(id: String) {
            val msg = "hourByHour fragment subscription onError, id=" + id
            Log.e(TAG, msg)
            crashReporter.logException(msg)
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