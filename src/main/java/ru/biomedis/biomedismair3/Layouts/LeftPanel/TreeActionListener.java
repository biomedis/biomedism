package ru.biomedis.biomedismair3.Layouts.LeftPanel;

import javafx.scene.control.TreeItem;
import ru.biomedis.biomedismair3.entity.INamed;

public interface TreeActionListener {
    void programItemDoubleClicked(TreeItem<INamed> selectedItem);
    void complexItemDoubleClicked(TreeItem<INamed> selectedItem);
}
