package cat.xojan.random1.domain.interactor

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import cat.xojan.random1.domain.entities.Podcast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MusicProvider @Inject constructor(val programInteractor: ProgramDataInteractor) {

    private val MEDIA_ID_ROOT = "MEDIA_ID_ROOT"

    fun isInitialized(): Boolean = false

    fun retrieveMedia(result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>,
                   parentId: String) {
        var subscription = programInteractor.getCurrentPodcasts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.updateView(it, result) },
                        { this.handleError(it) },
                        { this.handleComplete()})

        //result.sendResult(getMedia(parentMediaId, getResources()))
    }

    private fun handleComplete() {

    }

    private fun handleError(it: Throwable?) {
        Log.d("joan", it.toString())
    }

    private fun updateView(podcasts: List<Podcast>, result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        for (p in podcasts) {
            mediaItems.add(createBrowsableMediaItemForRoot(p))
        }
        result.sendResult(mediaItems)
    }

    private fun createBrowsableMediaItemForRoot(p: Podcast): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_ROOT)
                .setTitle(p.title)
                .setSubtitle(p.title)
                .build()
        return MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }
}