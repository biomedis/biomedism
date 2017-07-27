package ru.biomedis.biomedismair3.TherapyTabs.Programs;

import com.mpatric.mp3agic.Mp3File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.SearchFreqs;
import ru.biomedis.biomedismair3.entity.SearchName;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.io.File;
import java.util.ResourceBundle;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.Log.logger;

public class ProgramTable {
    private  ResourceBundle res;
    private  Image imageCancel;
    private  Image imageDone;
    private  Image imageSeq;
    private  Image imageParallel;
    private TableView<TherapyProgram> tableProgram;
    private static ProgramTable instance;
    private NeedUpdateComplexTime needUpdateListener;


    public static ProgramTable getInstance(TableView<TherapyProgram> tableComplex, ResourceBundle res, Image imageCancel, Image imageDone, Image imageSeq, Image imageParallel,NeedUpdateComplexTime needUpdateListener){

        if(instance==null){
            instance =new ProgramTable(tableComplex,res,imageCancel,imageDone,imageSeq,imageParallel, needUpdateListener);
            instance.initTable();
        }
        return instance;
    }

    private ProgramTable() {
    }

    private ProgramTable(TableView<TherapyProgram> tableProgram, ResourceBundle res, Image imageCancel, Image imageDone, Image imageSeq, Image imageParallel,NeedUpdateComplexTime needUpdateListener) {
        this.tableProgram = tableProgram;
        this.res = res;

        this.imageCancel = imageCancel;
        this.imageDone = imageDone;
        this.imageSeq = imageSeq;
        this.imageParallel = imageParallel;
        this.needUpdateListener = needUpdateListener;
    }

