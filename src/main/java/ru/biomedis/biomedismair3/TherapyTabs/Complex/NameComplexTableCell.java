package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import ru.biomedis.biomedismair3.entity.TherapyComplex;

public class NameComplexTableCell extends TableCell<TherapyComplex,String>{
    private VBox vbox = new VBox(3);
    private Label topText = new Label();
    private Label bottomText = new Label();
    private Font boldFont = Font.font(null, FontWeight.BOLD, 12);
    private Font italicFont = Font.font(null, FontPosture.ITALIC, 12);

    private TextField textField;

    public NameComplexTableCell() {
        vbox = new VBox();
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setAlignment(Pos.CENTER_LEFT);

        bottomText.setFont(italicFont);
        bottomText.setTextFill(Color.DARKSLATEGRAY);

        vbox.getChildren().addAll(topText,bottomText);
        setGraphic(vbox);

    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(null);

        if (!empty) {
            TherapyComplex thisComplex = (TherapyComplex) getTableRow().getItem();
            if (thisComplex == null) return;

                if(!thisComplex.getOname().isEmpty())  {
                    bottomText.setText(thisComplex.getOname());
                    if(!vbox.getChildren().contains(bottomText))vbox.getChildren().add(bottomText);

                }
                else  vbox.getChildren().remove(bottomText);

                topText.setText(thisComplex.getName());
        }
    }


    @Override
    public void startEdit() {
        if (! isEditable() || ! getTableView().isEditable() || ! getTableColumn().isEditable()) return;
        super.startEdit();
        System.out.println("startEdit");
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        System.out.println("commitEdit");
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        System.out.println("cancelEdit");
    }
/*
    private <T> TextField createTextField(final Cell<T> cell, final StringConverter<T> converter) {
        final TextField textField = new TextField(getItemText(cell, converter));

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction(event -> {
            if (converter == null) {
                throw new IllegalStateException(
                        "Attempting to convert text input into Object, but provided "
                                + "StringConverter is null. Be sure to set a StringConverter "
                                + "in your cell factory.");
            }
            cell.commitEdit(converter.fromString(textField.getText()));
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return textField;
    }
    */
}
