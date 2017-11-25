package cat.xojan.random1.viewmodel;

import java.util.List;

import cat.xojan.random1.domain.entities.Program;
import cat.xojan.random1.domain.entities.Section;
import cat.xojan.random1.domain.entities.SectionType;
import cat.xojan.random1.domain.interactor.ProgramDataInteractor;
import io.reactivex.Single;

public class SectionsViewModel {

    private final ProgramDataInteractor mProgramDataInteractor;

    public SectionsViewModel(ProgramDataInteractor programDataInteractor) {
        mProgramDataInteractor = programDataInteractor;
    }

    public Single<List<Section>> loadSections(Program program) {
        return mProgramDataInteractor.loadSections(program)
                .flatMapIterable(list -> list)
                .filter(Section::getActive)
                .filter(section -> section.getType() == SectionType.SECTION)
                .map(section -> {
                    section.setImageUrl(program.imageUrl());
                    return section;
                })
                .toList();
    }

    public void selectedSection(boolean selected) {
       // mProgramDataInteractor.setSectionSelected(selected);
    }
}
