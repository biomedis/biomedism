package ru.biomedis.biomedismair3.TherapyTabs.Complex;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.CellFactories.TextAreaTableCell;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.io.File;
import java.util.ResourceBundle;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.Log.logger;

public class ComplexTable {
    private  ResourceBundle res;
    private  Image imageCancel;
    private  Image imageDone;
    private TableView<TherapyComplex> tableComplex;
    private static ComplexTable instance;
    private SimpleStringProperty textComplexTime=new SimpleStringProperty();//хранит время комплекса в строковом представлении и через # id комплекса. Используется в выводе в табе комплекса времени и его обновления

    public SimpleStringProperty textComplexTimeProperty() {
        return textComplexTime;
    }

    public static ComplexTable init(TableView<TherapyComplex> tableComplex, ResourceBundle res, Image imageCancel, Image imageDone){

        if(instance==null){
            instance =new ComplexTable(tableComplex,res,imageCancel,imageDone);
            instance.initTable();
        }
        return instance;
    }

    public static ComplexTable getInstance(){

        if(instance==null){
           return null;
        }else return  instance;
    }
    private ComplexTable() {
    }

    private ComplexTable(TableView<TherapyComplex> tableComplex, ResourceBundle res,Image imageCancel, Image imageDone) {
        this.tableComplex = tableComplex;
        this.res = res;

        this.imageCancel = imageCancel;
        this.imageDone = imageDone;
    }

    private void initTable(){
        //номер по порядку
        TableColumn<TherapyComplex,Number> numComplexCol =new TableColumn<>("№");
        numComplexCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue()) + 1));


        //имя
        TableColumn<TherapyComplex,String> nameColTC=new TableColumn<>(res.getString("app.table.name_complex"));
        nameColTC.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyComplex, String>("name"));
        nameColTC.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColTC.setOnEditCommit(event ->
        {

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = event.getNewValue();
                if (s.length() == 0) {
                    event.getRowValue().setName(event.getOldValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    tableComplex.getSelectionModel().select(i);
                    p = null;
                    return;
                }
                event.getRowValue().setName(s);
                try {
                    getModel().updateTherapyComplex(event.getRowValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    tableComplex.getSelectionModel().select(i);
                    p = null;

                } catch (Exception e) {
                    logger.error("",e);
                }


            }
        });

        //описание
        TableColumn<TherapyComplex,String> descColTC=new TableColumn<>(res.getString("app.table.complex_descr"));
        descColTC.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyComplex, String>("description"));
        descColTC.setCellFactory(TextAreaTableCell.forTableColumn());
        descColTC.setOnEditCommit(event ->
        {

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = event.getNewValue();
                if (s.length() == 0) {
                    event.getRowValue().setDescription(event.getOldValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    p = null;
                    return;
                }
                event.getRowValue().setDescription(s);
                try {
                    getModel().updateTherapyComplex(event.getRowValue());
                    TherapyComplex p = event.getRowValue();
                    int i = tableComplex.getItems().indexOf(event.getRowValue());
                    tableComplex.getItems().set(i, null);
                    tableComplex.getItems().set(i, p);
                    p = null;

                } catch (Exception e) {
                    logger.error("",e);
                }


            }
        });

        //общая длительность, зависит от количества програм их частот и мультичастотного режима, также времени на частоту
        TableColumn<TherapyComplex,String> timeColTC=new TableColumn<>(res.getString("app.table.delay"));
        //  timeColTC.setCellValueFactory(param -> new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(getModel().getTimeTherapyComplex(param.getValue()))));
        //пересчет индуцируется при изменении свойства time


        timeColTC.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().timeProperty());
                }

                @Override
                protected String computeValue() {
                    String s = DateUtil.convertSecondsToHMmSs(getModel().getTimeTherapyComplex(param.getValue()));
                    textComplexTime.setValue(s+"#"+param.getValue().getId().longValue());
                    return s;
                }
            });
            return property;
        });


        TableColumn<TherapyComplex,Boolean>fileComplexCol=new TableColumn<>(res.getString("app.table.file"));
        fileComplexCol.cellValueFactoryProperty().setValue(param -> {
            SimpleBooleanProperty property = new SimpleBooleanProperty();
            property.bind(param.getValue().changedProperty());
            return property;
        });
        fileComplexCol.setCellFactory(col ->
        {
            TableCell<TherapyComplex, Boolean> cell = new TableCell<TherapyComplex, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {

                    super.updateItem(item, empty);
                    this.setText(null);
                    this.setGraphic(null);

                    HBox hbox=null;
                    ImageView iv=null;

                    if( this.getUserData()!=null)
                    {
                        hbox=(HBox)this.getUserData();
                        if(hbox!=null){
                            iv=(ImageView)hbox.getChildren().get(0);

                        }else {
                            iv=new ImageView();
                            hbox=new HBox();
                            hbox.setSpacing(3);
                            hbox.getChildren().addAll(iv);
                        }
                    }else {
                        iv=new ImageView(imageCancel);

                        hbox=new HBox();
                        hbox.setSpacing(3);
                        hbox.getChildren().addAll(iv);
                        this.setUserData(hbox);
                    }


                    if (!empty) {
                        if (this.getTableRow().getItem() == null) {
                            setText("");
                            return;
                        }
                        if (item)  iv.setImage(imageCancel);
                        else {
                            TherapyComplex thisComplex = (TherapyComplex) getTableRow().getItem();
                            if(thisComplex==null) return;

                            if(getModel().countTherapyPrograms(thisComplex)==0)iv.setImage(imageCancel);
                            else if(getModel().hasNeedGenerateProgramInComplex(thisComplex))  iv.setImage(imageCancel);
                            else  {
                                long sum=0;
                                File f;
                                for (Long id : getModel().getTherapyComplexFiles(thisComplex)) {
                                    f=new File(getApp().getDataDir(),id+".dat");
                                    if(f.exists())sum+=f.length();
                                }
                                for (String v : getModel().mp3ProgramPathsInComplex(thisComplex)) {

                                    f = new File(v);
                                    if (f.exists()) sum += f.length();
                                }


                                if(sum>0) setText(Math.ceil((double)sum / 1048576.0) + " Mb");
                                iv.setImage(imageDone);
                            }

                        }



                        setGraphic(hbox);
                    }
                }
            };

            return cell;
        });


        numComplexCol.setStyle( "-fx-alignment: CENTER;");
        timeColTC.setStyle( "-fx-alignment: CENTER;");
        nameColTC.setStyle( "-fx-alignment: CENTER-LEFT;");
        this.tableComplex.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableComplex.getColumns().addAll(numComplexCol, nameColTC, descColTC, timeColTC, fileComplexCol);
        tableComplex.placeholderProperty().setValue(new Label(res.getString("app.table.complex_placeholder")));
        tableComplex.setEditable(true);


        numComplexCol.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.1));
        nameColTC.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.325));
        descColTC.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.325));
        timeColTC.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.1));
        fileComplexCol.prefWidthProperty().bind(tableComplex.widthProperty().multiply(0.15));

        numComplexCol.setSortable(false);
        nameColTC.setSortable(false);
        descColTC.setSortable(false);
        timeColTC.setSortable(false);
        fileComplexCol.setSortable(false);

        fileComplexCol.setEditable(true);
    }

    private ModelDataApp getModel() {
        return App.getStaticModel();
    }
}
