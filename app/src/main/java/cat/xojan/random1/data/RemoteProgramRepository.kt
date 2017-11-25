package cat.xojan.random1.data

import cat.xojan.random1.domain.entities.Program
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Single
import java.io.IOException

class RemoteProgramRepository(private val service: Rac1ApiService): ProgramRepository {

    var programs: LinkedHashMap<String,  Program> = linkedMapOf()

    override fun getPrograms(): Single<List<Program>> {
        return Single.create { subscriber ->
            try {
                if (programs.isEmpty()) {
                    for (item in service.getProgramData().execute().body()!!.programs) {
                        programs.put(item.id, item)
                    }
                }
                subscriber.onSuccess(programs.values.toList())
            } catch (e: IOException) {
                subscriber.onError(e)
            }
        }
    }

    override fun getProgram(programId: String): Program? {
        return programs[programId]
    }
}