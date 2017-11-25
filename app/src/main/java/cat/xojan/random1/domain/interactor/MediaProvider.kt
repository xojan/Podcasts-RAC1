package cat.xojan.random1.domain.interactor

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.ui.home.ProgramFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MediaProvider @Inject constructor(
        private val programInteractor: ProgramDataInteractor,
        private val podcastInteractor: PodcastDataInteractor) {

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
        if (parentId == ProgramFragment.MEDIA_ID_ROOT) {
            compositeDisposable.add(programInteractor.loadPrograms()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { n -> handleNextPrograms(n, result) },
                            { e -> handleError(e, result) }
                    )
            )
        } else {
            compositeDisposable.add(
                    podcastInteractor.getHourByHourPodcasts(parentId)
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
        Log.d(tag, "Retrieve programs success")
        val mediaItems = podcasts.mapTo(ArrayList()) { createBrowsableMediaItemForPodcast(it) }
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

    private fun createBrowsableMediaItemForPodcast(podcast: Podcast): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(podcast.audioId)
                .setTitle(podcast.title)
                .setMediaUri(Uri.parse(podcast.path))
                //.setIconUri(Uri.parse(podcast.imageUrl))
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
}