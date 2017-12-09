package cat.xojan.random1.data

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_DOWNLOAD_REFERENCE
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_FILE_PATH
import cat.xojan.random1.domain.model.Podcast.Companion.PODCAST_STATE
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class MediaDescriptionCompatJsonAdapter {

    @ToJson
    fun toJson(item: MediaDescriptionCompat): MediaDescriptionCompatJson {
        return MediaDescriptionCompatJson(
                item.mediaId,
                item.title.toString(),
                item.mediaUri.toString(),
                item.iconUri.toString(),
                item.extras?.getSerializable(PODCAST_STATE) as Podcast.State,
                item.extras?.getLong(PODCAST_DOWNLOAD_REFERENCE),
                item.extras?.getString(PODCAST_FILE_PATH))
    }

    @FromJson
    fun fromJson(itemJson: MediaDescriptionCompatJson): MediaDescriptionCompat {
        val extras = Bundle()
        extras.putSerializable(PODCAST_STATE, itemJson.state)
        extras.putLong(PODCAST_DOWNLOAD_REFERENCE, itemJson.downloadReference!!)
        extras.putString(PODCAST_FILE_PATH, itemJson.mediaFilePath)

        return MediaDescriptionCompat.Builder()
                .setMediaId(itemJson.id)
                .setTitle(itemJson.title)
                .setMediaUri(Uri.parse(itemJson.mediaUrl))
                .setIconUri(Uri.parse(itemJson.iconUrl))
                .setExtras(extras)
                .build()
    }
}