package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import ru.biomedis.biomedismair3.entity.TherapyComplex;

public interface ComplexAPI {
    void hideSpinners();

    void removeComplex();

    void printComplex();

    void updateComplexTime(TherapyComplex c, boolean reloadPrograms);
}
