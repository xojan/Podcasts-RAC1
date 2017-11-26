package cat.xojan.random1.ui.home

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList

import javax.inject.Inject

import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.injection.component.HomeComponent
import cat.xojan.random1.ui.BaseFragment
import cat.xojan.random1.ui.browser.PodcastListAdapter
import cat.xojan.random1.ui.browser.BrowserViewModel
import io.reactivex.disposables.CompositeDisposable

class DownloadsFragment : BaseFragment() {

    companion object {
        val TAG = DownloadsFragment::class.simpleName
    }

    @Inject internal lateinit var mPodcastsViewModel: BrowserViewModel
    @Inject internal lateinit var mProgramDataInteractor: ProgramDataInteractor

    private val mCompositeDisposable = CompositeDisposable()
    private var mAdapter: PodcastListAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    private var mEmptyList: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getComponent(HomeComponent::class.java).inject(this)
        val view = inflater.inflate(R.layout.recycler_view_fragment, container, false)

        mSwipeRefresh = view.findViewById(R.id.swipe_refresh)
        mRecyclerView = view.findViewById(R.id.recycler_view)
        mEmptyList = view.findViewById(R.id.empty_list)

        mRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        mSwipeRefresh!!.isEnabled = false
        mEmptyList!!.text = getString(R.string.no_downloaded_podcasts)

        mAdapter = PodcastListAdapter()
        mRecyclerView!!.adapter = mAdapter

        return view
    }

    /* override fun onResume() {
        super.onResume()
        mCompositeDisposable.add(mPodcastsViewModel!!.loadDownloadedPodcasts()
                .subscribeOn(Schedulers.io())
                /*.flatMap(Observable::from)
                .filter(podcast -> podcast.getState().equals(Podcast.State.DOWNLOADED))
                .toSortedList((podcast, podcast2) -> podcast2.getDate().compareTo(podcast.getDate()))*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<List<Podcast>> { this.updateView(it) }))
        mCompositeDisposable.add(mPodcastsViewModel!!.downloadedPodcastsUpdates
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.updateView(it) }))
    } */

    override fun onPause() {
        super.onPause()
        mCompositeDisposable.clear()
    }

    private fun updateView(podcasts: List<Podcast>) {
        val downloaded = ArrayList<Podcast>()
        if (podcasts.isEmpty()) {
            mEmptyList!!.visibility = View.VISIBLE
        } else {
            for (p in podcasts) {
                if (p.state == Podcast.State.DOWNLOADED) {
                    downloaded.add(p)
                }
            }
            /*Collections.sort(downloaded) { (_, _, _, dateTime), (_, _, _, dateTime) ->
                dateTime.compareTo(dateTime)
            }*/
            mEmptyList!!.visibility = View.GONE
        }

        //mAdapter.setPodcasts(downloaded);
    }
}
