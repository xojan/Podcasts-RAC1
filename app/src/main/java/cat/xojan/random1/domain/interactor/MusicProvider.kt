package cat.xojan.random1.domain.interactor

import android.content.res.Resources
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.other.MediaIDHelper
import cat.xojan.random1.other.MediaIDHelper.MEDIA_ID_PODCAST_BY_PROGRAM
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MusicProvider @Inject constructor(val programInteractor: ProgramDataInteractor) {

    private val TAG = MusicProvider::class.simpleName

    fun isInitialized(): Boolean = false

    fun retrieveMedia(
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
            parentId: String,
            resources: Resources) {
        if (parentId == MediaIDHelper.MEDIA_ID_ROOT) {
            programInteractor.loadPrograms()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { n -> handleSuccess(n, resources, result) },
                            { e -> handleError(e) },
                            { handleComplete() }
                    )
        } else {
            /*var subscription = programInteractor.getCurrentPodcasts()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ this.updateView(it, result) },
                            { this.handleError(it) },
                            { this.handleComplete()})*/
        }

        //result.sendResult(getMedia(parentMediaId, getResources()))
    }

    private fun handleSuccess(
            t: List<Program>,
            resources: Resources,
            result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "Retrieve programs success")
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        mediaItems.add(createBrowsableMediaItemForRoot(resources))
        result.sendResult(mediaItems)
    }

    private fun handleComplete() {

    }

    private fun handleError(it: Throwable?) {
        Log.e(TAG, it.toString())
    }

    /*private fun updateView(podcasts: List<Podcast>, result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        for (p in podcasts) {
            mediaItems.add(createBrowsableMediaItemForRoot(p))
        }
        result.sendResult(mediaItems)
    }*/

    private fun createBrowsableMediaItemForRoot(resources: Resources): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_PODCAST_BY_PROGRAM)
                .setTitle("Browse genres")
                .setSubtitle("Browse genre subtitle")
                .setIconUri(Uri.parse("android.resource://" + "com.example.android.uamp/drawable/ic_by_genre"))
                .build()
        return MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }
}