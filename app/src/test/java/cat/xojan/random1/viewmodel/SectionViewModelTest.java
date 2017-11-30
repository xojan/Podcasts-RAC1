package cat.xojan.random1.viewmodel;

import org.junit.Before;

import cat.xojan.random1.domain.entities.Section;
import cat.xojan.random1.ui.BaseActivity;

import static cat.xojan.random1.testutil.DataKt.getSection1;
import static org.mockito.Mockito.mock;

public class SectionViewModelTest {

    private BaseActivity mActivity;
    //private SectionViewModel mViewModel;
    private Section mSection;

    @Before
    public void setUp() {
        mActivity = mock(BaseActivity.class);
        mSection = getSection1();
        //mViewModel = new SectionViewModel(mActivity, mSection, getProgram1());
    }

    /*@Test
    public void read_title() {
        assertEquals(mViewModel.getTitle(), mSection.getTitle());
    }

    @Test
    public void read_image_url() {
        assertEquals(mViewModel.getImageUrl(), mSection.getImageUrl());
    }

    @Test
    public void click_section() {
        mViewModel.onClickSection().onClick(new View(mActivity));
        verify(mActivity).addFragment(any(SectionPodcastListFragment.class),
                eq(SectionPodcastListFragment.TAG), eq(true));
    }*/
}
