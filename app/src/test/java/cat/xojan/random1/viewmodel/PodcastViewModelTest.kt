package cat.xojan.random1.viewmodel

import android.content.Context
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.testutil.podcast1
import org.junit.Before
import org.mockito.Mockito.mock


class PodcastViewModelTest {

    private var mContext: Context? = null
    private var mPodcast: Podcast? = null
    private var mProgramDataInteractor: ProgramDataInteractor? = null

    @Before
    fun setUp() {
        mContext = mock<Context>(Context::class.java)
        mPodcast = podcast1
        mProgramDataInteractor = mock<ProgramDataInteractor>(ProgramDataInteractor::class.java)

        //mViewModel = PodcastViewModel(mContext, mPodcast, mProgramDataInteractor)
    }

    /*@Test
    fun read_title() {
        assertEquals(mViewModel!!.title, mPodcast!!.title)
    }

    @Test
    fun read_image_url() {
        assertEquals(mViewModel!!.imageUrl, mPodcast!!.imageUrl)
    }

    @Test
    fun read_state() {
        assertEquals(mViewModel!!.state, mPodcast!!.state)
    }

    @Test
    fun click_podcast() {
        mViewModel!!.onClickPodcast().onClick(any<View>())
        verify<Context>(mContext).startActivity(any<Intent>())
    }

    @Test
    fun click_podcast_with_icon_downloading() {
        mPodcast!!.state = Podcast.State.DOWNLOADING
        mViewModel!!.onClickIcon().onClick(View(mContext))
        verify<ProgramDataInteractor>(mProgramDataInteractor).refreshDownloadedPodcasts()
    }

    @Test
    fun click_podcast_with_icon_downloaded() {
        mPodcast!!.state = Podcast.State.DOWNLOADED
        mViewModel!!.onClickIcon().onClick(View(mContext))
        verify<ProgramDataInteractor>(mProgramDataInteractor).deleteDownload(mPodcast)
        verify<ProgramDataInteractor>(mProgramDataInteractor).refreshDownloadedPodcasts()
    }

    @Test
    fun click_podcast_with_icon_loaded() {
        mPodcast!!.state = Podcast.State.LOADED
        mViewModel!!.onClickIcon().onClick(View(mContext))
        verify<ProgramDataInteractor>(mProgramDataInteractor).download(mPodcast)
        verify<ProgramDataInteractor>(mProgramDataInteractor).refreshDownloadedPodcasts()
    }*/
}