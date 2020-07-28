package ru.biomedis.biomedismair3.TherapyTabs.Programs;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.LeftPanelAPI;
import ru.biomedis.biomedismair3.Layouts.LeftPanel.NamedTreeItem;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexTable;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileTable;
import ru.biomedis.biomedismair3.TherapyTabs.TablesCommon;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class ProgramController extends BaseController implements ProgramAPI{

    @FXML
    private TableView<TherapyProgram> tableProgram;



    @FXML private Button btnDeleteProgram;
    @FXML private Button  btnUpProgram;
    @FXML private Button  btnDownProgram;
    @FXML private Button searchReturnBtnPrograms;
    @FXML private Button searchBtnProgram;

    @FXML private TextField nameProgramSearch;
    @FXML private TextField freqProgramSearch;
    private SimpleBooleanProperty programSearch =new SimpleBooleanProperty(false);

    private ProgramTable programTable;
    private ResourceBundle res;
    private TabPane therapyTabPane;

    private Image imageDone;
    private Image imageCancel;

    private Image imageSeq;
    private Image imageParallel;

    private LeftPanelAPI leftAPI;
    private  ProfileAPI profileAPI;
    private ComplexAPI complexAPI;

    private boolean therapyProgramsCopied=false;

    @Override
    protected void onCompletedInitialization() {

    }

    @Override
    protected void onClose(WindowEvent event) {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        leftAPI = getLeftAPI();
        profileAPI = getProfileAPI();
        complexAPI =getComplexAPI();
        initSeqParallelImages();
        initDoneCancelImages();
        initTablesButtonVisibilityPolicy();
        programTable = initProgramsTable();
        programTable.initProgramsTableContextMenu(this::copyTherapyProgramToBase,
                this::editMP3ProgramPath,
                AppController::cutInTables,
                AppController::copyInTables,
                AppController::pasteInTables,
                AppController::deleteInTables,
                AppController::pasteInTables_after,
                ()->therapyProgramsCopied,
                ()-> {
                    boolean res = true;
                    if (leftAPI.selectedSectionTree() != null) {
                        INamed value = leftAPI.selectedSectionTreeItem();
                        if (value instanceof Section) {
                            return ((Section) value).isOwnerSystem();

                        }else  if (value instanceof Complex) {
                            return ((Complex) value).isOwnerSystem();

                        }
                    }
                    return res;
                });


        tableProgram.setOnKeyReleased(e ->{
            //if(e.getCode()==KeyCode.DELETE) onRemovePrograms();
            if(e.getCode()== KeyCode.LEFT && !therapyTabPane.getTabs().get(1).isDisable()) {
                therapyTabPane.getSelectionModel().select(1);
                ComplexTable.getInstance().requestFocus();
                if(ComplexTable.getInstance().getAllItems().size()!=0){
                    ComplexTable.getInstance().setItemFocus(ComplexTable.getInstance().getSelectedIndex());
                }
            }


        });

        initProgramSearch();
    }


    private LeftPanelAPI getLeftAPI(){
        return AppController.getLeftAPI();
    }

    private ProfileAPI getProfileAPI(){return AppController.getProfileAPI();}
    private ComplexAPI getComplexAPI(){return AppController.getComplexAPI();}

    private ProgressAPI getProgressAPI(){return AppController.getProgressAPI();}

    public void setTherapyTabPane( TabPane pane){ therapyTabPane=pane;}



    private void initSeqParallelImages() {
        URL location;
        location = getClass().getResource("/images/seq_16.png");
        imageSeq=new Image(location.toExternalForm());
        location = getClass().getResource("/images/parallel_16.png");
        imageParallel=new Image(location.toExternalForm());
    }

    private void initDoneCancelImages() {
        URL location = getClass().getResource("/images/done.png");
        imageDone=new Image(location.toExternalForm());
        location = getClass().getResource("/images/cancel.png");
        imageCancel=new Image(location.toExternalForm());
    }


    private ProgramTable initProgramsTable() {
        return ProgramTable.init(tableProgram,res,imageCancel,imageDone,imageSeq,imageParallel,(needUpdateProfileTime) -> {
            complexAPI.updateComplexTime(ComplexTable.getInstance().getSelectedItem(),true);
            if(needUpdateProfileTime)profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());
        });
    }


    private void initTablesButtonVisibilityPolicy() {
        btnDeleteProgram.disableProperty().bind(tableProgram.getSelectionModel().selectedItemProperty().isNull());


        btnUpProgram.disableProperty().bind(new BooleanBinding() {
            {bind(tableProgram.getSelectionModel().selectedItemProperty());}
            @Override
            protected boolean computeValue() {
                if(tableProgram.getSelectionModel().getSelectedIndices().size()==0)return true;
                if(tableProgram.getSelectionModel().getSelectedIndices().size()>1) return true;
                if(tableProgram.getSelectionModel().getSelectedIndex()==0) return true;//верхний элемент
                return false;
            }
        });

        btnDownProgram.disableProperty().bind(new BooleanBinding() {
            {
                //заставит этот биндинг обновляться при изменении свойства selectedIndexProperty
                super.bind(tableProgram.getSelectionModel().selectedItemProperty());

            }
            @Override
            protected boolean computeValue()
            {

                if(tableProgram.getSelectionModel().getSelectedIndices().size()==0)return true;
                if(tableProgram.getSelectionModel().getSelectedIndices().size()>1) return true;

                if( tableProgram.getSelectionModel().getSelectedIndex() == tableProgram.getItems().size()-1) return true;
                return false;
            }
        });
    }
    public void onUpProgram()
    {
        int i = tableProgram.getSelectionModel().getSelectedIndex();
        TherapyProgram tp1=tableProgram.getItems().get(i - 1);
        TherapyProgram tp2=tableProgram.getItems().get(i);
        tableProgram.getItems().set(i-1,tp2);
        tableProgram.getItems().set(i, tp1);

        long pos =  tp1.getPosition();
        tp1.setPosition(tp2.getPosition());
        tp2.setPosition(pos);
        try {
            getModel().updateTherapyProgram(tp1);
            getModel().updateTherapyProgram(tp2);
        } catch (Exception e) {
            log.error("",e);
        }


        tp1=null;
        tp2=null;



    }

    public void onDownProgram()
    {
        int i = tableProgram.getSelectionModel().getSelectedIndex();
        TherapyProgram tp1=tableProgram.getItems().get(i + 1);
        TherapyProgram tp2=tableProgram.getItems().get(i);
        tableProgram.getItems().set(i+1,tp2);
        tableProgram.getItems().set(i,tp1);

        long pos =  tp1.getPosition();
        tp1.setPosition(tp2.getPosition());
        tp2.setPosition(pos);

        try {
            getModel().updateTherapyProgram(tp1);
            getModel().updateTherapyProgram(tp2);
        } catch (Exception e) {
            log.error("",e);
        }
        tp1=null;
        tp2=null;

    }

    /**
     * Удаление программи из таблиц терапевтических программ
     */
    public void onRemovePrograms()
    {

        List<TherapyProgram> selectedItems = tableProgram.getSelectionModel().getSelectedItems().stream().collect(
                Collectors.toList());
        if(selectedItems.isEmpty())return;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title66"), "", res.getString("app.title67"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
        TherapyComplex therapyComplex = ComplexTable.getInstance().getSelectedItem();
        if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
        {
            try {
                for (TherapyProgram p : selectedItems) {
                    getModel().removeTherapyProgram(p);
                    File temp=new File(getApp().getDataDir(),p.getId()+".dat");
                    if(temp.exists())temp.delete();
                    tableProgram.getItems().remove(p);

                }

                selectedItems.clear();

                complexAPI.updateComplexTime(therapyComplex, true);
                tableProgram.getSelectionModel().clearSelection();
                profileAPI.updateProfileWeight(ProfileTable.getInstance().getSelectedItem());
            } catch (Exception e) {
                log.error("",e);

                showExceptionDialog("Ошибка удаления программы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);

            }


        }

    }

    /**
     * Добавить MP3 в выбранный комплекс
     */
    public void onAddMP3()
    {

        FileChooser fileChooser =new FileChooser();

        fileChooser.setTitle(res.getString("app.ui.program_by_mp3"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
        fileChooser.setInitialDirectory(new File(getModel().getMp3Path(System.getProperty("user.home"))));

        List<File> files = fileChooser.showOpenMultipleDialog(getApp().getMainWindow());



        if(files==null)return;
        if(files.size()==0) return;
        getModel().setMp3Path(files.get(0).getParentFile());
        TherapyComplex complex = ComplexTable.getInstance().getSelectedItem();
        if(complex==null) return;

        try {

            List<TherapyProgram> tpl=new ArrayList<>();
            for (File mp3 : files)
            {
                tpl.add(getModel().createTherapyProgramMp3(complex, TextUtil.digitText(mp3.getName().substring(0,mp3.getName().length()-4)), "", mp3.getAbsolutePath())) ;

            }



            if(tpl.isEmpty()) return;
            tableProgram.getItems().addAll(tpl);
            int i = tableProgram.getItems().size()-1;
            tableProgram.scrollTo(i);

            complexAPI.updateComplexTime(ComplexTable.getInstance().getSelectedItem(), true);
           // ProfileTable.getInstance().getSelectedItem().setProfileWeight( ProfileTable.getInstance().getSelectedItem().getProfileWeight()+1);
            profileAPI.updateProfileWeight(ProfileTable.getInstance().getSelectedItem());




        } catch (Exception e) {
            log.error("",e);
            showExceptionDialog("Ошибка создания программы", "","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }


    private void cleanSearchedProgram(){
        TherapyProgram tp;
        for(int i=0;i< tableProgram.getItems().size();i++) {
            tp = tableProgram.getItems().get(i);
            if(tp.isMatchedFreqs() || tp.isMatchedAnyName()){
                tp.cleanSearch();
                tableProgram.getItems().set(i,null);
                tableProgram.getItems().set(i,tp);
            }
        }
    }
    private void initProgramSearch(){

        searchReturnBtnPrograms.disableProperty().bind(programSearch.not());
        searchBtnProgram.disableProperty().bind(nameProgramSearch.textProperty().isEmpty().and(freqProgramSearch.textProperty().isEmpty()));
        programSearch.bind(nameProgramSearch.textProperty().isNotEmpty().or(freqProgramSearch.textProperty().isNotEmpty()));

        programSearch.addListener((observable, oldValue, newValue) -> {
            if(newValue==false)cleanSearchedProgram();
        });

        Predicate<String> characterFilter= c-> TextUtil.match(c,"[0-9. ]");


        freqProgramSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            //3.6 + 4 .9;7;.0;46.0;70.0  -;70.5;;;++72.5;95.0
            String val=
                    newValue.replaceAll("[^0-9\\.;\\+ ]","")
                            .replace(";"," ").replace("+"," ")
                            .replaceAll("\\s+"," ")
                            .replaceAll("\\s+\\.",".");
            if(!val.equals(newValue)) freqProgramSearch.setText(val);

        });
        freqProgramSearch.addEventFilter(KeyEvent.KEY_TYPED, event ->
        {
            if(!characterFilter.test( event.getCharacter())) {event.consume();}
        });
        nameProgramSearch.setOnAction(event ->onSearchProgram());
        freqProgramSearch.setOnAction(event ->onSearchProgram());
    }


    /**
     * Сброс режима поиска
     * programSearch зависит от содержимого полей поиска
     */
    public void onSearchReturnPrograms(){
        nameProgramSearch.setText("");
        freqProgramSearch.setText("");
        tableProgram.getSelectionModel().clearSelection();
        tableProgram.scrollTo(0);
    }

    public void onSearchProgram(){

        String name = nameProgramSearch.getText().trim();
        String freqs = freqProgramSearch.getText().trim();
        if(name.length()<=2 && freqs.length()==0) { showInfoDialog(res.getString("app.search"),res.getString("app.search_1"),"",getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}

        tableProgram.getSelectionModel().clearSelection();

        freqs=freqs.replaceAll("\\s+"," ").replaceAll("\\s+\\.",".");//исключим посторонние дырки в паттерне
        freqProgramSearch.setText(freqs);

        cleanSearchedProgram();

        boolean first=true;

        TherapyProgram tp;
        for(int i=0;i< tableProgram.getItems().size();i++){
            tp = tableProgram.getItems().get(i);
            boolean searchRes=false;
            if(!name.isEmpty() && freqs.isEmpty())searchRes = tp.searchNames(name);
            else if(!name.isEmpty() && !freqs.isEmpty()) {

                searchRes = tp.searchFreqs(freqs) & tp.searchNames(name);
                if(!searchRes){
                    if(tp.isMatchedAnyName())tp.cleanSearch();
                    else if(tp.isMatchedFreqs())tp.cleanSearch();
                }

            }
            else if(name.isEmpty() && !freqs.isEmpty()) searchRes = tp.searchFreqs(freqs);

            if(searchRes){
                tableProgram.getItems().set(i,null);
                tableProgram.getItems().set(i,tp);

                if(first){
                    tableProgram.scrollTo(i);
                    first=false;
                }
            }

        }




        if(!first){

            for(int i=0;i< tableProgram.getItems().size();i++) {
                tp = tableProgram.getItems().get(i);

                boolean searchRes=false;

                if(!name.isEmpty() && freqs.isEmpty())  searchRes = tp.isMatchedAnyName();
                else if(!name.isEmpty() && !freqs.isEmpty())searchRes = tp.isMatchedFreqs() && tp.isMatchedAnyName();
                else if(name.isEmpty() && !freqs.isEmpty())searchRes = tp.isMatchedFreqs();

                if(searchRes)tableProgram.getSelectionModel().select(i);
            }

            tableProgram.requestFocus();
        }else  {
            showInfoDialog(res.getString("app.search_res"),res.getString("app.search_res_1"),"",
                    getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;

        }
    }


    private void editMP3ProgramPath()
    {

        TherapyProgram item = tableProgram.getSelectionModel().getSelectedItem();
        if(item==null) return;
        if(!item.isMp3()) return;

        File tf=new File(item.getFrequencies());


        FileChooser fileChooser =new FileChooser();

        fileChooser.setTitle(res.getString("app.ui.program_by_mp3"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));

        //если такой файл уже есть то установим путь относительно него
        if( tf.exists()) fileChooser.setInitialDirectory(tf.getParentFile());
        else fileChooser.setInitialDirectory( new File(getModel().getMp3Path(System.getProperty("user.home"))));
        File mp3 = fileChooser.showOpenDialog(getApp().getMainWindow());

        if(mp3==null)return;

        item.setName(TextUtil.digitText(mp3.getName().substring(0,mp3.getName().length()-4)));
        item.setFrequencies(mp3.getAbsolutePath());
        int i = tableProgram.getItems().indexOf(item);
        item.setChanged(false);

        getModel().setMp3Path(mp3.getParentFile());
        try
        {

            getModel().updateTherapyProgram(item);
            profileAPI.updateProfileWeight(ProfileTable.getInstance().getSelectedItem());
        } catch (Exception e)
        {
            log.error("Ошибка обновления TherapyProgram",e);
            return;
        }

        tableProgram.getItems().remove(i);
        tableProgram.getItems().add(i,item);
        complexAPI.updateComplexTime(ComplexTable.getInstance().getSelectedItem(),true);
        tableProgram.getSelectionModel().select(i);



    }

    /**
     * Копирование текущей программы в пользовательскую базу
     */
    private void copyTherapyProgramToBase()
    {

        ObservableList<TherapyProgram> selectedItems = tableProgram.getSelectionModel().getSelectedItems();
        NamedTreeItem treeItem = leftAPI.selectedSectionTree();
        if(selectedItems.isEmpty()|| treeItem==null) return;

        List<TherapyProgram> src=selectedItems.stream().filter(i->!i.isMp3()).collect(Collectors.toList());


        List<Program> tpl=new ArrayList<>();
        try {

            if(treeItem.getValue() instanceof Section) {
                for (TherapyProgram therapyProgram : src) {
                    Program p = getModel().createProgram(therapyProgram.getName(), therapyProgram.getDescription(), therapyProgram.getFrequencies(),(Section)treeItem.getValue(), false, getModel().getUserLanguage());
                    tpl.add(p);
                }


            }
            else if(treeItem.getValue() instanceof Complex){
                for (TherapyProgram therapyProgram : src){

                    Program p = getModel().createProgram(therapyProgram.getName(), therapyProgram.getDescription(), therapyProgram.getFrequencies(),(Complex)treeItem.getValue(), false, getModel().getUserLanguage());
                    tpl.add(p);
                }
            }
            else throw new Exception();


            if (!treeItem.isLeaf())  {
                getModel().initStringsProgram(tpl);
                treeItem.getChildren().addAll(tpl.stream().map(NamedTreeItem::new).collect(Collectors.toList()));
            }


            boolean isleaf=treeItem.isLeaf();
            if (treeItem.isLeaf())treeItem.setLeafNode(false);
            if (!treeItem.isExpanded()) treeItem.setExpanded(true);
            //s  if (treeItem != null) sectionTree.getSelectionModel().select(treeItem.getChildren().get(treeItem.getChildren().size() - 1));//выделим

        } catch (Exception e) {
            log.error("",e);
            showExceptionDialog("Ошибка переноса программы в базу","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
    }

    @Override
    public void removePrograms() {
        onRemovePrograms();
    }

    @Override
    public void pasteTherapyPrograms(){
        if(ProgramTable.getInstance().getSelectedItems().size()>1){
            showWarningDialog(res.getString("app.ui.insertion_elements"),res.getString("app.ui.insertion_not_allowed"),res.getString("app.ui.ins_not_av_mess"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }
        if(therapyProgramsCopied)pasteTherapyProgramsByCopy();
        else pasteTherapyProgramsByCut();
    }

    @Override
    public void pasteTherapyPrograms_after() {
        ProgramTable.getInstance().clearSelection();
        pasteTherapyPrograms();
    }

    private void pasteTherapyProgramsByCopy(){
        TherapyComplex therapyComplex = ComplexTable.getInstance().getSelectedItem();
        if(therapyComplex==null) return;
        if (ProgramTable.getInstance().getSelectedItems().size()>1) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ProgramTable.PROGRAM_COPY_ITEM)) return;
        Long[] ids = (Long[]) clipboard.getContent(ProgramTable.PROGRAM_COPY_ITEM);
        if(ids==null) return;
        if(ids.length==0) return;

        int dropIndex = ProgramTable.getInstance().getSelectedIndex();

        List<TherapyProgram> therapyPrograms =  Arrays.stream(ids)
                                                      .map(i->getModel().getTherapyProgram(i))
                                                      .filter(i->i!=null)
                                                      .collect(Collectors.toList());


        if (ProgramTable.getInstance().getSelectedItems().isEmpty()) {
            //вставка просто в конец таблицы
            try {
                ProgramTable.getInstance().clearSelection();
                for (TherapyProgram therapyProgram : therapyPrograms) {

                    TherapyProgram tp = getModel().copyTherapyProgramToComplex(therapyComplex, therapyProgram);
                    if(tp==null) continue;
                    ProgramTable.getInstance().getAllItems().add(tp);
                    ProgramTable.getInstance().select(tp);

                }
                complexAPI.updateComplexTime(therapyComplex,true);
                //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            } catch (Exception e1) {
                log.error("",e1);
                showExceptionDialog("Ошибка копирования программ","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                therapyProgramsCopied=false;
                therapyPrograms.clear();
                clipboard.clear();
                return;
            }
        }else {
            //вставка до выбранного элемента со сдвигом остальных
            List<TherapyProgram> tpl=new ArrayList<>();
            try {
                ProgramTable.getInstance().clearSelection();
                for (TherapyProgram therapyProgram : therapyPrograms) {
                    TherapyProgram tp = getModel().copyTherapyProgramToComplex(therapyComplex, therapyProgram);
                    if(tp==null) continue;
                    tpl.add(tp);
                }
                List<TherapyProgram> tpSlided =  ProgramTable.getInstance().getAllItems()
                                                             .subList(dropIndex,  ProgramTable.getInstance().getAllItems().size());
                long posFirstSlidingElem =  ProgramTable.getInstance().getAllItems().get(dropIndex).getPosition();

                for (TherapyProgram tp : tpSlided) {
                    tp.setPosition(tp.getPosition()+tpl.size());
                    getModel().updateTherapyProgram(tp);
                }
                int cnt=0;
                for (TherapyProgram tp : tpl) {
                    tp.setPosition(posFirstSlidingElem + cnt++);
                    getModel().updateTherapyProgram(tp);
                }

                ProgramTable.getInstance().getAllItems().addAll(dropIndex,tpl);
                for (TherapyProgram tp : tpl) {
                    ProgramTable.getInstance().select(tp);
                }


                complexAPI.updateComplexTime(therapyComplex,true);
                //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            } catch (Exception e1) {
                log.error("",e1);
                showExceptionDialog("Ошибка копирования программ","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                therapyProgramsCopied=false;
                therapyPrograms.clear();
                clipboard.clear();
                return;
            }

        }
        //чтобы инициировать проверку видимости кнопкок вверх и вниз, если при вставке не изменился выделенный элемент
        if(tableProgram.getSelectionModel().getSelectedIndices().size()==1){
            int ind = tableProgram.getSelectionModel().getSelectedIndex();
            tableProgram.getSelectionModel().clearSelection();
            tableProgram.getSelectionModel().select(ind);
        }
        therapyProgramsCopied=false;
        therapyPrograms.clear();
        clipboard.clear();
    }
    /**
     * Перемещение терапквтических программ через буффер обмена
     */
    private void pasteTherapyProgramsByCut() {
        //в выбранный индекс вставляется новый элемент а все сдвигаются на 1 индекс  вырезанного индекса

        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasContent(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX)) return;
        if (!clipboard.hasContent(ProgramTable.PROGRAM_CUT_ITEM_ID)) return;
        if (!clipboard.hasContent(ProgramTable.PROGRAM_CUT_ITEM_INDEX)) return;
        try {
            TherapyComplex selectedComplex=ComplexTable.getInstance().getSelectedItem();
            Long idComplex = (Long) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX);
            if(idComplex==null)return;
            else if(idComplex.longValue()==selectedComplex.getId().longValue()){
                //вставка в текущем комплексе
                //if ( ProgramTable.getInstance().getSelectedItems().isEmpty()) return;
                if ( !ProgramTable.getInstance().getSelectedItems().isEmpty())if ( ProgramTable.getInstance().getSelectedItems().size()!=1) return;

                Integer[] indexes = (Integer[]) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_INDEX);
                if(indexes==null) return;
                if(indexes.length==0) return;
                List<Integer> ind = Arrays.stream(indexes).collect(Collectors.toList());

                int dropIndex = 0;
                if(ProgramTable.getInstance().getSelectedItems().size() ==  0){
                    dropIndex = ProgramTable.getInstance().getAllItems().size()-1;
                    if(dropIndex < 0) dropIndex = 0;
                }
                else dropIndex = ProgramTable.getInstance().getSelectedIndex();

                if(!TablesCommon.isEnablePaste(dropIndex,indexes)) {
                    showWarningDialog(res.getString("app.ui.moving_items"),"",res.getString("app.ui.can_not_move_to_pos"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                List<TherapyProgram> therapyPrograms = ind.stream().map(i-> ProgramTable.getInstance().getAllItems().get(i)).collect(Collectors.toList());
                int startIndex=ind.get(0);//первый индекс вырезки
                int lastIndex=ind.get(ind.size()-1);

//элементы всегда будут оказываться выше чем индекс по которому вставляли, те визуально вставляются над выбираемым элементом
                TherapyProgram dropProgram =  ProgramTable.getInstance().getAllItems().get(dropIndex);

                if(ProgramTable.getInstance().getSelectedItems().size() == 0){

                    for (TherapyProgram i : therapyPrograms) {
                        ProgramTable.getInstance().getAllItems().remove(i);
                    }
                    ProgramTable.getInstance().getAllItems().addAll(therapyPrograms);
                }
                else
                if(dropIndex < startIndex){

                    for (TherapyProgram i : therapyPrograms) {
                        ProgramTable.getInstance().getAllItems().remove(i);
                    }
                    //вставка программ в dropIndex; Изменение их позиции
                    ProgramTable.getInstance().getAllItems().addAll(dropIndex,therapyPrograms);
                }else if(dropIndex > lastIndex){


                    for (TherapyProgram i : therapyPrograms) {
                        ProgramTable.getInstance().getAllItems().remove(i);
                    }
                    dropIndex=  ProgramTable.getInstance().getAllItems().indexOf(dropProgram);
                    ProgramTable.getInstance().getAllItems().addAll(dropIndex,therapyPrograms);


                }else return;

                int i=0;
                for (TherapyProgram tp :  ProgramTable.getInstance().getAllItems()) {
                    tp.setPosition((long)(i++));
                    getModel().updateTherapyProgram(tp);
                }

                complexAPI.updateComplexTime(dropProgram.getTherapyComplex(),false);
                profileAPI.updateProfileTime(ProfileTable.getInstance().getSelectedItem());
                therapyPrograms.clear();

            }else {
                //вставка в другом комплексе. Нужно вырезать и просто вставить в указанном месте



                Long[] ids = (Long[]) clipboard.getContent(ProgramTable.PROGRAM_CUT_ITEM_ID);

                if(ids==null) return;
                if(ids.length==0) return;
                List<Long> ind = Arrays.stream(ids).collect(Collectors.toList());
                int dropIndex =-1;
                if ( ProgramTable.getInstance().getSelectedItem()!=null)dropIndex = ProgramTable.getInstance().getSelectedIndex();
                else if(ProgramTable.getInstance().getAllItems().size()==0) dropIndex=0;
                else if(ProgramTable.getInstance().getSelectedItems().size() ==0) dropIndex = -1;

                List<TherapyProgram> movedTP = ind.stream()
                                                  .map(i->getModel().getTherapyProgram(i))
                                                  .filter(i->i!=null)
                                                  .collect(Collectors.toList());
                TherapyComplex srcComplex=null;
                if(movedTP.size() > 0){
                    Optional<TherapyComplex> first = ComplexTable.getInstance().getAllItems().stream().filter(p -> p.getId().longValue() == idComplex.longValue()).findFirst();
                    //будет найден только если вставка в том же профиле
                    srcComplex=first.orElse(null);
                }
                //просто вставляем
                if(dropIndex==-1) ProgramTable.getInstance().getAllItems().addAll(movedTP);
                else   ProgramTable.getInstance().getAllItems().addAll(dropIndex,movedTP);
                //теперь все обновляем
                int i=0;
                for (TherapyProgram tp :  ProgramTable.getInstance().getAllItems()) {
                    tp.setPosition((long)(i++));
                    tp.setTherapyComplex(selectedComplex);
                    getModel().updateTherapyProgram(tp);
                }

                //обновление времени в таблицах
                if(movedTP.size()>0){
                    complexAPI.updateComplexTime(selectedComplex,true);
                    //если вставка в другом профиле то обновлять не надо
                    if(srcComplex!=null) complexAPI.updateComplexTime(srcComplex,true);
                    //updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
                }

                //idComplex
                movedTP.clear();
            }

        } catch (Exception e1) {
            log.error("", e1);
            showExceptionDialog("Ошибка обновления позиции программ","","",e1,getApp().getMainWindow(), Modality.WINDOW_MODAL);

            return;
        }finally {
            //чтобы инициировать проверку видимости кнопкок вверх и вниз, если при вставке не изменился выделенный элемент
            if(tableProgram.getSelectionModel().getSelectedIndices().size()==1){
               int ind = tableProgram.getSelectionModel().getSelectedIndex();
                tableProgram.getSelectionModel().clearSelection();
                tableProgram.getSelectionModel().select(ind);
            }
            clipboard.clear();
        }
    }
    @Override
    public void copySelectedTherapyProgramsToBuffer() {
        if (ProgramTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ProgramTable.PROGRAM_COPY_ITEM, ProgramTable.getInstance().getSelectedItems().stream()
                                                                .map(i->i.getId()).collect(Collectors.toList()).toArray(new Long[0]));
        clipboard.setContent(content);
        therapyProgramsCopied=true;
    }


    @Override
    public void cutSelectedTherapyProgramsToBuffer() {
        if (ProgramTable.getInstance().getSelectedItems().isEmpty()) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.clear();

        content.put(ProgramTable.PROGRAM_CUT_ITEM_INDEX, ProgramTable.getInstance().getSelectedIndexes().toArray(new Integer[0]));
        content.put(ProgramTable.PROGRAM_CUT_ITEM_COMPLEX, ComplexTable.getInstance().getSelectedItem().getId());
        content.put(ProgramTable.PROGRAM_CUT_ITEM_ID, ProgramTable.getInstance().getSelectedItems().stream()
                                                                  .map(i->i.getId())
                                                                  .collect(Collectors.toList())
                                                                  .toArray(new Long[0])
        );
        clipboard.setContent(content);
        therapyProgramsCopied=false;
    }

}
