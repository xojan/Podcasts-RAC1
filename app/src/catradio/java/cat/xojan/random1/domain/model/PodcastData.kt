package cat.xojan.random1.domain.model

import com.squareup.moshi.Json

class PodcastData(
        @Json(name = "resposta") private val resposta: RespostaPodcastsCatRadio
): PodcastInterface {
    override fun toPodcasts(programId: String): List<Podcast> {
        return resposta.items.items.map { p ->
            Podcast(
                    p.id,
                    getRemoteUrl(p.audios.audio),
                    null,
                    null, //TODO p.date Date
                    getDurationSeconds(p.audios.audio),
                    programId,
                    null,
                    null,
                    PodcastState.LOADED,
                    p.title
            )
        }
    }

    private fun getDurationSeconds(audio: List<AudioCatRadio>): Long {
        val item = audio.first()
        return item.length / item.durationFrame
    }

    private fun getRemoteUrl(audios: List<AudioCatRadio>): String {
        return "http://audios.catradio.cat/multimedia/" + audios.firstOrNull()?.urlPostfix
    }
}

class RespostaPodcastsCatRadio(
        @Json(name ="items") val items: PodcastsCatRadio
)

class PodcastsCatRadio(
        @Json(name="item") val items: List<PodcastCatRadio>,
        @Json(name = "num") val numItems: Int
)

class PodcastCatRadio(
        @Json(name = "id") var id: String,
        @Json(name = "titol") var title: String,
        @Json(name = "audios") var audios: AudiosCatRadio,
        @Json(name = "data_publicacio") var date: String
)

class AudiosCatRadio(
        @Json(name = "audio") var audio: List<AudioCatRadio>
)

class AudioCatRadio(
        @Json(name = "text") var urlPostfix: String,
        @Json(name = "longitud") var length: Long,
        @Json(name = "durada_frame") var durationFrame: Long
)