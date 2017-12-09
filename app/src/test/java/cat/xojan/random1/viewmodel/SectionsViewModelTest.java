package cat.xojan.random1.viewmodel;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import cat.xojan.random1.domain.model.Program;
import cat.xojan.random1.domain.model.Section;
import cat.xojan.random1.domain.interactor.ProgramDataInteractor;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static cat.xojan.random1.testutil.DataKt.getProgram1;
import static cat.xojan.random1.testutil.DataKt.getSectionList;
import static cat.xojan.random1.testutil.DataKt.getSectionListResult1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SectionsViewModelTest {

    private ProgramDataInteractor mProgramDataInteractor;
    private SectionsViewModel mViewModel;

    @Before
    public void setUp() {
        mProgramDataInteractor = mock(ProgramDataInteractor.class);
        mViewModel = new SectionsViewModel(mProgramDataInteractor);
    }

    @Test
    public void load_sections_successfully() {
        when(mProgramDataInteractor.loadSections(any(Program.class))).thenReturn(Observable.just
                (getSectionList()));
        TestObserver<List<Section>> testSubscriber = new TestObserver<>();
        mViewModel.loadSections(getProgram1()).subscribe(testSubscriber);

        testSubscriber.assertValue(getSectionListResult1());
    }

    @Test
    public void set_selected_mode() {
        mViewModel.selectedSection(true);
        verify(mProgramDataInteractor).setSectionSelected(true);
    }
}
