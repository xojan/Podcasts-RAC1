package cat.xojan.random1.ui.browser

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class BrowserViewModel @Inject
constructor(
        private val podcastInteractor: PodcastDataInteractor,
        private val programInteractor: ProgramDataInteractor) {

    fun getPodcastStateUpdates(): PublishSubject<List<MediaBrowserCompat.MediaItem>> {
         return podcastInteractor.getPodcastStateUpdates()
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

    fun updatePodcastState(loaded: List<MediaBrowserCompat.MediaItem>):
            List<MediaBrowserCompat.MediaItem> {
        val updated = podcastInteractor.fetchDownloadedPodcasts()

        for (mediaItem in loaded) {
            val podcast = mediaItem.description
            podcast.extras?.putString(Podcast.PODCAST_FILE_PATH, null)
            podcast.extras?.putSerializable(Podcast.PODCAST_STATE, Podcast.State.LOADED)
        }

        for (mediaItem in updated) {
            val updatedPodcast = mediaItem.description
            val currentPodcast = loaded.firstOrNull { it.mediaId == mediaItem.mediaId }
            currentPodcast?.let {
                val description = currentPodcast.description
                description.extras?.putString(Podcast.PODCAST_FILE_PATH,
                        updatedPodcast.extras?.getString(Podcast.PODCAST_FILE_PATH))
                description.extras?.putSerializable(Podcast.PODCAST_STATE,
                        updatedPodcast.extras?.getSerializable(Podcast.PODCAST_STATE)
                                as Podcast.State)
            }
        }
        return loaded
    }

    /*fun exportPodcasts(): Observable<Boolean> {
        return mProgramDataInteractor.exportPodcasts()
    }*/
}
