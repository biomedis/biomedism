package ru.biomedis.biomedismair3.Layouts.LeftPanel;

import java.nio.file.Path;
import java.util.List;
import javafx.collections.ObservableList;
import ru.biomedis.biomedismair3.entity.INamed;
import ru.biomedis.biomedismair3.entity.Section;

import java.io.File;

public interface LeftPanelAPI {
    void setTreeActionListener(TreeActionListener treeActionListener);

    Section selectedBase();

    int selectedBaseIndex();

    ObservableList<Section> getBaseAllItems();

    void selectBase(int index);

    void selectBase(Section section);

    Section selectedRootSection();

    int selectedRootSectionIndex();

    ObservableList<Section> getRootSectionAllItems();

    void selectRootSection(int index);

    void selectRootSection(Section section);

    NamedTreeItem selectedSectionTree();

    INamed selectedSectionTreeItem();

    void addTreeItemToTreeRoot(NamedTreeItem item);

    void addTreeItemToSelected(NamedTreeItem item);

    boolean loadComplexToBase(File dir, NamedTreeItem treeItem, boolean createComplex);

    /**
     * Если выбран раздел в пользовательской базе
     * @return
     */
    boolean isInUserBaseSectionSelected();

    /**
     * Если выбран комплекс в пользовательской базе
     * @return
     */
    boolean isInUserBaseComplexSelected();

    void exportUserBaseToDisk();

    void exportUserBaseToServer();

    void importUserBase();

    void importUserBase(Path file, String nameSection);

    void  importComplexToBaseFromDir();


}
