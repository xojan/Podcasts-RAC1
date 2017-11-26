package cat.xojan.random1.ui.browser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import cat.xojan.random1.R;
import cat.xojan.random1.domain.entities.CrashReporter;
import cat.xojan.random1.domain.entities.Podcast;
import cat.xojan.random1.domain.entities.Section;
import cat.xojan.random1.domain.interactor.ProgramDataInteractor;
import cat.xojan.random1.injection.component.BrowseComponent;
import cat.xojan.random1.ui.BaseActivity;
import cat.xojan.random1.ui.BaseFragment;
import io.reactivex.disposables.CompositeDisposable;

public class PodcastListFragment extends BaseFragment {

    public static final String TAG = PodcastListFragment.class.getSimpleName();
    public static final String ARG_PROGRAM = "program_param";
    public static final String ARG_SECTION = "section_param";

    @Inject
    BrowserViewModel mPodcastsViewModel;
    @Inject ProgramDataInteractor mProgramDataInteractor;
    @Inject CrashReporter mCrashReporter;

    private ActionBar mActionBar;
    private PodcastListAdapter mAdapter;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private TextView mEmptyList;

    public static PodcastListFragment newInstance(Section section) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_SECTION, section);
        // args.putParcelable(ARG_PROGRAM, program);

        PodcastListFragment podcastListFragment = new PodcastListFragment();
        podcastListFragment.setArguments(args);

        return podcastListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getComponent(BrowseComponent.class).inject(this);
        View view = inflater.inflate(R.layout.recycler_view_fragment, container, false);

        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyList = view.findViewById(R.id.empty_list);

        mSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefresh.setOnRefreshListener(() -> showPodcasts(true));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new PodcastListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        showPodcasts(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* @Override
    public void onResume() {
        super.onResume();
        mCompositeDisposable.add(mPodcastsViewModel.getDownloadedPodcastsUpdates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateViewWithDownloaded));
        getActivity().setTitle(((Section) getArguments().get(ARG_SECTION)).getTitle());
        showBackArrow();
    } */

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setTitle(getString(R.string.app_name));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCompositeDisposable.clear();
    }

    private void showPodcasts(final boolean refresh) {
        /* new Handler().postDelayed(() -> {
            mSwipeRefresh.setRefreshing(true);
            Program program = getArguments().getParcelable(PodcastListFragment.ARG_PROGRAM);
            Section section = getArguments().getParcelable(PodcastListFragment.ARG_SECTION);

            mCompositeDisposable.add(mPodcastsViewModel.loadPodcasts(program, section, refresh)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::updateView,
                            this::handleError));
        }, 0); */
    }

    private void updateView(List<Podcast> podcasts) {
        mSwipeRefresh.setRefreshing(false);
        //mAdapter.setPodcasts(podcasts);
        mEmptyList.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void updateViewWithDownloaded(List<Podcast> podcasts) {
        mAdapter.updateWithDownloaded(podcasts);
    }

    private void handleError(Throwable throwable) {
        mSwipeRefresh.setRefreshing(false);
        mEmptyList.setVisibility(View.VISIBLE);
        mCrashReporter.logException(throwable);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void showBackArrow() {
        setHasOptionsMenu(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }
}
