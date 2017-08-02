package ru.biomedis.biomedismair3.TherapyTabs.Programs;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import ru.biomedis.biomedismair3.entity.SearchName;
import ru.biomedis.biomedismair3.entity.TherapyProgram;

public class NameProgramTableCell extends TableCell<TherapyProgram,String>{
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
            TherapyProgram thisProgram = (TherapyProgram) getTableRow().getItem();
            if (thisProgram == null) return;
            vbox.getChildren().clear();
            tHbox.getChildren().clear();
            bHbox.getChildren().clear();

            // setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            vbox.getChildren().add(tHbox);
            if (thisProgram.isMatchedAnyName()) {

                Label lbl;
                if (thisProgram.isMatchedName()) {

                    for (SearchName.NamePart namePart : thisProgram.getSearchResultName()) {
                        lbl = new Label(namePart.getPart());
                        if (namePart.isMatched()) lbl.setFont(boldFont);
                        tHbox.getChildren().add(lbl);
                    }
                }else {

                    lbl = new Label(thisProgram.getName());
                    tHbox.getChildren().add(lbl);
                }

                if (thisProgram.isMatchedOName()) {
                    vbox.getChildren().add(bHbox);
                    for (SearchName.NamePart namePart : thisProgram.getSearchResultOName()) {
                        lbl = new Label(namePart.getPart());
                        if (namePart.isMatched()) lbl.setFont(boldFont);
                        else  lbl.setFont(italicFont);
                        lbl.setTextFill(Color.DARKSLATEGRAY);
                        bHbox.getChildren().add(lbl);
                    }
                }else {
                    if(!thisProgram.getOname().isEmpty()){
                        vbox.getChildren().add(bHbox);
                        lbl = new Label(thisProgram.getOname());
                        lbl.setFont(italicFont);
                        lbl.setTextFill(Color.DARKSLATEGRAY);
                        bHbox.getChildren().add(lbl);
                    }

                }


            } else {
                Label lbl;
                if(!thisProgram.getOname().isEmpty()){
                    vbox.getChildren().add(bHbox);
                    lbl = new Label(thisProgram.getOname());
                    lbl.setFont(italicFont);
                    lbl.setTextFill(Color.DARKSLATEGRAY);
                    bHbox.getChildren().add(lbl);
                }
                lbl = new Label(thisProgram.getName());
                tHbox.getChildren().add(lbl);
            }

            setGraphic(vbox);
        }
    }
}
