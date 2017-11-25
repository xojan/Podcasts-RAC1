package cat.xojan.random1.ui.browser

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.injection.component.BrowseComponent
import cat.xojan.random1.ui.BaseActivity
import cat.xojan.random1.ui.BaseFragment
import cat.xojan.random1.ui.IsMediaBrowserFragment
import cat.xojan.random1.ui.MediaBrowserProvider
import cat.xojan.random1.ui.home.ProgramFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.recycler_view_fragment.*
import javax.inject.Inject

class SectionFragment : BaseFragment(), IsMediaBrowserFragment {

    @Inject internal lateinit var crashReporter: CrashReporter

    private lateinit var adapter: SectionListAdapter
    private val mCompositeDisposable = CompositeDisposable()

    private var mediaBrowserProvider: MediaBrowserProvider? = null

    companion object {
        val TAG = SectionFragment::class.java.simpleName
        val ARG_PROGRAM = "program_param"

        fun newInstance(program: MediaBrowserCompat.MediaItem): SectionFragment {
            val args = Bundle()
            args.putParcelable(ARG_PROGRAM, program)

            val sectionFragment = SectionFragment()
            sectionFragment.arguments = args

            return sectionFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaBrowserProvider = context as BaseActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(BrowseComponent::class.java).inject(this)
        val view = inflater!!.inflate(R.layout.recycler_view_fragment, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(activity)

        adapter = SectionListAdapter(activity as BrowseActivity)
        recycler_view.adapter = adapter
    }

    /*override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.sections, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }*/

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                handleOnBackPressed()
                return true
            }
            R.id.action_hour_by_hour -> {
                showHourByHour()
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
    }

    private fun mediaId(): String? {
        val mediaItem = arguments?.getParcelable<MediaBrowserCompat.MediaItem>(ARG_PROGRAM)
        mediaItem?.let {
            return mediaItem.mediaId
        }
        return null
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
            val mediaId = mediaId() ?: ProgramFragment.MEDIA_ID_ROOT
            mediaBrowser.unsubscribe(mediaId)
            mediaBrowser.subscribe(mediaId, mediaBrowserSubscriptionCallback)
        }
    }

    private val mediaBrowserSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String,
                                      children: List<MediaBrowserCompat.MediaItem>) {
            Log.d(HourByHourListFragment.TAG, "onChildrenLoaded, parentId=" + parentId + "  count=" + children.size)
            adapter.sections = children
        }

        override fun onError(id: String) {
            val msg = "section fragment subscription onError, id=" + id
            Log.e(TAG, msg)
            crashReporter.logException(msg)
        }
    }

    /*private fun loadSections() {
        mCompositeDisposable.add(mSectionsViewModel!!.loadSections(mProgram)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<List<Section>> { this.updateView(it) }))
    }*/

    private fun showHourByHour() {
        /* mSectionsViewModel.selectedSection(false);
        HourByHourListFragment hourByHourListFragment = HourByHourListFragment.Companion
                .newInstance((Program) getArguments().get(ARG_PROGRAM));
        ((BaseActivity) getActivity()).addFragment(hourByHourListFragment,
                HourByHourListFragment.Companion.getTAG(), true);*/
    }
}
