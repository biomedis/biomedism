package ru.biomedis.biomedismair3.TherapyTabs.Profile;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.Log.logger;

public class ProfileTable {
    private  ResourceBundle res;
    private TableView<Profile> table;
    private static ProfileTable instance;
    private ContextMenu  profileMenu=new ContextMenu();

    public static  final DataFormat PROFILE_CUT_ITEM_INDEX =new DataFormat("biomedis/cut_profile_item_index");
    public static  DataFormat PROFILE_CUT_ITEM_ID=new DataFormat("biomedis/cut_profile_item_id");
    private Menu translateMenu = new Menu();

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

            if (!event.getNewValue().equals(event.getOldValue())) {

                String s = event.getNewValue();
                if (s.length() == 0) {
                    event.getRowValue().setName(event.getOldValue());
                    Profile p = event.getRowValue();
                    int i = table.getItems().indexOf(event.getRowValue());
                    table.getItems().set(i, null);
                    table.getItems().set(i, p);
                    p = null;
                    table.getSelectionModel().select(i);
                    return;
                }
                event.getRowValue().setName(s);
                try {
                    getModel().updateProfile(event.getRowValue());
                    Profile p = event.getRowValue();
                    int i = table.getItems().indexOf(event.getRowValue());
                    table.getItems().set(i, null);
                    table.getItems().set(i, p);
                    table.getSelectionModel().select(i);
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
        table.getColumns().addAll(numProfileCol, nameCol, timeCol, weightCol);
        table.placeholderProperty().setValue(new Label(res.getString("app.table.profile_not_avaliable")));
        table.setEditable(true);

        table.getItems().addAll(getModel().findAllProfiles()
                                          .stream()
                                          .filter(i->!i.getName().equals(App.BIOFON_PROFILE_NAME))
                                          .collect(Collectors.toList()));


        numProfileCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.50));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.25));
        weightCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));

        weightCol.setEditable(false);
        numProfileCol.setEditable(false);
        nameCol.setEditable(true);
        timeCol.setEditable(false);

        numProfileCol.setSortable(false);
        nameCol.setSortable(false);
        timeCol.setSortable(false);
        weightCol.setSortable(false);
    }

    public void initProfileContextMenu(Runnable onPrintProfile, Runnable cutInTables, Runnable pasteInTables,Runnable deleteInTables) {
        //MenuItem mip1 = new MenuItem(this.res.getString("app.ui.copy"));
        MenuItem mip2 =new MenuItem(this.res.getString("app.ui.paste"));
        MenuItem mip3 =new MenuItem(this.res.getString("app.cut"));
        MenuItem mip4 =new MenuItem(this.res.getString("app.delete"));
        MenuItem mip5 =new MenuItem(this.res.getString("app.menu.print_profile"));
        MenuItem mip6 =new SeparatorMenuItem();

        mip5.setOnAction(e->onPrintProfile.run());


        mip3.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        //mip1.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        mip2.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        mip4.setAccelerator(KeyCombination.keyCombination("Delete"));
        profileMenu.getItems().addAll(mip3,mip2,mip4,mip6,mip5,translateMenu);
        mip3.setOnAction(e->cutInTables.run());
        mip2.setOnAction(e->pasteInTables.run());
        mip4.setOnAction(e->deleteInTables.run());
        table.setContextMenu(profileMenu);
        profileMenu.setOnShowing(e->{
            mip2.setDisable(false);
            mip3.setDisable(false);
            mip4.setDisable(false);
            mip5.setDisable(false);
            if(table.getSelectionModel().getSelectedItem()==null) {
                mip2.setDisable(true);
                mip3.setDisable(true);
                mip4.setDisable(true);
                mip5.setDisable(true);
            }else {

                mip4.setDisable(false);
                Clipboard clipboard= Clipboard.getSystemClipboard();
                if(clipboard.hasContent(PROFILE_CUT_ITEM_ID)){
                    Integer ind = (Integer)clipboard.getContent(PROFILE_CUT_ITEM_INDEX);
                    if(ind ==null) mip2.setDisable(true);
                    else {
                        if(table.getSelectionModel().getSelectedIndex()==ind)mip2.setDisable(true);
                        else mip2.setDisable(false);
                    }

                }else  mip2.setDisable(true);
            }

        });
    }

    private ModelDataApp getModel() {
        return App.getStaticModel();
    }

    public Profile getSelectedItem(){
        return table.getSelectionModel().getSelectedItem();
    }
    public Integer getSelectedIndex(){return table.getSelectionModel().getSelectedIndex();}

    public ObservableList<Profile> getAllItems(){
        return table.getItems();
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
        return  table.getEditingCell()==null?false:true;
    }

    public ReadOnlyObjectProperty<Profile> getSelectedItemProperty(){
        return table.getSelectionModel().selectedItemProperty();
    }
}
