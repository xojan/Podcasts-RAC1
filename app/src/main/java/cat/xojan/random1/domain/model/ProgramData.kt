package cat.xojan.random1.domain.model

import com.squareup.moshi.Json
import org.jetbrains.annotations.TestOnly


class ProgramData {

    @Json(name = "result")
    @set:TestOnly
    var programs: List<Program> = listOf()
}