package cat.xojan.random1.feature.home

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.feature.BaseFragment
import cat.xojan.random1.feature.IsMediaBrowserFragment
import cat.xojan.random1.feature.MediaBrowserProvider
import cat.xojan.random1.feature.MediaPlayerBaseActivity
import cat.xojan.random1.feature.browser.BrowserViewModel
import cat.xojan.random1.feature.browser.PodcastListAdapter
import cat.xojan.random1.injection.component.HomeComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.recycler_view_fragment.*
import javax.inject.Inject

class DownloadsFragment : BaseFragment(), IsMediaBrowserFragment {

    companion object {
        val TAG: String = DownloadsFragment::class.simpleName.toString()
        val MEDIA_ID_DOWNLOADS = "/DOWNLOADS"
    }

    @Inject
    internal lateinit var viewModel: BrowserViewModel
    @Inject
    internal lateinit var crashReporter: CrashReporter

    private val compositeDisposable = CompositeDisposable()
    private lateinit var adapter: PodcastListAdapter
    private var mediaBrowserProvider: MediaBrowserProvider? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaBrowserProvider = context as MediaPlayerBaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(HomeComponent::class.java).inject(this)
        return inflater.inflate(R.layout.recycler_view_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(activity)
        swipe_refresh.isEnabled = false
        empty_list.text = getString(R.string.no_downloaded_podcasts)

        adapter = PodcastListAdapter(viewModel, activity as Activity)
        recycler_view.adapter = adapter
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
                        { onMediaControllerConnected() },
                        { e -> crashReporter.logException(e) }
                ))
    }

    override fun onPause() {
        super.onPause()
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
        return MEDIA_ID_DOWNLOADS
    }

    override fun onMediaControllerConnected() {
        if (isDetached) {
            return
        }
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

        // Add MediaController callback so we can redraw the list when metadata changes:
        val controller = MediaControllerCompat.getMediaController(activity as Activity)
        controller?.registerCallback(mediaControllerCallback)
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            if (children.isEmpty()) {
                empty_list.visibility = View.VISIBLE
                recycler_view.visibility = View.GONE
            } else {
                empty_list.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
                adapter.podcasts = children
            }
        }

        override fun onError(id: String) {
            val msg = "downloads fragment subscription onError, id=$id"
            Log.e(TAG, msg)
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
}
