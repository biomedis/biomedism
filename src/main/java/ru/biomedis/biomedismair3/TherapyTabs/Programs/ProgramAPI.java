package ru.biomedis.biomedismair3.TherapyTabs.Programs;

public interface ProgramAPI {
    void removePrograms();
    void pasteTherapyPrograms();
    void pasteTherapyPrograms_after();
    void copySelectedTherapyProgramsToBuffer();
    void cutSelectedTherapyProgramsToBuffer();

}
