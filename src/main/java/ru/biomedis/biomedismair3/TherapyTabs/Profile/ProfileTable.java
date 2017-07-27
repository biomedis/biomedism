package ru.biomedis.biomedismair3.TherapyTabs.Profile;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.io.File;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.Log.logger;

public class ProfileTable {
    private  ResourceBundle res;
    private TableView<Profile> tableProfile;
    private static ProfileTable instance;

    public static ProfileTable init(TableView<Profile> tableProfile, ResourceBundle res){

        if(instance==null){
            instance =new ProfileTable(tableProfile,res);
            instance.initTable();
        }
        return instance;
    }
    public static ProfileTable getInstance(){

        if(instance==null){
           return null;
        }else   return instance;
    }
    private ProfileTable() {
    }

    private ProfileTable(TableView<Profile> tableProfile,ResourceBundle res) {
        this.tableProfile = tableProfile;
        this.res = res;

    }

    private void initTable(){
        //номер по порядку
        TableColumn<Profile,Number> numProfileCol =new TableColumn<>("№");
        numProfileCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue())+1));

        //имя профиля
        TableColumn<Profile,String> nameCol=new TableColumn<>(res.getString("app.table.profile_name"));
        nameCol.cellValueFactoryProperty().setValue(new PropertyValueFactory<Profile, String>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event ->
        {

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = event.getNewValue();
                if (s.length() == 0) {
                    event.getRowValue().setName(event.getOldValue());
                    Profile p = event.getRowValue();
                    int i = tableProfile.getItems().indexOf(event.getRowValue());
                    tableProfile.getItems().set(i, null);
                    tableProfile.getItems().set(i, p);
                    p = null;
                    tableProfile.getSelectionModel().select(i);
                    return;
                }
                event.getRowValue().setName(s);
                try {
                    getModel().updateProfile(event.getRowValue());
                    Profile p = event.getRowValue();
                    int i = tableProfile.getItems().indexOf(event.getRowValue());
                    tableProfile.getItems().set(i, null);
                    tableProfile.getItems().set(i, p);
                    tableProfile.getSelectionModel().select(i);
                    p = null;

                } catch (Exception e) {
                    logger.error("",e);
                }


            }
        });


        //общая длительность, зависит от количества комплексов, програм их частот и мультичастотного режима, также времени на частоту
        TableColumn<Profile,String> timeCol=new TableColumn<>(res.getString("app.table.delay"));
        timeCol.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().timeProperty());//используем фейковое свойство, для инициализации расчета
                }

                @Override
                protected String computeValue() {
                    return DateUtil.convertSecondsToHMmSs(getModel().getTimeProfile(param.getValue()));
                }
            });
            return property;
        });


        TableColumn<Profile,String> weightCol=new TableColumn<>(res.getString("app.table.file_size"));
        weightCol.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().profileWeightProperty());//используем фейковое свойство, для инициализации расчета
                }

                @Override
                protected String computeValue() {

                    File f = null;
                    double summ = 0;
                    if(getModel().isNeedGenerateFilesInProfile(param.getValue())) return "";
                    for (Long v : getModel().getProfileFiles(param.getValue())) {

                        f = new File(getApp().getDataDir(), v + ".dat");
                        if (f.exists()) summ += f.length() ;
                    }
                    for (String v : getModel().mp3ProgramPathsInProfile(param.getValue())) {

                        f = new File(v);
                        if (f.exists()) summ += f.length();
                    }
                    summ = (double)summ / 1048576;
                    return Math.ceil(summ) + " Mb";
                }
            });
            return property;
        });

        timeCol.setStyle( "-fx-alignment: CENTER;");
        numProfileCol.setStyle( "-fx-alignment: CENTER;");
        weightCol.setStyle( "-fx-alignment: CENTER;");
        tableProfile.getColumns().addAll(numProfileCol, nameCol, timeCol, weightCol);
        tableProfile.placeholderProperty().setValue(new Label(res.getString("app.table.profile_not_avaliable")));
        tableProfile.setEditable(true);

        tableProfile.getItems().addAll(getModel().findAllProfiles()
                                                 .stream()
                                                 .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                                                 .collect(Collectors.toList()));


        numProfileCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.1));
        nameCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.50));
        timeCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.25));
        weightCol.prefWidthProperty().bind(tableProfile.widthProperty().multiply(0.15));

        weightCol.setEditable(false);
        numProfileCol.setEditable(false);
        nameCol.setEditable(true);
        timeCol.setEditable(false);

        numProfileCol.setSortable(false);
        nameCol.setSortable(false);
        timeCol.setSortable(false);
        weightCol.setSortable(false);
    }

    private ModelDataApp getModel() {
        return App.getStaticModel();
    }
}
