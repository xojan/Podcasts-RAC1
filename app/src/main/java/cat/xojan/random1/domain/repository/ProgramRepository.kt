package cat.xojan.random1.domain.repository

import cat.xojan.random1.domain.entities.Program
import io.reactivex.Single

interface ProgramRepository {
    fun getPrograms(): Single<List<Program>>

    fun getProgram(programId: String): Program?
}