package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import ru.biomedis.biomedismair3.entity.TherapyComplex;

public class NameComplexTableCell extends TableCell<TherapyComplex,String>{
    private VBox vbox = new VBox(3);
    private HBox tHbox = new HBox();
    private HBox bHbox = new HBox();
    private Font boldFont = Font.font(null, FontWeight.BOLD, 12);
    private Font italicFont = Font.font(null, FontPosture.ITALIC, 12);
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (vbox == null) {
            vbox = new VBox();
            vbox.setMaxWidth(Double.MAX_VALUE);
            vbox.setAlignment(Pos.CENTER_LEFT);

            tHbox = new HBox();
            tHbox.setMaxWidth(Double.MAX_VALUE);
            bHbox.setAlignment(Pos.CENTER_LEFT);

            bHbox = new HBox();
            bHbox.setMaxWidth(Double.MAX_VALUE);
            bHbox.setAlignment(Pos.CENTER_LEFT);
        }
        this.setText(null);
        this.setGraphic(null);
        if (!empty) {
            TherapyComplex thisComplex = (TherapyComplex) getTableRow().getItem();
            if (thisComplex == null) return;
            vbox.getChildren().clear();
            tHbox.getChildren().clear();
            bHbox.getChildren().clear();

            // setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            vbox.getChildren().add(tHbox);

                Label lbl;
                if(!thisComplex.getOname().isEmpty()){
                    vbox.getChildren().add(bHbox);
                    lbl = new Label(thisComplex.getOname());
                    lbl.setFont(italicFont);
                    lbl.setTextFill(Color.DARKSLATEGRAY);
                    bHbox.getChildren().add(lbl);
                }
                lbl = new Label(thisComplex.getName());
                tHbox.getChildren().add(lbl);


            setGraphic(vbox);
        }
    }
}