    private void initTable(){
        tableProgram.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        //номер по порядку
        TableColumn<TherapyProgram,Number> numProgCol =new TableColumn<>("№");
        numProgCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue()) + 1));


        //имя
        TableColumn<TherapyProgram,String> nameColTP=new TableColumn<>(res.getString("app.table.program_name"));
        nameColTP.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyProgram, String>("name"));
        nameColTP.setCellFactory(param1 -> {

            TableCell<TherapyProgram, String> cell = new TableCell<TherapyProgram, String>() {
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
            };


            return cell;
        });
        //частоты
        TableColumn<TherapyProgram,String> descColTP=new TableColumn<>(res.getString("app.table.freqs"));
        descColTP.cellValueFactoryProperty().setValue(param -> new SimpleStringProperty(param.getValue().getFrequencies().replace(";", ";  ")));
        descColTP.setCellFactory(param1 -> {

            TableCell<TherapyProgram, String> cell = new TableCell<TherapyProgram, String>() {
                //private Text text;
                private FlowPane textFlow;
                //private  Font normalFont = Font.font(null, FontWeight.NORMAL, 12);
                private  Font boldFont = Font.font(null, FontWeight.BOLD, 12);
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
            };


            return cell;
        });


        //общая длительность, зависит от количества  частот и мультичастотного режима, также времени на частоту и пачек частот
        TableColumn<TherapyProgram,String> timeColTP=new TableColumn<>(res.getString("app.table.delay"));
        timeColTP.setCellValueFactory(param ->
        {
            if(param.getValue().isMp3())
            {

                Mp3File mp3file = null;
                try {
                    mp3file = new Mp3File(param.getValue().getFrequencies());
                } catch (Exception e)
                {

                    mp3file=null;
                }

                if(mp3file!=null)   return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds()));
                else return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(0));

            }else
            {

                return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(calcTherapyProgramTime(param.getValue())));
            }
        });


        TableColumn<TherapyProgram,Boolean> fileCol=new TableColumn<>(res.getString("app.table.file"));
        fileCol.setCellValueFactory(param -> {
            SimpleBooleanProperty property = new SimpleBooleanProperty();
            property.bind(param.getValue().changedProperty().or(param.getValue().changedProperty()));
            return property;
        });

        fileCol.setCellFactory(col ->
                {
                    TableCell<TherapyProgram, Boolean> cell = new TableCell<TherapyProgram, Boolean>() {
                        @Override
                        protected void updateItem(Boolean item, boolean empty) {

                            super.updateItem(item, empty);
                            this.setText(null);
                            this.setGraphic(null);

                            HBox hbox=null;
                            ImageView iv=null;
                            ImageView iv2=null;
                            if( this.getUserData()!=null)
                            {
                                hbox=(HBox)this.getUserData();
                                if(hbox!=null){
                                    iv=(ImageView)hbox.getChildren().get(0);
                                    iv2=(ImageView)hbox.getChildren().get(1);
                                }else {
                                    iv=new ImageView();
                                    iv2=new ImageView();
                                    hbox=new HBox();
                                    hbox.setSpacing(3);
                                    hbox.getChildren().addAll(iv,iv2);
                                }
                            }else {
                                iv=new ImageView(imageCancel);
                                iv2=new ImageView(imageDone);
                                hbox=new HBox();
                                hbox.setSpacing(3);
                                hbox.getChildren().addAll(iv,iv2);
                                this.setUserData(hbox);
                            }


                            if (!empty) {
                                if (this.getTableRow().getItem() == null) {setText(""); return;}

                                File f;
                                if(((TherapyProgram) this.getTableRow().getItem()).isMp3())
                                {
                                    iv2.setImage(null);
                                    //в любом случае проверим наличие файла
                                    f = new File(((TherapyProgram) this.getTableRow().getItem()).getFrequencies());


                                    if (f.exists())
                                    {
                                        setText(Math.ceil((double) f.length() / 1048576.0) + " Mb");
                                        iv.setImage(imageDone);
                                    }
                                    else
                                    {
                                        setText("");
                                        iv.setImage(imageCancel);


                                        //если установленно что не требуется генерация, а файла нет, то изменим флаг генерации и иконку
                                        if(((TherapyProgram) this.getTableRow().getItem()).isChanged()==false)
                                        {
                                            ((TherapyProgram) this.getTableRow().getItem()).setChanged(true);
                                            try {
                                                getModel().updateTherapyProgram(((TherapyProgram) this.getTableRow().getItem()));
                                                needUpdateListener.update();
                                            } catch (Exception e) {
                                                logger.error("",e);
                                            }
                                        }
                                    }
                                }else
                                {
                                    if(((TherapyProgram) this.getTableRow().getItem()).isMultyFreq())  iv2.setImage(imageParallel);
                                    else  iv2.setImage(imageSeq);

                                    if (item) {
                                        iv.setImage(imageCancel);
                                        setText("");
                                    } else
                                    {
                                        long id = ((TherapyProgram) this.getTableRow().getItem()).getId();
                                        f = new File(getApp().getDataDir(), id + ".dat");


                                        if (f.exists())
                                        {
                                            setText(Math.ceil((double) f.length() / 1048576.0) + " Mb");
                                            iv.setImage(imageDone);

                                        }
                                        else{ setText("");    iv.setImage(imageCancel);}



                                    }

                                }
                                setGraphic(hbox);
                            }
                        }
                    };

                    return cell;
                }
        );


        numProgCol.setStyle( "-fx-alignment: CENTER;");
        timeColTP.setStyle( "-fx-alignment: CENTER;");
        nameColTP.setStyle( "-fx-alignment: CENTER-LEFT;");
        tableProgram.getColumns().addAll(numProgCol, nameColTP, descColTP, timeColTP, fileCol);
        tableProgram.placeholderProperty().setValue(new Label(res.getString("app.table.programm_placeholder")));

        tableProgram.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        numProgCol.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.033));
        nameColTP.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.2));
        descColTP.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.557));
        timeColTP.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.1));
        fileCol.prefWidthProperty().bind(tableProgram.widthProperty().multiply(0.11));

        numProgCol.setSortable(false);
        nameColTP.setSortable(false);
        descColTP.setSortable(false);
        timeColTP.setSortable(false);
        fileCol.setSortable(false);
    }

    /**
     * Считает время терапевтической программы
     * @param tp
     * @return
     */
    public  static long calcTherapyProgramTime(TherapyProgram tp){
        final TherapyComplex selectedComplex = tp.getTherapyComplex();
        int freqBundlesCount=1;//сколько пачек получем из частот программы
        if(tp.isMultyFreq() &&  selectedComplex.getBundlesLength()>=2){
            int numFreqsForce =tp.getNumFreqsForce();
            freqBundlesCount=(int)Math.ceil((float)numFreqsForce/(float)selectedComplex.getBundlesLength());
        }

        long tSec;
        if(tp.isMultyFreq()) tSec = selectedComplex.getTimeForFrequency()*freqBundlesCount;
        else tSec = tp.getNumFreqs() * selectedComplex.getTimeForFrequency();
        return tSec;
    }

    private ModelDataApp getModel() {
        return App.getStaticModel();
    }

    public interface NeedUpdateComplexTime{
        public void update();
    }
}
