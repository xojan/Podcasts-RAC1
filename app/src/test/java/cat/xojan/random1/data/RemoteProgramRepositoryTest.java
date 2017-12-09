package cat.xojan.random1.data;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import cat.xojan.random1.domain.model.Podcast;
import cat.xojan.random1.domain.model.PodcastData;
import cat.xojan.random1.domain.model.ProgramData;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

import static cat.xojan.random1.testutil.DataKt.getPodcastList;
import static cat.xojan.random1.testutil.DataKt.getProgramList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoteProgramRepositoryTest {

    private RemoteProgramRepository mRemoteRepository;
    private Rac1ApiService mService;

    @Before
    public void setUp() {
        mService = mock(Rac1ApiService.class);
        mRemoteRepository = new RemoteProgramRepository(mService);
    }

    @Test @Ignore
    public void get_program_list() throws IOException {
        when(mService.getProgramData().execute().body()).thenReturn(getProgramData());

        assertEquals(mRemoteRepository.getPrograms(), getProgramList());
    }

    @Test
    public void get_podcasts_list_by_program() throws IOException {
        when(mService.getPodcastData(anyString())).thenReturn(Flowable.just(getPodcastData()));
        TestSubscriber<List<Podcast>> testSubscriber = new TestSubscriber<>();

        mRemoteRepository.getPodcast("programId", null).subscribe(testSubscriber);
        testSubscriber.assertValue(getPodcastList());
    }

    @Test
    public void get_podcasts_list_by_section() throws IOException {
        when(mService.getPodcastData(anyString(), anyString())).thenReturn(Flowable.just(getPodcastData()));
        TestSubscriber<List<Podcast>> testSubscriber = new TestSubscriber<>();

        mRemoteRepository.getPodcast("programId", "sectionId").subscribe(testSubscriber);
        testSubscriber.assertValue(getPodcastList());
    }

    private ProgramData getProgramData() {
        ProgramData programData = new ProgramData();
        programData.setPrograms(getProgramList());
        return programData;
    }

    private PodcastData getPodcastData() {
        PodcastData podcastData = new PodcastData();
        podcastData.setPodcasts(getPodcastList());
        return podcastData;
    }
}

