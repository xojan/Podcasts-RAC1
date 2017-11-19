package cat.xojan.random1.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import cat.xojan.random1.R;
import cat.xojan.random1.domain.entities.Podcast;
import cat.xojan.random1.domain.interactor.ProgramDataInteractor;
import cat.xojan.random1.injection.component.HomeComponent;
import cat.xojan.random1.ui.BaseFragment;
import cat.xojan.random1.ui.browser.PodcastListAdapter;
import cat.xojan.random1.viewmodel.PodcastsViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadsFragment extends BaseFragment {

    @Inject PodcastsViewModel mPodcastsViewModel;
    @Inject ProgramDataInteractor mProgramDataInteractor;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private PodcastListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private TextView mEmptyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getComponent(HomeComponent.class).inject(this);
        View view = inflater.inflate(R.layout.recycler_view_fragment, container, false);

        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyList = view.findViewById(R.id.empty_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefresh.setEnabled(false);
        mEmptyList.setText(getString(R.string.no_downloaded_podcasts));

        mAdapter = new PodcastListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeDisposable.add(mPodcastsViewModel.loadDownloadedPodcasts()
                .subscribeOn(Schedulers.io())
                /*.flatMap(Observable::from)
                .filter(podcast -> podcast.getState().equals(Podcast.State.DOWNLOADED))
                .toSortedList((podcast, podcast2) -> podcast2.getDate().compareTo(podcast.getDate()))*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView));
        mCompositeDisposable.add(mPodcastsViewModel.getDownloadedPodcastsUpdates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView));
    }

    @Override
    public void onPause() {
        super.onPause();
        mCompositeDisposable.clear();
    }

    private void updateView(List<Podcast> podcasts) {
        List<Podcast> downloaded = new ArrayList<>();
        if (podcasts.isEmpty()) {
            mEmptyList.setVisibility(View.VISIBLE);
        } else {
            for (Podcast p : podcasts) {
                if (p.getState().equals(Podcast.State.DOWNLOADED)) {
                    downloaded.add(p);
                }
            }
            Collections.sort(downloaded, (podcast, podcast2) -> podcast2.getDateTime().compareTo(podcast
                    .getDateTime()));
            mEmptyList.setVisibility(View.GONE);
        }

        //mAdapter.setPodcasts(downloaded);
    }
}
