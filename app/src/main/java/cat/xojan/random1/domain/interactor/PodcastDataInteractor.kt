package cat.xojan.random1.domain.interactor

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.Podcast.Companion.PODCAST_FILE_PATH
import cat.xojan.random1.domain.repository.DownloadPodcastRepository
import cat.xojan.random1.domain.repository.PodcastPreferencesRepository
import cat.xojan.random1.domain.repository.PodcastRepository
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.ArrayList
import java.util.HashSet
import javax.inject.Inject

class PodcastDataInteractor @Inject constructor(
        private val programRepo: ProgramRepository,
        private val podcastRepo: PodcastRepository,
        private val podcastPref: PodcastPreferencesRepository,
        private val downloadManager: DownloadManager,
        private val context: Context,
        private val downloadRepo: DownloadPodcastRepository)
{
    private val TAG = PodcastDataInteractor::class.simpleName
    private val PODCAST_DOWNLOAD_REFERENCE = "PODCAST_DOWNLOAD_REFERENCE"

    private val downloadedPodcastsSubject: PublishSubject<List<MediaDescriptionCompat>> =
            PublishSubject.create()

    fun getHourByHourPodcasts(programId: String): Single<List<Podcast>> {
        val program = programRepo.getProgram(programId)
        return podcastRepo.getPodcasts(programId)
                .flatMapIterable { list -> list }
                .map { podcast ->
                    program?.let {
                        podcast.programId = program.id
                        podcast.imageUrl = program.imageUrl()
                    }
                    podcast
                }
                .toList()
    }

    fun getSectionPodcasts(programId: String, sectionId: String): Single<List<Podcast>> {
        val program = programRepo.getProgram(programId)
        return podcastRepo.getPodcasts(programId, sectionId)
                .flatMapIterable { list -> list }
                .map { podcast ->
                    program?.let {
                        podcast.programId = program.id
                        podcast.imageUrl = program.imageUrl()
                    }
                    podcast
                }
                .toList()
    }

    fun isSectionSelected(): Boolean {
        return podcastPref.isSectionSelected()
    }

    fun setSectionSelected(selected: Boolean) {
        podcastPref.setSectionSelected(selected)
    }

    fun download(podcast: MediaDescriptionCompat) {
        val uri = podcast.mediaUri
        val request = DownloadManager.Request(uri)
                .setTitle(podcast.title)
                .setDestinationInExternalFilesDir(
                        context,
                        Environment.DIRECTORY_DOWNLOADS,
                        podcast.mediaId + ProgramDataInteractor.EXTENSION)
                .setVisibleInDownloadsUi(true)

        val reference = downloadManager.enqueue(request)
        podcast.extras?.putLong(PODCAST_DOWNLOAD_REFERENCE, reference)
        downloadRepo.addDownloadingPodcast(podcast)
    }

    fun deleteDownload(podcast: MediaDescriptionCompat) {
        val file = File(podcast.extras?.getString(PODCAST_FILE_PATH))
        if (file.delete()) {
            downloadRepo.deleteDownloadedPodcast(podcast)
        }
    }

    fun getDownloadedPodcasts(): Single<List<MediaDescriptionCompat>> {
        return Single.just(fetchDownloadedPodcasts())
    }

    fun getDownloadedPodcastsUpdates(): PublishSubject<List<MediaDescriptionCompat>> {
        return downloadedPodcastsSubject
    }

    fun refreshDownloadedPodcasts() {
        downloadedPodcastsSubject.onNext(fetchDownloadedPodcasts())
    }

    private fun fetchDownloadedPodcasts(): List<MediaDescriptionCompat> {
        val podcastList = HashSet<MediaDescriptionCompat>()
        val downloading = downloadRepo.getDownloadingPodcasts()
        val downloaded = downloadRepo.getDownloadedPodcasts()
        podcastList.addAll(downloading)
        podcastList.addAll(downloaded)

        Log.d(TAG, "Downloading: " + downloading.size + ", downloaded: " + downloaded.size)
        return ArrayList(podcastList)
    }

    fun deleteDownloading(reference: Long) {
        var podcast: MediaDescriptionCompat? = null
        for (pod in downloadRepo.getDownloadingPodcasts()) {
            if (reference == pod.extras?.getLong(PODCAST_DOWNLOAD_REFERENCE)) {
                podcast = pod
            }
        }
        downloadRepo.deleteDownloadingPodcast(podcast!!)
    }

    fun addDownload(audioId: String) {
        val from = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() +
                File.separator + audioId + ProgramDataInteractor.EXTENSION)
        val to = File(context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS).toString() +
                File.separator + audioId + ProgramDataInteractor.EXTENSION)

        if (from.renameTo(to)) {
            Log.d(TAG, "moving download from " + from.path + " to " + to.path)
            downloadRepo.setPodcastAsDownloaded(audioId, to.path)
        } else {
            from.delete()
            to.delete()
        }
    }
}