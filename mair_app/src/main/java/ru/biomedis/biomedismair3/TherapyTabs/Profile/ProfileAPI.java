package ru.biomedis.biomedismair3.TherapyTabs.Profile;


import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import ru.biomedis.biomedismair3.entity.Profile;

public interface ProfileAPI {
    Profile selectedProfile();
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

    void importProfile(File file);


    void importProfiles(List<Path> file, Consumer<Integer> afterAction);

    void importProfiles(List<Path> files, Map<Path, String> profileNames, Consumer<Integer> afterAction);

    void loadProfileDir();

    void setLastChangeProfile(long profileID);
    void updateProfileWeight(Profile p);
}
