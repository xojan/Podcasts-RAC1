package cat.xojan.random1.feature.mediaplayback

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import cat.xojan.random1.domain.interactor.ProgramDataInteractor
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_BIG_IMAGE_URL
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_DATE
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_DURATION
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_PROGRAM_ID
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_STATE
import cat.xojan.random1.domain.model.Program
import cat.xojan.random1.domain.model.Section
import cat.xojan.random1.feature.home.DownloadsFragment
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
            parentId.contains("__PROGRAMS__") -> {
                val splitString: List<String> = parentId.split("/")
                val refresh = if (!splitString.isEmpty() && splitString.size > 1) {
                    splitString[1].toBoolean()
                } else {
                    false
                }
                compositeDisposable.add(programInteractor.loadPrograms(refresh)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { n -> handleNextPrograms(n, result) },
                                { e -> handleError(e, result) }
                        )
                )
            }
            parentId == DownloadsFragment.MEDIA_ID_DOWNLOADS ->
                compositeDisposable.add(podcastInteractor.getDownloadedPodcasts()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {p -> handleNextDownloadedPodcasts(p, result)},
                                {e -> handleError(e, result)}
                        ))
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
                val (programId, sectionId, refresh) = parentId.split("/")
                compositeDisposable.add(podcastInteractor.getSectionPodcasts(programId,
                        sectionId, refresh.toBoolean())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { p -> handleNextPodcasts(p, result) },
                                { e -> handleError(e, result) }
                        )
                )
            }
            else ->  {
                val (programId, refresh) = parentId.split(":")
                compositeDisposable.add(podcastInteractor.getHourByHourPodcasts(programId,
                        refresh.toBoolean())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { p -> handleNextPodcasts(p, result)},
                                { e -> handleError(e, result)}
                        )
                )
            }
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

        queueManager.potentialPlaylist = mediaItemsToQueueItems(mediaItems)
    }

    private fun handleNextDownloadedPodcasts(
            podcasts: List<MediaBrowserCompat.MediaItem>,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        queueManager.updateDownloadsPlaylist(mediaItemsToQueueItems(podcasts
                as ArrayList<MediaBrowserCompat.MediaItem>))
        result.sendResult(podcasts as MutableList<MediaBrowserCompat.MediaItem>?)
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
        val descriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(program.id)
                .setTitle(program.title)

        program.smallImageUrl?.let {
            descriptionBuilder.setIconUri(Uri.parse(program.smallImageUrl))
        }
        return MediaBrowserCompat.MediaItem(descriptionBuilder.build(),
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
        extras.putString(PODCAST_PROGRAM_ID, podcast.programId)
        extras.putString(PODCAST_BIG_IMAGE_URL, podcast.bigImageUrl)
        extras.putLong(PODCAST_DURATION, podcast.durationSeconds)
        extras.putSerializable(PODCAST_DATE, podcast.dateTime)

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