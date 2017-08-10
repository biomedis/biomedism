package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;

import java.util.List;
import java.util.function.Consumer;

public interface ComplexAPI {
    void hideSpinners();

    void removeComplex();

    void printComplex();

    void updateComplexTime(TherapyComplex c, boolean reloadPrograms);

    void pasteTherapyComplexes();

    void copySelectedTherapyComplexesToBuffer();

    void cutSelectedTherapyComplexesToBuffer();

    void exportTherapyComplexes(List<TherapyComplex> complexes);

    void importTherapyComplex(Profile profile, Consumer<Integer> afterAction);

    void importComplexFromDir();

}
