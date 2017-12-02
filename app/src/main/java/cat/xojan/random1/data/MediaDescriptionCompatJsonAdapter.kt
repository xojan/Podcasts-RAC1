package cat.xojan.random1.data

import android.support.v4.media.MediaDescriptionCompat
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson



class MediaDescriptionCompatJsonAdapter {

    @ToJson
    fun toJson(podcasts: MutableSet<MediaDescriptionCompat>): Collection<MediaDescriptionCompat> {
        return podcasts
    }

    @FromJson
    fun fromJson(collection: Collection<MediaDescriptionCompat>): MutableSet<MediaDescriptionCompat> {
        val podcasts = mutableSetOf<MediaDescriptionCompat>()
        podcasts.addAll(collection)
        return podcasts
    }
}