package cat.xojan.random1.feature.browser

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.feature.BaseFragment
import cat.xojan.random1.feature.IsMediaBrowserFragment
import cat.xojan.random1.feature.MediaBrowserProvider
import cat.xojan.random1.feature.MediaPlayerBaseActivity
import cat.xojan.random1.injection.component.BrowseComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.recycler_view_fragment.*
import javax.inject.Inject

class SectionPodcastListFragment : BaseFragment(), IsMediaBrowserFragment {

    @Inject internal lateinit var viewModel: BrowserViewModel
    @Inject internal lateinit var crashReporter: CrashReporter

    private lateinit var adapter: PodcastListAdapter
    private val compositeDisposable = CompositeDisposable()

    private var mediaBrowserProvider: MediaBrowserProvider? = null
    private var refresh = false

    companion object {
        val TAG = SectionPodcastListFragment::class.java.simpleName
        val ARG_MEDIA_ID = "media_id"

        fun newInstance(mediaId: String?): SectionPodcastListFragment {
            val args = Bundle()
            args.putString(ARG_MEDIA_ID, mediaId)

            val podcastListFragment = SectionPodcastListFragment()
            podcastListFragment.arguments = args

            return podcastListFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaBrowserProvider = context as MediaPlayerBaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(BrowseComponent::class.java).inject(this)
        val view = inflater.inflate(R.layout.recycler_view_fragment, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh.setColorSchemeResources(R.color.colorAccent)
        swipe_refresh.setProgressBackgroundColorSchemeResource(R.color.colorPrimaryDark)
        swipe_refresh.setOnRefreshListener {
            refresh = true
            onMediaControllerConnected()
        }
        recycler_view.layoutManager = LinearLayoutManager(activity)

        adapter = PodcastListAdapter(viewModel, activity as Activity)
        recycler_view.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                activity!!.onBackPressed()
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
                onMediaControllerConnected()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(viewModel.getPodcastStateUpdates()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {p -> adapter.updatePodcastsState(p)},
                        {e -> crashReporter.logException(e)}
                ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

    override fun onStop() {
        super.onStop()
        val mediaBrowser = mediaBrowserProvider?.getMediaBrowser()
        mediaBrowser?.let {
            if (mediaBrowser.isConnected) {
                mediaBrowser.unsubscribe(mediaId())
            }
        }
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.unregisterCallback(mediaControllerCallback)
    }

    override fun onDetach() {
        super.onDetach()
        mediaBrowserProvider = null
    }

    private fun mediaId(): String {
        val mediaId = arguments!!.getString(ARG_MEDIA_ID) + "/" + refresh
        refresh = false
        return mediaId
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
            val mediaId = mediaId()
            mediaBrowser.unsubscribe(mediaId)
            mediaBrowser.subscribe(mediaId, mediaBrowserSubscriptionCallback)
        }

        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.registerCallback(mediaControllerCallback)
    }

    private fun showPodcasts() {
        empty_list.visibility = View.GONE
        swipe_refresh.isRefreshing = false
        recycler_view.visibility = View.VISIBLE
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            if (isChildrenError(children)) {
                handleError(children[0].description)
            } else {
                adapter.podcasts = viewModel.updatePodcastState(children)
                showPodcasts()
            }
        }

        override fun onError(id: String) {
            val msg = "sectionPodcast fragment subscription onError, id=" + id
            Log.e(HourByHourListFragment.TAG, msg)
            crashReporter.logException(msg)
        }
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            if (metadata == null) {
                return
            }
            adapter.notifyDataSetChanged()
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            adapter.notifyDataSetChanged()
        }
    }

    private fun handleError(d: MediaDescriptionCompat) {
        crashReporter.logException(d.description.toString())
        empty_list.visibility = View.VISIBLE
        swipe_refresh.isRefreshing = false
        recycler_view.visibility = View.GONE
    }
}
