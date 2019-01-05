package ru.biomedis.biomedismair3.TherapyTabs.Profile;

import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Modality;
import ru.biomedis.biomedismair3.*;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.io.File;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.BaseController.getApp;

public class ProfileTable {
    private  ResourceBundle res;
    private TableView<Profile> table;
    private static ProfileTable instance;
    private ContextMenu  profileMenu=new ContextMenu();

    public static  final DataFormat PROFILE_CUT_ITEM_INDEX =new DataFormat("biomedis/cut_profile_item_index");
    public static  DataFormat PROFILE_CUT_ITEM_ID=new DataFormat("biomedis/cut_profile_item_id");
    private Menu translateMenu = new Menu();

    private ObservableList<Profile> masterData = FXCollections.observableArrayList(p -> new Observable[] {p.nameProperty(), p.lastChangeProperty()});
    private FilteredList<Profile> filteredData;
    private SortedList<Profile> sortedData;
    private INamedComparator comparator;


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
        this.table = tableProfile;
        this.res = res;

    }


    private void initTranslateMenu() {
        translateMenu.setText(res.getString("app.menu.translate_to"));
        MenuItem firstItem = new MenuItem(getModel().getProgramLanguage().getName());
        firstItem.setUserData(getModel().getProgramLanguage().getId());
        firstItem.setOnAction(ProfileTable.getInstance()::translateAction);
        translateMenu.getItems().add(firstItem);

        getModel().findAvaliableLangs().stream()
                  .filter(l -> !l.getAbbr().equals(getModel().getProgramLanguage().getAbbr()))
                  .map(l->{
                      MenuItem mItem = new MenuItem(l.getName());
                      mItem.setUserData(l.getId());
                      mItem.setOnAction(ProfileTable.getInstance()::translateAction);
                      return mItem;
                  }).forEach(menuItem -> translateMenu.getItems().add(menuItem));


    }

    private void translateAction(ActionEvent e){
        MenuItem mi =((MenuItem)e.getSource());
        Long langId = (Long)mi.getUserData();
        if(langId==null) {
            Log.logger.error("в элементе меню отсутствует ID языка для перевода");
            return;
        }
        try {


                Profile profile = getSelectedItem();
                if(profile==null) return;
                int index = getAllItems().indexOf(profile);
                getModel().translate(profile, getModel().getLanguage(langId));



            refreshItem(getSelectedItem());
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(index);
            table.getFocusModel().focus(index);

        }catch (Exception ex){
            Log.logger.error("",ex);
            BaseController.showExceptionDialog("Перевод","Ошибка перевода","",ex,getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }

    }

    /**
     * Обновляет элементы таблицы
     * @param pList список элементов, не из базы, а из самой таблицы!!
     */
    public void refreshItems(ObservableList<Profile> pList){
        List<Profile> profiles  = pList.stream().collect(Collectors.toList());
        profiles.forEach(c->refreshItem(c));
    }

    /**
     * Обновляет элемент таблицы
     * @param item элемент из таблицы, не из базы!
     */
    public void refreshItem(Profile item){
        int i = getAllItems().indexOf(item);
        if(i < 0){
            Log.logger.warn("refreshItem - объект отсутствуетс в таблице");
            return;
        }
        getAllItems().set(i,null);
        getAllItems().set(i,item);
    }

    private void initTable(){
        initTranslateMenu();
        //номер по порядку
        TableColumn<Profile,Number> numProfileCol =new TableColumn<>("№");
        numProfileCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue())+1));

        //имя профиля
        TableColumn<Profile,String> nameCol=new TableColumn<>(res.getString("app.table.profile_name"));
        nameCol.cellValueFactoryProperty().setValue(new PropertyValueFactory<Profile, String>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event ->
        {

//            if (!event.getNewValue().equals(event.getOldValue())) {
//
//                String s = event.getNewValue();
//                if (s.length() == 0) {
//                    event.getRowValue().setName(event.getOldValue());
//                    Profile p = event.getRowValue();
//                    int i = table.getItems().indexOf(event.getRowValue());
//                    table.getItems().set(i, null);
//                    table.getItems().set(i, p);
//                    p = null;
//                    table.getSelectionModel().select(i);
//                    return;
//                }
//                event.getRowValue().setName(s);
//                try {
//                    getModel().updateProfile(event.getRowValue());
//                    Profile p = event.getRowValue();
//                    int i = table.getItems().indexOf(event.getRowValue());
//                    table.getItems().set(i, null);
//                    table.getItems().set(i, p);
//                    table.getSelectionModel().select(i);
//                    p = null;
//
//                } catch (Exception e) {
//                    logger.error("",e);
//                }
//
//
//            }


            if (!event.getNewValue().equals(event.getOldValue())) {
                Profile profile = event.getRowValue();
                String s = event.getNewValue();
                if (s.length() == 0) {
                    profile.setName(event.getOldValue());
                    return;
                }

                profile.setName(s);
                profile.setNowChanged();
                try {
                    getModel().updateProfile(profile);

                } catch (Exception e) {
                    Log.logger.error("",e);
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

        //общая длительность, зависит от количества комплексов, програм их частот и мультичастотного режима, также времени на частоту
        TableColumn<Profile,String> lastChangeCol=new TableColumn<>(res.getString("app.last_change"));
        lastChangeCol.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.bind(new StringBinding() {
                {
                    super.bind(param.getValue().lastChangeProperty());//для инициализации расчета
                }

                @Override
                protected String computeValue() {
                    return DateUtil.timeStampToStringDateTime(param.getValue().getLastChange(), false);
                }
            });
            return property;
        });

        timeCol.setStyle( "-fx-alignment: CENTER;");
        lastChangeCol.setStyle( "-fx-alignment: CENTER;");
        weightCol.setStyle( "-fx-alignment: CENTER;");
        table.getColumns().addAll(numProfileCol, nameCol,lastChangeCol, timeCol, weightCol);
        table.placeholderProperty().setValue(new Label(res.getString("app.table.profile_not_avaliable")));
        table.setEditable(true);



        lastChangeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        numProfileCol.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.56));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));
        weightCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));

        numProfileCol.setEditable(false);
        weightCol.setEditable(false);
        lastChangeCol.setEditable(false);
        nameCol.setEditable(true);
        timeCol.setEditable(false);

        lastChangeCol.setSortable(false);
        nameCol.setSortable(false);
        timeCol.setSortable(false);
        weightCol.setSortable(false);
        numProfileCol.setSortable(false);


        //инициализация сортирующей обертки над masterData. Все изменения в  masterData сразу срабатывают на sortedData

        sortedData = new SortedList<>(masterData);
        //Не совсем идеально, тк если профили будут на разных языках, то сортировка корректно будет работать только на языке программы
        comparator = new INamedComparator(Collator.getInstance(new Locale(getModel().getProgramLanguage().getAbbr())));
        sortedData.setComparator((o1, o2) -> comparator.compare(o1, o2));

        table.setItems(sortedData);

        masterData.addAll(getModel().findAllProfiles()
                                          .stream()
                                          .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                                          .collect(Collectors.toList()));
    }

    public void initProfileContextMenu(Runnable onPrintProfile,
                                       Runnable cutInTables,
                                       Runnable pasteInTables,
                                       Runnable deleteInTables,
                                       Runnable pasteInTables_after) {
        MenuItem mip1 = new MenuItem(this.res.getString("app.ui.duplicate"));
        MenuItem mip2 =new MenuItem(this.res.getString("app.menu.insert_before"));
        MenuItem mip3 =new MenuItem(this.res.getString("app.cut"));
        MenuItem mip4 =new MenuItem(this.res.getString("app.delete"));
        MenuItem mip5 =new MenuItem(this.res.getString("app.menu.print_profile"));
        MenuItem mip6 =new SeparatorMenuItem();
        MenuItem mi_insert_botom =new MenuItem(this.res.getString("app.menu.insert_after"));


        mip5.setOnAction(e->onPrintProfile.run());


        //mip3.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        //mip2.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        mip4.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        profileMenu.getItems().addAll(mip1, mip4,mip6,mip5,translateMenu);
        mip1.setOnAction(e->duplicateProfile());
        mip3.setOnAction(e->cutInTables.run());
        mip2.setOnAction(e->pasteInTables.run());
        mip4.setOnAction(e->deleteInTables.run());
        mi_insert_botom.setOnAction(e->pasteInTables_after.run());
        table.setContextMenu(profileMenu);
        profileMenu.setOnShowing(e->{
            mip2.setDisable(false);
            mip3.setDisable(false);
            mip4.setDisable(false);
            mip5.setDisable(false);
            mip1.setDisable(true);
            mi_insert_botom.setDisable(false);
            if(table.getSelectionModel().getSelectedItem()==null) {
                mip2.setDisable(true);
                mip3.setDisable(true);
                mip4.setDisable(true);
                mip5.setDisable(true);
                mi_insert_botom.setDisable(true);
            }else {
                mip1.setDisable(false);
                mip4.setDisable(false);
                Clipboard clipboard= Clipboard.getSystemClipboard();
                if(clipboard.hasContent(PROFILE_CUT_ITEM_ID)){
                    Integer ind = (Integer)clipboard.getContent(PROFILE_CUT_ITEM_INDEX);
                    if(ind ==null) {
                        mip2.setDisable(true);
                        mi_insert_botom.setDisable(true);
                    }
                    else {
                        if(table.getSelectionModel().getSelectedIndex()==ind){
                            mip2.setDisable(true);
                            mi_insert_botom.setDisable(false);
                        }
                        else {
                            mip2.setDisable(false);
                            mi_insert_botom.setDisable(false);
                        }
                    }

                }else  {
                    mip2.setDisable(true);
                    mi_insert_botom.setDisable(true);
                }
            }

        });
    }

    private void duplicateProfile() {
        try {
            Profile p = getModel().duplicateProfile(getSelectedItem());
            int index = getAllItems().indexOf(getSelectedItem());
            if(index < getAllItems().size()-1) getAllItems().add(index+1, p);
            else getAllItems().add(p);
            select(p);
            scrollTo(p);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ModelDataApp getModel() {
        return App.getStaticModel();
    }

    public Profile getSelectedItem(){
        return table.getSelectionModel().getSelectedItem();
    }
    public Integer getSelectedIndex(){return table.getSelectionModel().getSelectedIndex();}

    public ObservableList<Profile> getAllItems(){
        return masterData;
    }

    public void clearSelection(){
        table.getSelectionModel().clearSelection();
    }
    public void select(int index){
        table.getSelectionModel().select(index);
    }
    public void select(Profile p){
        table.getSelectionModel().select(p);
    }
    public void requestFocus(){
        table.requestFocus();
    }

    public void setItemFocus(int index){
        table.getFocusModel().focus(index);
    }
    public void scrollTo(int index){
        table.scrollTo(index);
    }
    public void scrollTo(Profile p){
        table.scrollTo(p);
    }
    /**
     * Редактируются ли ячейки в таблице сейчас
     * @return
     */
    public boolean isTextEdited(){
        return table.getEditingCell() != null;
    }

    public ReadOnlyObjectProperty<Profile> getSelectedItemProperty(){
        return table.getSelectionModel().selectedItemProperty();
    }
}
