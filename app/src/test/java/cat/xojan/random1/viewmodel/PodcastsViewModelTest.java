package cat.xojan.random1.viewmodel;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cat.xojan.random1.domain.model.Podcast;
import cat.xojan.random1.domain.model.Program;
import cat.xojan.random1.domain.model.Section;
import cat.xojan.random1.domain.interactor.ProgramDataInteractor;
import cat.xojan.random1.feature.browser.BrowserViewModel;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static cat.xojan.random1.testutil.DataKt.getPodcast1;
import static cat.xojan.random1.testutil.DataKt.getPodcast3;
import static cat.xojan.random1.testutil.DataKt.getPodcastList;
import static cat.xojan.random1.testutil.DataKt.getProgram1;
import static cat.xojan.random1.testutil.DataKt.getSection1;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PodcastsViewModelTest {

    private ProgramDataInteractor mProgramDataInteractor;
    private BrowserViewModel mViewModel;

    @Before
    public void setUp() {
        mProgramDataInteractor = mock(ProgramDataInteractor.class);
        mViewModel = new BrowserViewModel(mProgramDataInteractor);
    }

    @Test
    public void load_downloaded_podcasts_successfully() {
        when(mProgramDataInteractor.getDownloadedPodcasts()).thenReturn(Single.just(getPodcastList()));
        TestObserver<List<Podcast>> testSubscriber = new TestObserver<>();

        mViewModel.loadDownloadedPodcasts().subscribe(testSubscriber);
        testSubscriber.assertValue(getPodcastList());
    }

    @Test
    public void fail_to_load_downloaded_podcasts() {
        when(mProgramDataInteractor.getDownloadedPodcasts()).thenReturn(Single.error(new IOException()));
        TestObserver<List<Podcast>> testSubscriber = new TestObserver<>();

        mViewModel.loadDownloadedPodcasts().subscribe(testSubscriber);
        testSubscriber.assertError(IOException.class);
    }

    @Test
    public void load_podcasts_successfully() {
        Program program = getProgram1();
        Section section = getSection1();

        when(mProgramDataInteractor.loadPodcasts(program, section, false)).thenReturn(Flowable.just(getPodcastList()));
        when(mProgramDataInteractor.getDownloadedPodcasts()).thenReturn(Single.just(getDownloadedPodcastList()));
        TestObserver<List<Podcast>> testSubscriber = new TestObserver<>();
        mViewModel.loadPodcasts(program, section, false).subscribe(testSubscriber);

        testSubscriber.assertValue(getPodcastList());
    }

    @Test
    public void fail_to_load_podcasts() {
        when(mProgramDataInteractor.getDownloadedPodcasts()).thenReturn(Single.error(new IOException()));
        TestObserver<List<Podcast>> testSubscriber = new TestObserver<>();

        mViewModel.loadDownloadedPodcasts().subscribe(testSubscriber);
        testSubscriber.assertError(IOException.class);
    }

    @Test
    public void get_downloaded_podcasts_update() {
        PublishSubject<List<Podcast>> ps = PublishSubject.create();

        when(mProgramDataInteractor.getDownloadedPodcastsUpdates()).thenReturn(ps);
        TestObserver<List<Podcast>> testSubscriber = new TestObserver<>();
        mViewModel.getDownloadedPodcastsUpdates().subscribe(testSubscriber);
        ps.onNext(getDownloadedPodcastList());

        testSubscriber.assertValue(getDownloadedPodcastList());
    }

    @Test
    public void set_selected_mode() {
        mViewModel.selectedSection(true);
        verify(mProgramDataInteractor).setSectionSelected(true);
    }

    private List<Podcast> getDownloadedPodcastList() {
        List<Podcast> podcasts = new ArrayList<>();

        podcasts.add(getPodcast1());
        podcasts.add(getPodcast3());

        return podcasts;
    }
}
