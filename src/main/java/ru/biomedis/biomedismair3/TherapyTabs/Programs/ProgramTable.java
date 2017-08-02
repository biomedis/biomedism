package ru.biomedis.biomedismair3.TherapyTabs.Programs;

import com.mpatric.mp3agic.Mp3File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexTable;
import ru.biomedis.biomedismair3.TherapyTabs.TablesCommon;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.BaseController.getApp;
import static ru.biomedis.biomedismair3.Log.logger;

public class ProgramTable {
    private  ResourceBundle res;
    private  Image imageCancel;
    private  Image imageDone;
    private  Image imageSeq;
    private  Image imageParallel;
    private TableView<TherapyProgram> table;
    private static ProgramTable instance;
    private NeedUpdateComplexTime needUpdateListener;

    public static final DataFormat PROGRAM_CUT_ITEM_INDEX =new DataFormat("biomedis/cut_programitem_index");
    public static final DataFormat PROGRAM_CUT_ITEM_ID=new DataFormat("biomedis/cut_programitem_id");
    public static final DataFormat PROGRAM_CUT_ITEM_COMPLEX =new DataFormat("biomedis/cut_programitem_complex");
    public static final DataFormat PROGRAM_COPY_ITEM=new DataFormat("biomedis/copy_programitem");
    private ContextMenu programMenu =new ContextMenu();
    private Menu translateMenu = new Menu();

    public static ProgramTable init(TableView<TherapyProgram> tableProgram, ResourceBundle res, Image imageCancel, Image imageDone, Image imageSeq, Image imageParallel,NeedUpdateComplexTime needUpdateListener){

        if(instance==null){
            instance =new ProgramTable(tableProgram,res,imageCancel,imageDone,imageSeq,imageParallel, needUpdateListener);
            instance.initTable();
        }
        return instance;
    }

    public static ProgramTable getInstance(){

        if(instance==null){
           return null;
        }else  return instance;
    }

    private ProgramTable() {
    }

    private ProgramTable(TableView<TherapyProgram> tableProgram, ResourceBundle res, Image imageCancel, Image imageDone, Image imageSeq, Image imageParallel,NeedUpdateComplexTime needUpdateListener) {
        this.table = tableProgram;
        this.res = res;

        this.imageCancel = imageCancel;
        this.imageDone = imageDone;
        this.imageSeq = imageSeq;
        this.imageParallel = imageParallel;
        this.needUpdateListener = needUpdateListener;


    }




