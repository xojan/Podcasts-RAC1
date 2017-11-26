package cat.xojan.random1.domain.repository

import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.domain.entities.Section
import io.reactivex.Single

interface ProgramRepository {
    fun getPrograms(): Single<List<Program>>

    fun getProgram(programId: String): Program?

    fun hasSections(programId: String): Boolean

    fun getSections(programId: String): List<Section>?
}