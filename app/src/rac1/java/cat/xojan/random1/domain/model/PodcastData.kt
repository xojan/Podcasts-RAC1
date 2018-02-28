package cat.xojan.random1.domain.model

import com.squareup.moshi.Json
import java.util.*

class PodcastData(
        @Json(name = "result") var podcasts: List<PodcastRac1> = listOf()
): PodcastInterface {
    override fun toPodcasts(programId: String): List<Podcast> {
        return podcasts.map { p ->
            Podcast(
                    p.audio.id,
                    p.remoteUrl,
                    null,
                    p.date,
                    p.durationSeconds,
                    programId,
                    null,
                    null,
                    PodcastState.LOADED,
                    p.title
            )
        }
    }
}

class PodcastRac1(
        @Json(name = "audio") var audio: AudioRac1,
        @Json(name = "path") var remoteUrl: String,
        @Json(name = "dateTime") var date: Date?,
        @Json(name = "durationSeconds") var durationSeconds: Long,
        @Json(name = "appMobileTitle") var title: String
)

class AudioRac1(
        @Json(name = "id") var id: String
)