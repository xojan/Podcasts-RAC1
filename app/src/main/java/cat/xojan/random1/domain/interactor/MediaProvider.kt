package cat.xojan.random1.domain.interactor

import android.content.res.Resources
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import cat.xojan.random1.domain.entities.Podcast
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.other.MediaIDHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MediaProvider @Inject constructor(val programInteractor: ProgramDataInteractor) {

    private val TAG = MediaProvider::class.simpleName

    fun retrieveMedia(
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
            parentId: String,
            resources: Resources) {
        if (parentId == MediaIDHelper.MEDIA_ID_ROOT) {
            programInteractor.loadPrograms()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { n -> handleNextPrograms(n, result) },
                            { e -> handleError(e) },
                            { handleComplete() }
                    )
        } else {
            programInteractor.getHourByHourPodcasts(parentId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { n -> handleNextPodcasts(n, result) },
                            { e -> handleError(e) },
                            { handleComplete() }
                    )
        }
    }

    private fun handleNextPrograms(
            programs: List<Program>,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "Retrieve programs success")
        val mediaItems = programs.mapTo(ArrayList()) { createBrowsableMediaItemForProgram(it) }
        result.sendResult(mediaItems)
    }

    private fun handleNextPodcasts(
            podcasts: List<Podcast>,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "Retrieve programs success")
        val mediaItems = podcasts.mapTo(ArrayList()) { createBrowsableMediaItemForPodcast(it) }
        result.sendResult(mediaItems)
    }

    private fun handleComplete() {}

    private fun handleError(it: Throwable?) {
        Log.e(TAG, it.toString())
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