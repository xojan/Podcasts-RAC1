package cat.xojan.random1.ui.browser

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class BrowserViewModel @Inject
constructor(
        private val podcastInteractor: PodcastDataInteractor,
        private val programInteractor: ProgramDataInteractor) {

    fun downloadedPodcastsUpdates(): PublishSubject<List<MediaBrowserCompat.MediaItem>> {
        return podcastInteractor.getDownloadedPodcastsUpdates()
    }

    fun selectedSection(b: Boolean) = podcastInteractor.setSectionSelected(b)

    fun isSectionSelected(): Boolean = podcastInteractor.isSectionSelected()

    fun hasSections(programId: String?): Boolean {
        programId?.let {
            return programInteractor.hasSections(programId)
        }
        return false
    }

    fun downloadPodcast(podcast: MediaDescriptionCompat) {
        podcastInteractor.download(podcast)
    }

    fun deletePodcast(podcast: MediaDescriptionCompat) {
        podcastInteractor.deleteDownload(podcast)
    }

    fun refreshDownloadedPodcast() {
        podcastInteractor.refreshDownloadedPodcasts()
    }

    fun loadDownloadedPodcasts(): Single<List<MediaBrowserCompat.MediaItem>> {
        return podcastInteractor.getDownloadedPodcasts()
    }

    /* fun loadDownloadedPodcasts(): Single<List<Podcast>> {
        return mProgramDataInteractor.getDownloadedPodcasts()
    } */

    /*fun loadPodcasts(program: Program, section: Section,
                     refresh: Boolean): Single<List<Podcast>> {
        val loadedPodcasts = mProgramDataInteractor.loadPodcasts(program, section, refresh)
                .flatMapIterable { list -> list }
                .map { podcast ->
                    podcast.programId = program.id
                    podcast.imageUrl = program.imageUrl()
                    podcast
                }
                .toList()

        val downloadedPodcasts = mProgramDataInteractor.getDownloadedPodcasts()

        return Single.zip(loadedPodcasts, downloadedPodcasts, BiFunction {
            loaded, downloaded ->
            for (podcast in loaded) {
                podcast.filePath = null
                podcast.state = Podcast.State.LOADED
            }

            for (download in downloaded) {
                val index = loaded.indexOf(download)
                if (index >= 0) {
                    val podcast = loaded.get(index)
                    podcast.filePath = download.filePath
                    podcast.state = download.state
                }
            }
            loaded
        })
    }*/

    /*fun exportPodcasts(): Observable<Boolean> {
        return mProgramDataInteractor.exportPodcasts()
    }*/
}
