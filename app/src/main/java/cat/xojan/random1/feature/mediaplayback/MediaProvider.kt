package cat.xojan.random1.feature.mediaplayback

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_STATE
import cat.xojan.random1.domain.model.Program
import cat.xojan.random1.domain.model.Section
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.feature.home.ProgramFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MediaProvider @Inject constructor(
        private val programInteractor: ProgramDataInteractor,
        private val podcastInteractor: PodcastDataInteractor,
        private val queueManager: QueueManager) {

    companion object {
        val ERROR = "ERROR"
    }
    private val tag = MediaProvider::class.simpleName
    private val compositeDisposable = CompositeDisposable()

    fun clear() {
        compositeDisposable.clear()
    }

    fun retrieveMedia(
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
            parentId: String) {
        when {
            parentId == ProgramFragment.MEDIA_ID_ROOT ->
                compositeDisposable.add(programInteractor.loadPrograms()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { n -> handleNextPrograms(n, result) },
                            { e -> handleError(e, result) }
                    )
            )
            parentId.contains("/SECTIONS") -> {
                val programId = parentId.split("/")[0]
                compositeDisposable.add(programInteractor.loadSections(programId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { s -> handleNextSections(s, result) },
                                { e -> handleError(e, result) }
                        )
                )
            }
            parentId.contains("/") -> {
                val programId = parentId.split("/")[0]
                val sectionId = parentId.split("/")[1]
                compositeDisposable.add(podcastInteractor.getSectionPodcasts(programId, sectionId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { p -> handleNextPodcasts(p, result) },
                                { e -> handleError(e, result) }
                        )
                )
            }
            else -> compositeDisposable.add(podcastInteractor.getHourByHourPodcasts(parentId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { p -> handleNextPodcasts(p, result)},
                            { e -> handleError(e, result)}
                    )
            )
        }
    }

    private fun handleNextPrograms(
            programs: List<Program>,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(tag, "Retrieve programs success")
        val mediaItems = programs.mapTo(ArrayList()) { createBrowsableMediaItemForProgram(it) }
        result.sendResult(mediaItems)
    }

    private fun handleNextPodcasts(
            podcasts: List<Podcast>,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(tag, "Retrieve podcasts success")
        val mediaItems = podcasts.mapTo(ArrayList()) { createBrowsableMediaItemForPodcast(it) }
        result.sendResult(mediaItems)

        queueManager.items = mediaItemsToQueueItems(mediaItems)
    }

    private fun handleNextSections(
        sections: List<Section>?,
        result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = sections?.mapTo(ArrayList()) { createBrowsableMediaItemForSection(it) }
        result.sendResult(mediaItems)
    }

    private fun handleError(
            it: Throwable?,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.e(tag, it.toString())
        result.sendResult(arrayListOf(createErrorBrowsableMediaItem(it)))
    }

    private fun createErrorBrowsableMediaItem(it: Throwable?): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(ERROR)
                .setTitle(it?.message)
                .setDescription(it?.printStackTrace().toString())
                .build()
        return MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun createBrowsableMediaItemForProgram(program: Program): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(program.id)
                .setTitle(program.title)
                .setIconUri(Uri.parse(program.imageUrl()))
                .build()
        return MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun createBrowsableMediaItemForSection(section: Section): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(section.programId + "/" + section.id)
                .setTitle(section.title)
                .setIconUri(Uri.parse(section.imageUrl))
                .build()
        return MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun createBrowsableMediaItemForPodcast(podcast: Podcast): MediaBrowserCompat.MediaItem {
        val extras = Bundle()
        extras.putSerializable(PODCAST_STATE, podcast.state)

        val description = MediaDescriptionCompat.Builder()
                .setMediaId(podcast.audioId)
                .setTitle(podcast.title)
                .setMediaUri(Uri.parse(podcast.path))
                .setIconUri(Uri.parse(podcast.imageUrl))
                .setExtras(extras)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun mediaItemsToQueueItems(mediaItems: ArrayList<MediaBrowserCompat.MediaItem>)
            : List<MediaSessionCompat.QueueItem> {

        return mediaItems.mapTo(ArrayList()) {
            it -> MediaSessionCompat.QueueItem(it.description, mediaItems.indexOf(it).toLong())
        }
    }
}