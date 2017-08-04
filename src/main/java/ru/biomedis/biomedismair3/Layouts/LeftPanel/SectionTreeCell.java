package ru.biomedis.biomedismair3.Layouts.LeftPanel;

import javafx.scene.control.TreeCell;
import ru.biomedis.biomedismair3.entity.INamed;

class SectionTreeCell extends TreeCell<INamed> {
    @Override
    protected void updateItem(INamed item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            if (getTreeItem().getValue() == null) {
                this.setText(null);
                this.setGraphic(null);
                return;
            }
            this.setText(getTreeItem().getValue().getNameString());//имя из названия INamed
            this.setGraphic(getTreeItem().getGraphic());//иконку мы устанавливали для элементов в NamedTreeItem согласно типу содержимого
        }
    }
}
