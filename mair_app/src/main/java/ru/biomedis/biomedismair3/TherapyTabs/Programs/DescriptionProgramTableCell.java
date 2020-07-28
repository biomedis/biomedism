package ru.biomedis.biomedismair3.TherapyTabs.Programs;

import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ru.biomedis.biomedismair3.entity.SearchFreqs;
import ru.biomedis.biomedismair3.entity.TherapyProgram;

public class DescriptionProgramTableCell extends TableCell<TherapyProgram, String> {
    //private Text text;
    private FlowPane textFlow;
    //private  Font normalFont = Font.font(null, FontWeight.NORMAL, 12);
    private Font boldFont = Font.font(null, FontWeight.BOLD, 12);
    private Text text;
    private Insets padding =new Insets(0,3,0,0);
    private Color colorAllMatching = Color.rgb(136, 0, 255);
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if(textFlow==null) {
            textFlow=new FlowPane();
            textFlow.setPrefSize(USE_COMPUTED_SIZE,17);
            textFlow.setPadding(new Insets(0));
            textFlow.setMaxWidth(Double.MAX_VALUE);

        }
        this.setText(null);
        this.setGraphic(null);
        if (!empty) {


            TherapyProgram thisProgram = (TherapyProgram) getTableRow().getItem();
            if(thisProgram==null) return;
            textFlow.getChildren().clear();


            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            if(thisProgram.isMatchedFreqs()) {
                Label lbl;
                for (SearchFreqs.Freq freq : thisProgram.getSearchResultFreqs()) {
                    if(freq.isMatched()){

                        lbl =  new Label(freq.getFreq());
                        lbl.setFont(boldFont);
                        if(thisProgram.hasAllFreqListMatching()) lbl.setTextFill(colorAllMatching);
                        else lbl.setTextFill(Color.BLACK);
                        textFlow.getChildren().add(lbl);
                        lbl=new Label(freq.getDelmitter());
                        lbl.setTextFill(Color.BLACK);
                        if(freq.getDelmitter().equals(";"))  lbl.setPadding(padding);
                        textFlow.getChildren().add(lbl);


                    }else {
                        lbl = new Label(freq.getFreq().concat(freq.getDelmitter()));
                        lbl.setTextFill(Color.BLACK);
                        if(freq.getDelmitter().equals(";"))  lbl.setPadding(padding);
                        textFlow.getChildren().add(lbl);
                    }
                }


                setGraphic(textFlow);
            }else {
                if(text==null) {
                    text = new Text(item);
                    text.setWrappingWidth((getTableColumn().getWidth())); // Setting the wrapping width to the Text
                    text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                }else text.setText(item);

                setGraphic(text);
            }


        }
    }
}
