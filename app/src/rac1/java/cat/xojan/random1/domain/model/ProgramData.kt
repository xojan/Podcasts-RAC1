package cat.xojan.random1.domain.model

import com.squareup.moshi.Json


class ProgramData(
        @Json(name = "result") val programs: List<ProgramRac1> = listOf()
): ProgramInterface {
    override fun toPrograms(): List<Program> {
        return programs
                .filter { p -> p.active }
                .map { p ->
            Program(
                    p.id,
                    p.title,
                    p.images?.smallImageUrl,
                    p.images?.bigImageUrl,
                    toSections(p.sectionsRac1, p.id, p.images?.smallImageUrl)
            )
        }
    }

    private fun toSections(sectionsRac1: List<SectionRac1>, programId: String, imageUrl: String?)
            : List<Section> {
        return sectionsRac1
                .filter { s -> s.active }
                .filter { s -> s.type == "SECTION"}
                .map { s ->
                    Section(
                            s.id,
                            s.title,
                            imageUrl,
                            programId
                    )
                }
    }
}

class ProgramRac1(
        @Json(name = "id") val id: String,
        @Json(name = "title") var title: String? = null,
        @Json(name = "sections") var sectionsRac1: List<SectionRac1> = listOf(),
        @Json(name = "images") var images: ImagesRac1? = null,
        @Json(name = "active") val active: Boolean
)

class ImagesRac1(
        @Json(name = "person-small") val smallImageUrl: String? = null,
        @Json(name = "app") val bigImageUrl: String? = null
)

class SectionRac1(
        @Json(name = "id") val id: String,
        @Json(name = "title") val title: String,
        @Json(name = "active") val active: Boolean,
        @Json(name = "type") var type: String
)

enum class SectionTypeRac1 {
    HOUR,
    SECTION,
    GENERIC
}