package ru.biomedis.biomedismair3.TherapyTabs.Profile;


import ru.biomedis.biomedismair3.entity.Profile;

public interface ProfileAPI {
    void disableGenerateBtn();
    void enableGenerateBtn();
    void updateProfileTime(Profile p);
    void checkUpploadBtn();
    void removeProfileFiles();
    void removeProfile();
    void printProfile();
    void pasteProfile();
    void pasteProfile_after();
    void cutProfileToBuffer();
    void exportProfile();
    void importProfile();
    void loadProfileDir();

    void setLastChangeProfile(long profileID);
}
