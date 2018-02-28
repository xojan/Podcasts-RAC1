package cat.xojan.random1.data

import cat.xojan.random1.domain.model.Program
import cat.xojan.random1.domain.model.ProgramData
import cat.xojan.random1.domain.model.Section
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Observable
import io.reactivex.Single



class RemoteProgramRepository(private val service: ApiService): ProgramRepository {

    private var programData: Single<ProgramData>? = null

    override fun getPrograms(refresh: Boolean): Single<List<Program>> {
        if (programData == null || refresh) {
            programData = service.getProgramData(
                    true,
                    false,
                    "pucr-alacarta-programa",
                    "frontal",
                    "1.0"
            ).cache()
        }
        return programData!!.map { pd: ProgramData -> pd.toPrograms() }
    }

    override fun getProgram(programId: String): Single<Program> =
            getPrograms(false).flatMap {
                programs -> Observable.fromIterable(programs)
                    .filter { p: Program ->  p.id == programId }
                    .firstOrError()
            }

    override fun hasSections(programId: String): Single<Boolean> =
            getProgram(programId).map { p -> p.sections!!.size > 1 }

    override fun getSections(programId: String): Single<List<Section>> =
            getProgram(programId)
                    .map { p -> p.sections }
}