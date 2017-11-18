package cat.xojan.random1.ui.browser

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
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

    private var mAdapter: PodcastListAdapter? = null
    private val mCompositeDisposable = CompositeDisposable()

    private var mRecyclerView: RecyclerView? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    private var mEmptyList: TextView? = null

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

        mAdapter = PodcastListAdapter(mProgramDataInteractor!!)
        mRecyclerView!!.adapter = mAdapter

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
        mAdapter!!.podcasts = podcasts
        mRecyclerView!!.visibility = View.VISIBLE
    }

    private fun updateViewWithDownloaded(podcasts: List<Podcast>) {
        mAdapter!!.updateWithDownloaded(podcasts)
    }

    private fun showSections() {
        mPodcastsViewModel!!.selectedSection(true)
        val sectionListFragment = SectionFragment.newInstance(arguments.get(ARG_PROGRAM) as Program)
        (activity as BaseActivity).addFragment(sectionListFragment, SectionFragment.TAG, true)
    }

    companion object {
        val TAG = HourByHourListFragment::class.java.simpleName
        val ARG_PROGRAM = "program_param"

        fun newInstance(program: Program): HourByHourListFragment {
            val args = Bundle()
            args.putParcelable(ARG_PROGRAM, program)

            val hourByHourListFragment = HourByHourListFragment()
            hourByHourListFragment.arguments = args

            return hourByHourListFragment
        }
    }
}
