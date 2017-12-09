package cat.xojan.random1.viewmodel;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import cat.xojan.random1.domain.model.Program;
import cat.xojan.random1.domain.interactor.ProgramDataInteractor;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static cat.xojan.random1.testutil.DataKt.getProgramList;
import static cat.xojan.random1.testutil.DataKt.getProgramListResult1;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProgramsViewModelTest {

    private ProgramDataInteractor mProgramDataInteractor;
    private ProgramsViewModel mViewModel;

    @Before
    public void setUp() {
        mProgramDataInteractor = mock(ProgramDataInteractor.class);
        mViewModel = new ProgramsViewModel(mProgramDataInteractor);
    }

    @Test
    public void load_programs_successfully() {
        when(mProgramDataInteractor.loadPrograms()).thenReturn(Observable.just(getProgramList()));
        TestObserver<List<Program>> testSubscriber = new TestObserver<>();

        mViewModel.loadPrograms().subscribe(testSubscriber);
        testSubscriber.assertValue(getProgramListResult1());
    }

    @Test
    public void fail_to_load_programs() {
        when(mProgramDataInteractor.loadPrograms()).thenReturn(Observable.error(new IOException()));
        TestObserver<List<Program>> testSubscriber = new TestObserver<>();

        mViewModel.loadPrograms().subscribe(testSubscriber);
        testSubscriber.assertError(IOException.class);
    }
}
