package cat.xojan.random1.feature.home

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.feature.BaseFragment
import cat.xojan.random1.feature.IsMediaBrowserFragment
import cat.xojan.random1.feature.MediaBrowserProvider
import cat.xojan.random1.feature.MediaPlayerBaseActivity
import cat.xojan.random1.injection.component.HomeComponent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragments_programs.*
import javax.inject.Inject


class ProgramFragment: BaseFragment(), IsMediaBrowserFragment {

    companion object {
        val TAG = ProgramFragment::class.simpleName
        val MEDIA_ID_ROOT = "__PROGRAMS__"
        val MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__"
    }

    @Inject internal lateinit var crashReporter: CrashReporter

    private val mCompositeDisposable = CompositeDisposable()
    private lateinit var adapter: ProgramListAdapter
    private var mediaBrowserProvider: MediaBrowserProvider? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaBrowserProvider = context as MediaPlayerBaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(HomeComponent::class.java).inject(this)
        return inflater.inflate(R.layout.fragments_programs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        programs_progress_bar.visibility = VISIBLE
        programs_load_button.setOnClickListener({onMediaControllerConnected()})
        adapter = ProgramListAdapter()
        programs_recycler_view.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    programs_recycler_view.stopScroll()
                }
            }
        })
        programs_recycler_view.adapter = adapter
        setLayoutManager(resources.configuration.orientation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeDisposable.clear()
    }

    private fun setLayoutManager(orientation: Int) {
        var columns = 2
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) columns = 3
        programs_recycler_view.layoutManager = GridLayoutManager(activity, columns)
    }

    private fun showPrograms() {
        programs_empty_view.visibility = GONE
        programs_progress_bar.visibility = GONE
        programs_recycler_view.visibility = VISIBLE
    }

    private fun handleError(d: MediaDescriptionCompat) {
        crashReporter.logException(d.description.toString())
        programs_empty_view.visibility = VISIBLE
        programs_progress_bar.visibility = GONE
        programs_recycler_view.visibility = GONE
    }

    override fun onMediaControllerConnected() {
        if (isDetached) {
            return
        }
        if (adapter.programs.isEmpty()) {
            programs_empty_view.visibility = GONE
            programs_progress_bar.visibility = VISIBLE
            programs_recycler_view.visibility = GONE
        }


        val mediaId = MEDIA_ID_ROOT
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
            mediaBrowser.unsubscribe(mediaId)
            mediaBrowser.subscribe(mediaId, mediaBrowserSubscriptionCallback)
        }
    }

    override fun onStart() {
        super.onStart()
        // fetch browsing information to fill the recycler view
        val mediaBrowser =  mediaBrowserProvider?.getMediaBrowser()
        mediaBrowser?.let {
            Log.d(TAG, "onStart, onConnected=" + mediaBrowser.isConnected)
            if (mediaBrowser.isConnected) {
                Log.d(TAG, "onStart, mediaId=" + mediaBrowser.root)
                onMediaControllerConnected()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val mediaBrowser =  mediaBrowserProvider?.getMediaBrowser()
        mediaBrowser?.let {
            if (mediaBrowser.isConnected) {
                mediaBrowser.unsubscribe(mediaBrowser.root)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mediaBrowserProvider = null
    }

    private val mediaBrowserSubscriptionCallback =
            object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                        children: List<MediaBrowserCompat.MediaItem>) {
            if (isChildrenError(children)) {
                handleError(children[0].description)
            } else {
                Log.d(TAG, "onChildrenLoaded, parentId=" + parentId + "  count=" + children.size)
                adapter.programs = children
                showPrograms()
            }
        }

        override fun onError(id: String) {
            val msg = "program fragment subscription onError, id=" + id
            Log.e(TAG, msg)
            crashReporter.logException(msg)
        }
    }
}