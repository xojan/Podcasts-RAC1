package cat.xojan.random1.domain.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.*

class PodcastJsonAdapter {

    private val df = SimpleDateFormat("MMM dd, yyyy H:mm:ss aaa", Locale.ENGLISH)

    @ToJson
    fun toJson(podcast: Podcast): PodcastJson {
        return PodcastJson(
                podcast.title,
                podcast.id,
                df.format(podcast.dateTime),
                podcast.durationSeconds,
                podcast.filePath,
                podcast.smallImageUrl,
                podcast.programId,
                podcast.state,
                podcast.remoteUrl
        )
    }

    @FromJson
    fun fromJson(itemJson: PodcastJson): Podcast {
        return Podcast(
                itemJson.id,
                itemJson.path,
                itemJson.mFilePath,
                df.parse(itemJson.dateTime),
                itemJson.durationSeconds,
                itemJson.mProgramId,
                itemJson.mImageUrl,
                itemJson.mImageUrl,
                itemJson.mState,
                itemJson.appMobileTitle
        )
    }
}