    private void initTranslateMenu() {
        translateMenu.setText(res.getString("app.menu.translate_to"));
        MenuItem firstItem = new MenuItem(getModel().getProgramLanguage().getName());
        firstItem.setUserData(getModel().getProgramLanguage().getId());
        firstItem.setOnAction(ProgramTable.getInstance()::translateAction);
        translateMenu.getItems().add(firstItem);

        getModel().findAvaliableLangs().stream()
                        .filter(l -> !l.getAbbr().equals(getModel().getProgramLanguage().getAbbr()))
                        .map(l->{
                            MenuItem mItem = new MenuItem(l.getName());
                            mItem.setUserData(l.getId());
                            mItem.setOnAction(ProgramTable.getInstance()::translateAction);
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

            for (TherapyProgram program : getSelectedItems()) {
                getModel().translate(program, getModel().getLanguage(langId));

            }

            refreshItems(getSelectedItems());
            table.getSelectionModel().clearSelection();

        }catch (Exception ex){
            BaseController.showExceptionDialog("Перевод","Ошибка перевода","",ex,getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }

    }

    /**
     * Обновляет элементы таблицы
     * @param pList список элементов, не из базы, а из самой таблицы!!
     */
    public void refreshItems(ObservableList<TherapyProgram> pList){
        List<TherapyProgram> programs  = pList.stream().collect(Collectors.toList());
        programs.forEach(p->refreshItem(p));
    }

    /**
     * Обновляет элемент таблицы
     * @param item элемент из таблицы, не из базы!
     */
    public void refreshItem(TherapyProgram item){
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

        table.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        //номер по порядку
        TableColumn<TherapyProgram,Number> numProgCol =new TableColumn<>("№");
        numProgCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getTableView().getItems().indexOf(param.getValue()) + 1));


        //имя
        TableColumn<TherapyProgram,String> nameColTP=new TableColumn<>(res.getString("app.table.program_name"));
        nameColTP.cellValueFactoryProperty().setValue(new PropertyValueFactory<TherapyProgram, String>("name"));
        nameColTP.setCellFactory(param1 ->new NameProgramTableCell());
        //частоты
        TableColumn<TherapyProgram,String> descColTP=new TableColumn<>(res.getString("app.table.freqs"));
        descColTP.cellValueFactoryProperty().setValue(param -> new SimpleStringProperty(param.getValue().getFrequencies().replace(";", ";  ")));
        descColTP.setCellFactory(param1 -> new DescriptionProgramTableCell());


        //общая длительность, зависит от количества  частот и мультичастотного режима, также времени на частоту и пачек частот
        TableColumn<TherapyProgram,String> timeColTP=new TableColumn<>(res.getString("app.table.delay"));
        timeColTP.setCellValueFactory(ProgramTable::timeCellValueFactory);


        TableColumn<TherapyProgram,Boolean> fileCol=new TableColumn<>(res.getString("app.table.file"));
        fileCol.setCellValueFactory(ProgramTable::fileCellValueFactory);

        fileCol.setCellFactory(col -> new FileProgramTableCell());


        numProgCol.setStyle( "-fx-alignment: CENTER;");
        timeColTP.setStyle( "-fx-alignment: CENTER;");
        nameColTP.setStyle( "-fx-alignment: CENTER-LEFT;");
        table.getColumns().addAll(numProgCol, nameColTP, descColTP, timeColTP, fileCol);
        table.placeholderProperty().setValue(new Label(res.getString("app.table.programm_placeholder")));

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        numProgCol.prefWidthProperty().bind(table.widthProperty().multiply(0.033));
        nameColTP.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        descColTP.prefWidthProperty().bind(table.widthProperty().multiply(0.557));
        timeColTP.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        fileCol.prefWidthProperty().bind(table.widthProperty().multiply(0.11));

        numProgCol.setSortable(false);
        nameColTP.setSortable(false);
        descColTP.setSortable(false);
        timeColTP.setSortable(false);
        fileCol.setSortable(false);
    }

    private static ObservableValue<Boolean> fileCellValueFactory(TableColumn.CellDataFeatures<TherapyProgram, Boolean> param) {
        SimpleBooleanProperty property = new SimpleBooleanProperty();
        property.bind(param.getValue().changedProperty().or(param.getValue().changedProperty()));
        return property;
    }

