package cat.xojan.random1.domain.model

import com.squareup.moshi.Json

class ProgramData(
        @Json(name = "resposta") val resposta: Resposta

) {
    val programs: List<Program> = listOf()
}

class Resposta(
        @Json(name ="items") val items: Items
)

class Items(
        @Json(name = "lletra") val lletres: List<Lletra>
)

class Lletra(
        @Json(name="valor") val valor: Char,
        @Json(name="item") val items: List<Item>
)

class Item(
        @Json(name = "lletra") val lletra: Char,
        @Json(name = "url_podcast") val urlPodcast: String? = null,
        @Json(name = "id") val id: String,
        @Json(name = "titol") val titol: String,
        @Json(name = "data_publicacio") val dataPublicacio: String,
        @Json(name = "domini") val domini: String
        //@Json(name = "seccions") val seccions: Seccio,
        //@Json(name = "imatges") val imatges: Imatge,
)