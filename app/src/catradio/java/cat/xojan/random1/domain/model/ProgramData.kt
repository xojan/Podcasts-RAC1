package cat.xojan.random1.domain.model

import com.squareup.moshi.Json

class ProgramData(
        @Json(name = "resposta") val resposta: Resposta
): ProgramInterface {
    override fun toPrograms(): List<Program> {
        return resposta.items.items
            .map { p ->
                    Program(
                            p.id,
                            p.titol,
                            getSmallImageUrl(p.imatges?.imatge),
                            getBigImageUrl(p.imatges?.imatge),
                            toSections(p.seccions,p.imatges?.imatge?.get(0)?.text, p.id)
                    )
            }
    }

    private fun getSmallImageUrl(images: List<ImageCatRadio>?): String? {
        return images?.firstOrNull {
            it -> it.size == "200x200"
        }?.text
    }

    private fun getBigImageUrl(images: List<ImageCatRadio>?): String? {
        return images?.firstOrNull {
            it -> it.size == "670x378"
        }?.text
    }

    private fun toSections(seccions: SectionsCatRadio?, imageUrl: String?, programId: String)
            : List<Section>? {
        // TODO filter
        return seccions?.seccions?.map { s ->
            Section(
                    s.id,
                    s.desc,
                    imageUrl,
                    programId
            )
        }
    }
}

class Resposta(
        @Json(name ="items") val items: Items
)

class Items(
        @Json(name="item") val items: List<Item>,
        @Json(name = "num") val numItems: Int
)

class Item(
        @Json(name = "url_podcast") val urlPodcast: String? = null,
        @Json(name = "id") val id: String,
        @Json(name = "titol") val titol: String,
        @Json(name = "imatges") val imatges: ImagesCatRadio? = null,
        @Json(name = "seccions") val seccions: SectionsCatRadio? = null

)

class ImagesCatRadio(
        @Json(name = "imatge") val imatge: List<ImageCatRadio>? = listOf()
)

class ImageCatRadio(
        @Json(name = "text") val text: String? = null,
        @Json(name = "mida") val size: String? = null
)

class SectionsCatRadio(
        @Json(name = "seccio") val seccions: List<SectionCatRadio>? = listOf()
)

class SectionCatRadio(
        @Json(name = "id") val id: String,
        @Json(name = "desc") val desc: String?,
        @Json(name = "nom") val nom: String?, //"SECCIO"
        @Json(name = "tipologia") val tipologia: String?, //WCR_SECCIO
        @Json(name = "url_podcast") val urlPodcast: String?,
        @Json(name = "poad_pub") val poadPub: String? //SI, NO
)