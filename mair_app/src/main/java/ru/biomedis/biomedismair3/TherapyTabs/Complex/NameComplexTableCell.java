package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import ru.biomedis.biomedismair3.entity.TherapyComplex;

public class NameComplexTableCell extends TableCell<TherapyComplex,String>{
    private final StackPane stack = new StackPane();
    private final VBox vbox = new VBox(3);
    private final Label topText = new Label();
    private final Label bottomText = new Label();
    private static final Font italicFont = Font.font(null, FontPosture.ITALIC, 12);

    private  TextField textField;

    public NameComplexTableCell() {
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setAlignment(Pos.CENTER_LEFT);
        bottomText.setFont(italicFont);
        bottomText.setTextFill(Color.DARKSLATEGRAY);
        vbox.getChildren().addAll(topText,bottomText);

        stack.getChildren().addAll(vbox);
        setGraphic(stack);

    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (!empty) {
            TherapyComplex thisComplex = (TherapyComplex) getTableRow().getItem();
            if (thisComplex == null) return;

                if(!thisComplex.getOname().isEmpty())  {
                    bottomText.setText(thisComplex.getOname());
                    if(!vbox.getChildren().contains(bottomText))vbox.getChildren().add(bottomText);

                }
                else  vbox.getChildren().remove(bottomText);

                topText.setText(thisComplex.getName());
            setGraphic(stack);
        }
    }


    @Override
    public void startEdit() {
        if (! isEditable() || ! getTableView().isEditable() || ! getTableColumn().isEditable()) return;
        super.startEdit();
        createEditor();

    }



    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        clearEditor();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        clearEditor();
    }

    private  TextField createTextField(final NameComplexTableCell cell) {
        final TextField textField = new TextField(topText.getText());
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setMaxHeight(Double.MAX_VALUE);

        textField.setOnAction(event -> {
            cell.commitEdit(textField.getText());
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

    private void createEditor() {
        clearEditor();
        textField = createTextField(this);
        stack.getChildren().add(textField);
        stack.getChildren().get(0).setVisible(false);
        stack.getChildren().get(1).setVisible(true);


    }

    private void clearEditor(){
        if(stack.getChildren().size()==2){
            stack.getChildren().remove(1);
        }
        stack.getChildren().get(0).setVisible(true);
        if(textField==null) return;
        textField.setOnAction(null);
        textField.setOnKeyReleased(null);
        textField = null;

    }

}
