package cat.xojan.random1.domain.model

import com.squareup.moshi.Json

class ProgramData(
        @Json(name = "resposta") val resposta: Resposta
): ProgramInterface {
    override fun toPrograms(): List<Program> {
        return resposta.items.lletres
            .flatMap { it ->
                it.items
                    .filter { p -> p.domini == "PUCR" }
                    .map { p ->
                        Program(
                                p.id,
                                p.titol,
                                p.imatges?.imatge?.get(0)?.text, //TODO
                                p.imatges?.imatge?.get(0)?.text, //TODO
                                toSections(p.seccions,p.imatges?.imatge?.get(0)?.text, p.id)
                        )
                    }
            }
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
        @Json(name = "domini") val domini: String, // PUCR, WCR
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