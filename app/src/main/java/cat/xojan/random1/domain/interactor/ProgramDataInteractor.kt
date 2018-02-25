package cat.xojan.random1.domain.interactor
import cat.xojan.random1.domain.model.Program
import cat.xojan.random1.domain.model.Section
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ProgramDataInteractor @Inject constructor(private val programRepo: ProgramRepository) {

    fun loadPrograms(refresh: Boolean): Single<List<Program>> {
        return programRepo.getPrograms(refresh)
                .flatMap {
                    programs -> Observable.just(programs)
                        .flatMapIterable { p -> p }
                        .toList()
                }
    }

    fun hasSections(programId: String): Single<Boolean> {
        return programRepo.hasSections(programId)
    }

    fun loadSections(programId: String): Single<List<Section>> {
        return programRepo.getSections(programId)
    }
}