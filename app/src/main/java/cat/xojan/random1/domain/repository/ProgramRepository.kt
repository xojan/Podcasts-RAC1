package cat.xojan.random1.domain.repository

import cat.xojan.random1.domain.model.Program
import cat.xojan.random1.domain.model.Section
import io.reactivex.Single

interface ProgramRepository {
    fun getPrograms(): Single<List<Program>>

    fun getProgram(programId: String): Program?

    fun hasSections(programId: String): Boolean

    fun getSections(programId: String): Single<List<Section>>
}