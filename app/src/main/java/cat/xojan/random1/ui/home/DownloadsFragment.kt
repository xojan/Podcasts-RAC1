package cat.xojan.random1.ui.home

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cat.xojan.random1.R
import cat.xojan.random1.domain.entities.CrashReporter
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.Podcast.Companion.PODCAST_STATE
import cat.xojan.random1.injection.component.HomeComponent
import cat.xojan.random1.ui.BaseFragment
import cat.xojan.random1.ui.browser.BrowserViewModel
import cat.xojan.random1.ui.browser.PodcastListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.recycler_view_fragment.*
import javax.inject.Inject

class DownloadsFragment : BaseFragment() {

    companion object {
        val TAG = DownloadsFragment::class.simpleName
    }

    @Inject internal lateinit var viewModel: BrowserViewModel
    @Inject internal lateinit var crashReporter: CrashReporter

    private val compositeDisposable = CompositeDisposable()
    private lateinit var adapter: PodcastListAdapter

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

        adapter = PodcastListAdapter(viewModel)
        recycler_view.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(viewModel.loadDownloadedPodcasts()
                .subscribeOn(Schedulers.newThread())
                /*.flatMap(Observable::from)
                .filter(podcast -> podcast.getState().equals(Podcast.State.DOWNLOADED))
                .toSortedList((podcast, podcast2) -> podcast2.getDate().compareTo(podcast.getDate()))*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {p -> this.updateView(p)},
                        {e -> this.onError(e)}
                ))
        compositeDisposable.add(viewModel.downloadedPodcastsUpdates()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {p -> this.updateView(p)},
                        {e -> this.onError(e)}
                ))
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    private fun updateView(podcasts: List<MediaBrowserCompat.MediaItem>) {
        val downloaded = mutableListOf<MediaBrowserCompat.MediaItem>()
        if (podcasts.isEmpty()) {
            empty_list.visibility = View.VISIBLE
        } else {
            podcasts.filterTo(downloaded) {
                it.description.extras?.getSerializable(PODCAST_STATE)
                        as Podcast.State == Podcast.State.DOWNLOADED
            }
            /*Collections.sort(downloaded) { (_, _, _, dateTime), (_, _, _, dateTime) ->
                dateTime.compareTo(dateTime)
            }*/
            empty_list.visibility = View.GONE
        }
        adapter.podcasts = downloaded
    }

    private fun onError(e: Throwable) {
        crashReporter.logException(e)
    }
}
