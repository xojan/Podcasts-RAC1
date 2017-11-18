package cat.xojan.random1.ui.browser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import cat.xojan.random1.R;
import cat.xojan.random1.domain.entities.Program;
import cat.xojan.random1.domain.entities.Section;
import cat.xojan.random1.injection.component.BrowseComponent;
import cat.xojan.random1.ui.BaseActivity;
import cat.xojan.random1.ui.BaseFragment;
import cat.xojan.random1.viewmodel.SectionsViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SectionFragment extends BaseFragment {

    public static final String TAG = SectionFragment.class.getSimpleName();
    private static final String ARG_PROGRAM = "program_arg";

    @Inject SectionsViewModel mSectionsViewModel;

    private SectionListAdapter mAdapter;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Program mProgram;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRecyclerView;

    public static SectionFragment newInstance(Program program) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PROGRAM, program);

        SectionFragment sectionListFragment = new SectionFragment();
        sectionListFragment.setArguments(args);

        return sectionListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getComponent(BrowseComponent.class).inject(this);
        View view = inflater.inflate(R.layout.recycler_view_fragment, container, false);
        mProgram = (Program) getArguments().get(ARG_PROGRAM);

        setHasOptionsMenu(true);
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mSwipeRefresh.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SectionListAdapter((BaseActivity) getActivity(), mProgram);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sections, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadSections();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleOnBackPressed();
                return true;
            case R.id.action_hour_by_hour:
                showHourByHour();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCompositeDisposable.clear();
    }

    private void loadSections() {
        mCompositeDisposable.add(mSectionsViewModel.loadSections(mProgram)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView));
    }

    private void updateView(List<Section> sections) {
        mAdapter.setSections(sections);
    }

    private void showHourByHour() {
       /* mSectionsViewModel.selectedSection(false);
        HourByHourListFragment hourByHourListFragment = HourByHourListFragment.Companion
                .newInstance((Program) getArguments().get(ARG_PROGRAM));
        ((BaseActivity) getActivity()).addFragment(hourByHourListFragment,
                HourByHourListFragment.Companion.getTAG(), true);*/
    }
}