    private static ObservableValue<String> timeCellValueFactory(TableColumn.CellDataFeatures<TherapyProgram, String> param) {
        if (param.getValue().isMp3()) {

            Mp3File mp3file = null;
            try {
                mp3file = new Mp3File(param.getValue().getFrequencies());
            } catch (Exception e) {

                mp3file = null;
            }

            if (mp3file != null)
                return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds()));
            else return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(0));

        } else {

            return new SimpleStringProperty(DateUtil.convertSecondsToHMmSs(calcTherapyProgramTime(param.getValue())));
        }
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

    public TherapyProgram getSelectedItem(){
        return table.getSelectionModel().getSelectedItem();
    }
    public ObservableList<TherapyProgram> getSelectedItems(){
        return table.getSelectionModel().getSelectedItems();
    }
    public ObservableList<Integer> getSelectedIndexes(){
        return table.getSelectionModel().getSelectedIndices();
    }

    public ObservableList<TherapyProgram> getAllItems(){
        return table.getItems();
    }

    public interface NeedUpdateComplexTime{
         void update(boolean needUpdateProfileTime);
    }


    public void initProgramsTableContextMenu(Runnable copyTherapyProgramToBase,
                                              Runnable editMP3ProgramPath,
                                              Runnable cutInTables,
                                              Runnable copyInTables,
                                              Runnable pasteInTables,
                                              Runnable deleteInTables,
                                              Supplier<Boolean> therapyProgramsCopied,
                                              Supplier<Boolean> toUserBaseMenuItemPredicate) {
        MenuItem mi1=new MenuItem(res.getString("app.cut"));
        MenuItem mi6=new MenuItem(res.getString("app.ui.edit_file_path"));
        MenuItem mi7=new MenuItem(res.getString("app.ui.copy"));
        MenuItem mi2=new MenuItem(res.getString("app.paste"));
        MenuItem mi16=new MenuItem(res.getString("app.delete"));

        mi1.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        mi7.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        mi2.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        mi16.setAccelerator(KeyCombination.keyCombination("Delete"));

        MenuItem mi3=new SeparatorMenuItem();
        MenuItem mi4=new MenuItem(res.getString("app.to_user_base"));
        MenuItem mi5=new SeparatorMenuItem();
        MenuItem mi8=new MenuItem(res.getString("app.ui.multy_switch_on"));
        MenuItem mi9=new MenuItem(res.getString("app.ui.multy_switch_off"));
        MenuItem mi10=new SeparatorMenuItem();
        MenuItem mi11=new MenuItem(res.getString("app.ui.copy_program_name"));
        MenuItem mi12=new MenuItem(res.getString("app.ui.copy_program_name_main"));
        MenuItem mi13=new MenuItem(res.getString("app.ui.copy_program_freq"));
        MenuItem mi14=new MenuItem(res.getString("app.ui.invert_seletion"));
        MenuItem mi15=new SeparatorMenuItem();
        MenuItem mi17=new MenuItem(res.getString("app.copy_freq_and_name"));



        mi11.setOnAction(e->{
            TherapyProgram selectedItem = getSelectedItem();
            if(selectedItem==null) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedItem.getName());
            clipboard.setContent(content);

        });
        mi17.setOnAction(e->{
            TherapyProgram selectedItem = getSelectedItem();
            if(selectedItem==null) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedItem.getName()+" \n"+selectedItem.getFrequencies());
            clipboard.setContent(content);

        });
        mi12.setOnAction(e->{
            TherapyProgram selectedItem = getSelectedItem();
            if(selectedItem==null) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedItem.getOname());
            clipboard.setContent(content);

        });
        mi13.setOnAction(e->{
            TherapyProgram selectedItem = getSelectedItem();
            if(selectedItem==null) return;
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedItem.getFrequencies());
            clipboard.setContent(content);

        });
        mi14.setOnAction(e->{
            List<Integer> selected = getSelectedIndexes().stream().collect(Collectors.toList());
            table.getSelectionModel().selectAll();
            for (Integer ind : selected) {
                table.getSelectionModel().clearSelection(ind);
            }


        });

        mi4.setOnAction(event2 -> copyTherapyProgramToBase.run());
        mi6.setOnAction(event2 -> editMP3ProgramPath.run());

        mi1.setOnAction(e ->
        {
            cutInTables.run();

        });

        mi7.setOnAction(e->{
            copyInTables.run();
        });
        mi8.setOnAction(e-> multyFreqProgramSwitchOn());
        mi9.setOnAction(e->multyFreqProgramSwitchOff());
        mi2.setOnAction(e -> pasteInTables.run());
        mi16.setOnAction(e->deleteInTables.run());
        programMenu.getItems().addAll(mi1,
                mi7,
                mi2,
                mi16,
                mi3,
                mi8,
                mi9,
                mi15,
                mi17,
                mi11,
                mi12,
                mi13,
                mi14,
                mi10,
                mi4,
                mi6,
                translateMenu);

        table.setContextMenu(programMenu);
        programMenu.setOnShowing((event1) -> {
            if(getSelectedItem() == null) {
                mi2.setDisable(true);
                mi1.setDisable(true);
                mi3.setDisable(true);
                mi4.setDisable(true);
                mi5.setDisable(true);
                mi6.setDisable(true);
                mi7.setDisable(true);
                mi8.setDisable(true);
                mi9.setDisable(true);
                mi11.setDisable(true);
                mi12.setDisable(true);
                mi13.setDisable(true);
                mi14.setDisable(true);
                mi16.setDisable(true);
                translateMenu.setDisable(true);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if(clipboard.hasContent(PROGRAM_COPY_ITEM))if(therapyProgramsCopied.get())   mi2.setDisable(false);
                if(clipboard.hasContent(PROGRAM_CUT_ITEM_ID))   mi2.setDisable(false);
            } else {
                translateMenu.setDisable(false);
                mi16.setDisable(false);
                if(getSelectedIndexes().size()==1){
                    mi11.setDisable(false);
                    mi12.setDisable(false);
                    mi13.setDisable(false);

                }else {
                    mi11.setDisable(true);
                    mi12.setDisable(true);
                    mi13.setDisable(true);

                }
                mi14.setDisable(false);
                mi2.setDisable(true);
                mi1.setDisable(false);//всегда можно вырезать
                mi3.setDisable(true);
                mi4.setDisable(true);
                mi5.setDisable(true);
                if(getSelectedIndexes().size()==1 && getSelectedItem().isMp3())mi6.setDisable(false);
                else mi6.setDisable(true);

                mi7.setDisable(false);

                mi8.setDisable(false);
                mi9.setDisable(false);


                Clipboard clipboard = Clipboard.getSystemClipboard();
                if(clipboard.hasContent(PROGRAM_CUT_ITEM_INDEX) || clipboard.hasContent(PROGRAM_COPY_ITEM)) {


                    if (clipboard.hasContent(PROGRAM_COPY_ITEM)) {
                        if(getSelectedIndexes().size()==1) mi2.setDisable(false);
                        else mi2.setDisable(true);
                    }
                    else  if(getSelectedIndexes().size()==1) {

                        Integer[] ind = (Integer[]) clipboard.getContent(PROGRAM_CUT_ITEM_INDEX);
                        if (ind != null) {
                            if (ind.length != 0) {
                                Long idComplex = (Long) clipboard.getContent(PROGRAM_CUT_ITEM_COMPLEX);
                                if(idComplex==null)mi2.setDisable(true);
                                else if(idComplex.longValue()== ComplexTable.getInstance().getSelectedItem().getId().longValue()){
                                    //вставка в том же профиле
                                    int dropIndex = table.getSelectionModel().getSelectedIndex();
                                    if(TablesCommon.isEnablePaste(dropIndex,ind))mi2.setDisable(false);

                                }else   mi2.setDisable(false);//вставка в другом профиле, можно в любое место
                            }
                        }

                    }

                } else {
                    mi2.setDisable(true);
                    mi1.setDisable(false);
                }

                if(getSelectedItem() == null) {
                    mi4.setDisable(true);
                }

                mi4.setDisable(toUserBaseMenuItemPredicate.get());

                if(getSelectedItem().isMp3() && getSelectedItems().size()==1) {
                    mi4.setDisable(true);
                    mi6.setDisable(false);
                }
            }
        });
    }


    private void multyFreqProgramSwitchOff() {
        List<TherapyProgram> selectedItems = getAllItems().stream().collect(Collectors.toList());
        if(selectedItems.size()==0) return;
        try {

            for (TherapyProgram selectedItem : selectedItems) {
                if(selectedItem.isMultyFreq()){
                    selectedItem.setChanged(true);
                    selectedItem.setMultyFreq(false);
                    getModel().updateTherapyProgram(selectedItem);
                    int ind=table.getItems().indexOf(selectedItem);
                    table.getItems().set(ind,null);
                    table.getItems().set(ind,selectedItem);
                    // tableProgram.getSelectionModel().select(ind);
                }

            }
            needUpdateListener.update(true);
            //updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(),true);
            //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            table.getSelectionModel().clearSelection();

        } catch (Exception e) {
            logger.error("Ошибка обновления MultyFreq в терапевтической программе",e);
        }
    }

    private void multyFreqProgramSwitchOn() {
        List<TherapyProgram> selectedItems = getAllItems().stream().collect(Collectors.toList());
        if(selectedItems.size()==0) return;
        try {

            for (TherapyProgram selectedItem : selectedItems) {
                if(!selectedItem.isMultyFreq()){
                    selectedItem.setChanged(true);
                    selectedItem.setMultyFreq(true);
                    getModel().updateTherapyProgram(selectedItem);
                    int ind=table.getItems().indexOf(selectedItem);
                    table.getItems().set(ind,null);
                    table.getItems().set(ind,selectedItem);
                    table.getSelectionModel().select(ind);
                }


            }
            needUpdateListener.update(true);
            //updateComplexTime(tableComplex.getSelectionModel().getSelectedItem(),true);
            //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            table.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Ошибка обновления MultyFreq в терапевтической программе",e);
        }

    }

    private  class FileProgramTableCell extends TableCell<TherapyProgram, Boolean> {
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
                                needUpdateListener.update(false);
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
    }

}
