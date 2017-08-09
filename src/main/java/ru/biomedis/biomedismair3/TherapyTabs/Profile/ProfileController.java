package ru.biomedis.biomedismair3.TherapyTabs.Profile;

import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import ru.biomedis.biomedismair3.*;
import ru.biomedis.biomedismair3.Dialogs.SearchProfile;
import ru.biomedis.biomedismair3.Dialogs.TextInputValidationController;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexTable;
import ru.biomedis.biomedismair3.TherapyTabs.Programs.ProgramTable;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.m2.*;
import ru.biomedis.biomedismair3.utils.Audio.MP3Encoder;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;
import ru.biomedis.biomedismair3.utils.Files.FilesProfileHelper;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

public class ProfileController extends BaseController implements ProfileAPI {
    private static final int MAX_BUNDLES = AppController.MAX_BUNDLES;
    @FXML private Button btnGenerate;
    @FXML private Button btnUploadm;//закачать на прибор
    @FXML private Button btnDeleteProfile;
    @FXML private TableView<Profile> tableProfile;
    private ProfileTable profileTable;
    private ResourceBundle res;
    private ContextMenu uploadMenu=new ContextMenu();
    private SimpleBooleanProperty checkUppload=new SimpleBooleanProperty(false);
    private TabPane therapyTabPane;
    private SimpleBooleanProperty m2Ready;
    private SimpleBooleanProperty m2Connected;
    private SimpleBooleanProperty connectedDevice;

    @Override
    protected void onCompletedInitialise() {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        btnGenerate.setDisable(true);
        profileTable = initProfileTable();
        profileTable.initProfileContextMenu( this::onPrintProfile,
                this::cutProfileToBuffer,
                this::pasteProfile,
                this::removeProfile);

        btnDeleteProfile.disableProperty().bind(tableProfile.getSelectionModel().selectedItemProperty().isNull());

        initDoubleClickSwitchTable();
        initHotKeyProfileTab();

    }

    private void initHotKeyProfileTab() {
        tableProfile.setOnKeyReleased(event ->
        {
            if(event.getCode()== KeyCode.DELETE) removeProfile();
            else
            if(event.getCode()==KeyCode.RIGHT && !therapyTabPane.getTabs().get(1).isDisable()){
                if(ProfileTable.getInstance().isTextEdited()) return;
                therapyTabPane.getSelectionModel().select(1);
                ComplexTable.getInstance().requestFocus();
                if(ComplexTable.getInstance().getAllItems().size()!=0){
                    ComplexTable.getInstance().setItemFocus(ComplexTable.getInstance().getSelectedIndex());

                }
            }
        });
    }

    private void initDoubleClickSwitchTable() {
        tableProfile.setOnMouseClicked(event -> {
            if(event.getClickCount()==2) {
                event.consume();
                //int selectedIndex = tableProfile.getSelectionModel().getSelectedIndex();
                // tableProfile.getSelectionModel().clearSelection();
                //tableProfile.getSelectionModel().select(selectedIndex);
                if(ProfileTable.getInstance().getSelectedItem()!=null) therapyTabPane.getSelectionModel().select(1);

            }
        });
    }

    private  SimpleBooleanProperty connectedDeviceProperty() {
        return connectedDevice;
    }
    private boolean getConnectedDevice() {
        return connectedDevice.get();
    }

    private Supplier<Path> devicePathFunc;
    public void setDevicePathMethod(Supplier<Path> f)
    {
        devicePathFunc =f;
    }

   public void setTherapyTabPane( TabPane pane){ therapyTabPane=pane;}

   public void setDevicesProperties(SimpleBooleanProperty m2Ready, SimpleBooleanProperty connectedDeviceProperty, SimpleBooleanProperty m2Connected){
       if( this.m2Ready!=null) return;
       this.connectedDevice =connectedDeviceProperty;
       this.m2Connected=m2Connected;
       this.m2Ready=m2Ready;

       initUploadMenuBtn();//инициализация зависит от наличия свойств устанавливаемых
   }

    private Path getDevicePath(){return devicePathFunc.get();}


    /**
     * Проверяет максимальное значение пачек частот в комплексах, если превышено ставит максимальное
     * @param complexList
     * @throws Exception
     */
    public static void checkBundlesLength(List<TherapyComplex> complexList) throws Exception {

        List<TherapyComplex> edited = complexList.stream().filter(c -> c.getBundlesLength() > MAX_BUNDLES).collect(Collectors.toList());

        for (TherapyComplex tc : edited) {
            tc.setBundlesLength(MAX_BUNDLES);
            App.getStaticModel().updateTherapyComplex(tc);

        }


    }
    private ProfileTable initProfileTable() {
        return ProfileTable.init(tableProfile, res);
    }

    private void initUploadMenuBtn() {

        MenuItem btnUploadDir=new MenuItem(res.getString("app.upload_to_dir"));
        btnUploadDir.setDisable(true);
        btnUploadDir.setOnAction(event -> uploadInDir());
        initUploadToDirDisabledPolicy(btnUploadDir);

        MenuItem btnUploadM2=new MenuItem(res.getString("app.ui.record_on_trinity"));
        btnUploadM2.setOnAction(event ->  uploadM2(ProfileTable.getInstance().getSelectedItem()));
        btnUploadM2.disableProperty().bind(m2Ready.and(ProfileTable.getInstance().getSelectedItemProperty().isNotNull()).not());

        MenuItem btnUpload=new MenuItem(res.getString("app.uppload"));
        btnUpload.setDisable(true);
        btnUpload.setOnAction(event -> onUploadProfile());
        initButtonUploadDisabledPolicy(btnUpload);

        uploadMenu.getItems().addAll(btnUploadDir,btnUpload, btnUploadM2);
        btnUploadm.setOnAction(event4 ->
        {

            if(!uploadMenu.isShowing()) uploadMenu.show(btnUploadm, Side.BOTTOM, 0, 0);
            else uploadMenu.hide();

        });



    }

    private void uploadM2(Profile profile) {

        //проверка установленных пачек частот и если есть отличные от 3, то нужно указать, на это
        long  cnt=getModel().findAllTherapyComplexByProfile(profile).stream().filter(c->c.getBundlesLength()!= M2Complex.BUNDLES_LENGTH).count();
        if(cnt!=0){
            showWarningDialog(
                    res.getString("app.ui.record_on_trinity"),
                    res.getString("app.ui.attention"),
                    res.getString("app.m2.message"),
                    getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }

        Platform.runLater(() ->   {
            m2Connected.set(false);
            m2Ready.setValue(false);
        });

        Task task = new Task() {
            protected Boolean call() {
                boolean res1=false;
                M2BinaryFile m2BinaryFile=null;
                try {
                    System.out.println("Запись на прибор");

                    m2BinaryFile = M2.uploadProfile(profile,true);

                    try {
                        Thread.sleep(1000);//таймаут дает время прибору подумать после записи
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // M2BinaryFile m2BinaryFile = M2.readFromDevice(true);
                    M2BinaryFile bf=m2BinaryFile;
                    Platform.runLater(() ->AppController.getM2UI().setContent(bf));


                    res1=true;
                } catch (M2Complex.MaxTimeByFreqBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),
                            res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Complex.MaxPauseBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Program.ZeroValueFreqException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("zerofreqval"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Program.MaxProgramIDValueBoundException e) {
                    Platform.runLater(() ->  showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Program.MinFrequenciesBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("mustbefreqs"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Complex.MaxCountProgramBoundException e) {
                    Platform.runLater(() ->  showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("moreprograms"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2BinaryFile.MaxBytesBoundException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("trinity.maxboundsize"),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2Complex.ZeroCountProgramBoundException e) {
                    Platform.runLater(() -> showErrorDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("app.ui.must_have_program"), getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (LanguageDevice.NoLangDeviceSupported e) {
                    Platform.runLater(() -> showErrorDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),res.getString("app.ui.lang_not_support"), getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } catch (M2.WriteToDeviceException e) {
                    Platform.runLater(() -> showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));
                } /*catch (M2.ReadFromDeviceException e) {
                    Platform.runLater(() -> {
                        showExceptionDialog(res.getString("app.ui.reading_device"),res.getString("app.error"),"",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    });
                }*/catch (Exception e){
                    Platform.runLater(() ->  showExceptionDialog( res.getString("app.ui.record_on_trinity"),res.getString("app.error"),e.getMessage(),e, getApp().getMainWindow(),Modality.WINDOW_MODAL));

                }finally {
                    Platform.runLater(() ->   {
                        m2Connected.set(true);
                        m2Ready.setValue(true);
                    });
                }
                return res1;
            }
        };

        task.setOnScheduled((event) -> {
            Waiter.openLayer(getApp().getMainWindow(), true);
        });
        task.setOnFailed(ev -> {
            Waiter.closeLayer();
            Platform.runLater(() ->  showErrorDialog( res.getString("app.ui.record_on_trinity"),"" , res.getString("app.error"), getApp().getMainWindow(),Modality.WINDOW_MODAL));

        });
        task.setOnSucceeded(ev -> {
            Waiter.closeLayer();
            if(((Boolean)task.getValue()).booleanValue()) {
                showInfoDialog(res.getString("app.ui.uploadM2"), "",res.getString("app.ui.upload_ok"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();



    }

    private void initUploadToDirDisabledPolicy(MenuItem btnUploadDir) {
        btnUploadDir.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(tableProfile.getSelectionModel().selectedItemProperty(),checkUppload, btnGenerate.disabledProperty());//подключение устройств или выключение вызывает проверку computeValue() также если появиться необходимость генерации или мы переключаем профиль(если вдруг выбор пустой стал)
            }

            @Override
            protected boolean computeValue() {

                boolean res=false;
                if(btnGenerate.isDisable())  if(tableProfile.getSelectionModel().getSelectedItem()!=null)res = !getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem());


                //если устройство подключено, выбран профиль и не активна кнопка генерации, то можно совершить загрузку в устройство
                return !(res && tableProfile.getSelectionModel().getSelectedItem() != null);

            }
        });
    }

    private void initButtonUploadDisabledPolicy(MenuItem btnUpload) {
        btnUpload.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(tableProfile.getSelectionModel().selectedItemProperty(), connectedDeviceProperty(),checkUppload, btnGenerate.disabledProperty());//подключение устройств или выключение вызывает проверку computeValue() также если появиться необходимость генерации или мы переключаем профиль(если вдруг выбор пустой стал)
            }

            @Override
            protected boolean computeValue() {

                boolean res=false;
                if(btnGenerate.isDisable())  if(tableProfile.getSelectionModel().getSelectedItem()!=null)res = !getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem());




                //если устройство подключено, выбран профиль и не активна кнопка генерации, то можно совершить загрузку в устройство
                return !(res && tableProfile.getSelectionModel().getSelectedItem() != null && getConnectedDevice());

            }
        });
    }




    /**
     * Поиск по профилям
     */
    public void onSearchProfile()
    {
        //возврат ID выбранного профиля
        SearchProfile.Data res=null;
        try {
            res=  BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/Search_profile.fxml", this.res.getString("search_in_profile"), false, StageStyle.UTILITY, 0, 0, 0, 0, new SearchProfile.Data(0));
        } catch (IOException e) {
            logger.error("",e);
        }

        if(res!=null)
        {

            if(res.isChanged())
            {
                long resf=res.getNewVal();
                List<Profile> collect = tableProfile.getItems().stream().filter(profile -> profile.getId().longValue() == resf).collect(
                        Collectors.toList());
                if(!collect.isEmpty())
                {

                    int i = tableProfile.getItems().indexOf(collect.get(0));
                    Platform.runLater(() -> {
                        therapyTabPane.getSelectionModel().select(0);//выберем таб с профилями
                        tableProfile.requestFocus();
                        tableProfile.getSelectionModel().select(i);
                        tableProfile.getFocusModel().focus(i);
                        tableProfile.scrollTo(i);
                    });
                }
            }
        }

    }

    public void onCreateProfile()
    {
        TextInputValidationController.Data td = new TextInputValidationController.Data("", s -> !BaseController.muchSpecials(s),true);
        try {
            openDialogUserData(getApp().getMainWindow(), "/fxml/TextInputValidationDialogCreate.fxml", res.getString("app.title63"), false, StageStyle.DECORATED, 0, 0, 0, 0, td);

            if (td.isChanged())
            {
                Profile profile = getModel().createProfile(td.getNewVal());

                tableProfile.getItems().add(profile);

                int i = tableProfile.getItems().indexOf(profile);
                tableProfile.requestFocus();
                tableProfile.getSelectionModel().select(i);
                tableProfile.scrollTo(i);
                tableProfile.getFocusModel().focus(i);

            }




            td = null;
        } catch (Exception e) {
            td = null;
            logger.error("",e);
            showExceptionDialog("Ошибка  создания профиля", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }



    }



    public void  onGenerate()
    {
        if(m2Ready.get()){
            showWarningDialog(res.getString("app.ui.attention"),"",res.getString("trinity.warn"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
        }
        if(tableProfile.getSelectionModel().getSelectedItem()==null){btnGenerate.setDisable(true);return;}
        if(!getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem())) {btnGenerate.setDisable(true);return;}

        try {

            //в начале мы снимим пометки с комплексов и пометим все программы требующие изменений. Далее кодек будет уже брать программы и по мере кодирования
            //снимать с них пометки. В конце просто обновиться список программ в таблице

            for (TherapyProgram therapyProgram : getModel().findNeedGenerateList((tableProfile.getSelectionModel().getSelectedItem())) )
            {
                if(therapyProgram.isMp3()) continue;

                therapyProgram.setChanged(true);//установим для всех программ(особенно тех чей комплекс помечен изменением) статус изменения
                getModel().updateTherapyProgram(therapyProgram);
            }



            //в том комплексе что выбран в в таблице программ пометим все как имзмененные. Чтобы не исчезла иконка
            if(ComplexTable.getInstance().getSelectedItem()!=null)
            {

               ProgramTable.getInstance().getAllItems().forEach(itm->{
                    if(ComplexTable.getInstance().getSelectedItem().isChanged())
                    {
                        if(!itm.isMp3())
                        {
                            itm.setChanged(true);
                            itm.getTherapyComplex().setChanged(true);
                        }
                    }


                });


            }
            //снимим пометки с комплексов об изменеии. Теперь важны лишь программы

            //тк генерить можно только выбрав профиль, то все комплексы в таблице можно просматривать
            for (TherapyComplex therapyComplex : ComplexTable.getInstance().getAllItems())
            {
                if(therapyComplex.isChanged())
                {
                    therapyComplex.setChanged(false);
                    getModel().updateTherapyComplex(therapyComplex);
                }

            }






            btnGenerate.setDisable(true);
        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog(res.getString("app.title82"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }




        try
        {

            MP3Encoder encoder=new MP3Encoder(tableProfile.getSelectionModel().getSelectedItem(), MP3Encoder.CODEC_TYPE.EXTERNAL_CODEC,44100);
            Thread thread =new Thread(encoder);



            CalcLayer.openLayer(() -> Platform.runLater(() ->
            {
                //отмена генерации. Что успело нагенериться то успело
                //скрытие произойдет по окончании работы  ниже
                CalcLayer.setInfoLayer(res.getString("app.title76"));
                encoder.stopEncode();

            }), getApp().getMainWindow(),false);






            // обработка окончания кодирования каждой програмы. Может быть по отмене! isCanceled. Само окннчание по отмене мы специально не ловим. Просто убираем ненужное с экрана
            encoder.setActionListener((id, isCanceled) -> {
                //здесь изменим параметры в базе. Если была отмена, то не будем менять для этой програмы
                if(!isCanceled)
                {
                    //просто перезапишим Changed поле. По окончаюю всей обработки,обновим таблицу програм, если она активна и все. Комплексы уже подправлены ранее
                    TherapyProgram tp = getModel().getTherapyProgram(id);
                    tp.setChanged(false);
                    tp.setUuid(UUID.randomUUID().toString());//при загрузки файлов это читается из базы а не таблицы. можно не беспокоится об обновлении бинов таблицы

                    try {
                        getModel().updateTherapyProgram(tp);
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                    tp=null;
                }


            });





            encoder.setOnScheduled(event1 -> getProgressAPI().setProgressBar(0.0,res.getString("app.title83"),res.getString("app.title84")));

            encoder.setOnFailed(event ->
                    Platform.runLater(() ->
                    {
                        CalcLayer.closeLayer();
                        getProgressAPI().setProgressBar(100.0, res.getString("app.error"), res.getString("app.title84"));
                        getProgressAPI().hideProgressBar(true);

                        encoder.removeActionListener();

                        //обновим таблицу программ если у нас выбран комлекс
                        if(ComplexTable.getInstance().getSelectedItem()!=null)
                        {
                            TherapyComplex p=ComplexTable.getInstance().getSelectedItem();
                            int i = ComplexTable.getInstance().getAllItems().indexOf(p);
                            ComplexTable.getInstance().getAllItems().set(i, null);
                            ComplexTable.getInstance().getAllItems().set(i, p);
                            ComplexTable.getInstance().select(i);
                            p = null;
                        }


                    })
            );
            encoder.setOnSucceeded(event ->
                    Platform.runLater(() ->
                    {
                        if(encoder.isStop())
                        {
                            CalcLayer.closeLayer();
                            getProgressAPI().setProgressBar(0.0, res.getString("app.cancel"), res.getString("app.title84"));
                            getProgressAPI(). hideProgressBar(true);
                            encoder.removeActionListener();
                            return;
                        }//если была остановка от кнопки в диалоге

                        if(encoder.getValue())getProgressAPI().setProgressBar(100.0, res.getString("app.title85"), res.getString("app.title84"));
                        else  getProgressAPI().setProgressBar(100.0, res.getString("app.cancel"), res.getString("app.title84"));
                        CalcLayer.closeLayer();
                        getProgressAPI().hideProgressBar(true);
                        //System.out.println("COMPLETE");
                        encoder.removeActionListener();

                        //обновим таблицу программ если у нас выбран комлекс
                        if(ComplexTable.getInstance().getSelectedItem()!=null)
                        {
                            TherapyComplex p=ComplexTable.getInstance().getSelectedItem();
                            int i = ComplexTable.getInstance().getAllItems().indexOf(p);
                            ComplexTable.getInstance().getAllItems().set(i, null);
                            ComplexTable.getInstance().getAllItems().set(i, p);
                            ComplexTable.getInstance().select(i);
                            p = null;
                        }

                    })
            );

            encoder.progressProperty().addListener((observable, oldValue, newValue) ->
            {
                //System.out.println(newValue);
                Platform.runLater(() -> getProgressAPI().setProgressBar(newValue.doubleValue(),encoder.getCurrentName() /*res.getString("app.title83")*/, res.getString("app.title84")));

            });


            thread.start();


            CalcLayer.showLayer();


        } catch (Exception e) {
            logger.error("",e);
            showExceptionDialog(res.getString("app.title86"), "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
        }



        tableProfile.getSelectionModel().getSelectedItem().setProfileWeight(tableProfile.getSelectionModel().getSelectedItem().getProfileWeight()+1);//пересчет веса инициализируем
        checkUpploadBtn();





    }



    /**
     * Загрузка профиля в выбранную папку. Папка должна быть пустая
     */
    public void uploadInDir()
    {
        if(tableProfile.getSelectionModel().getSelectedItem()==null) return;

        if(getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem()))
        {
            showWarningDialog(res.getString("app.title87"), "", res.getString("app.title97"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }

        //проверим нет ли мп3 с неверными путями
        List<TherapyProgram> failedMp3=new ArrayList<>();
        long count = getModel().mp3ProgramInProfile(tableProfile.getSelectionModel().getSelectedItem()).stream().filter(itm ->
        {
            File ff=new File(itm.getFrequencies());//в частотах прописан путь к файлу
            if(!ff.exists()) { failedMp3.add(itm); return true;}
            else return false;
        }).count();

        //напишим в окне список путей невверных
        if(count>0) {
            String failed = failedMp3.stream().map(tt -> tt.getFrequencies()).collect(Collectors.joining("\n"));
            updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            showWarningDialog(res.getString("app.title87"),failed,res.getString("app.ui.mp3_not_avaliable"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

            return;
        }


        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.upload_to_dir"));
        dirChooser.setInitialDirectory(new File(getModel().getLastSavedFilesPath(System.getProperty("user.home"))));

        File dir= dirChooser.showDialog(getApp().getMainWindow());
        if(dir==null)return;
        if(!dir.exists()) {
            showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_exist"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;}

        getModel().setLastSavedFilesPath(dir.getParentFile());

        int cnt = dir.list().length;
        if(cnt!=0)
        {
            showWarningDialog(res.getString("app.upload_to_dir"),  res.getString("app.ui.dir_not_empty"), res.getString("app.ui_dir_not_empty2"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;}

        ForceCopyProfile fcp=new ForceCopyProfile();



        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                fcp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                fcp.setFailHandler(failMessage -> failed());
                boolean r=false;
                try
                {
                    r=  fcp.forceCopyProfile(dir,true);
                }catch (Exception e)
                {
                    logger.error("",e);
                }
                return r;

            }
        };



        CalcLayer.openLayer(() -> Platform.runLater(() ->
        {
            //отмена генерации. Что успело нагенериться то успело
            //скрытие произойдет по окончании работы  ниже
            CalcLayer.setInfoLayer(res.getString("app.title76"));
            fcp.cancel();

        }), getApp().getMainWindow(),false);


        task.progressProperty().addListener((observable, oldValue, newValue) -> getProgressAPI().setProgressBar(newValue.doubleValue(),res.getString("app.title101"),""));
        task.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title102"),""));

        task.setOnSucceeded(event ->
        {
            CalcLayer.closeLayer();

            if (task.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

            } else {

                getProgressAPI().hideProgressBar(false);
                if(fcp.isCancel()) getProgressAPI().setProgressIndicator(res.getString("app.title104"));
                else getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            CalcLayer.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);


        });


        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");

        threadTask.start();
        CalcLayer.showLayer();

    }


    /**
     * Загрузка профиля в папку или на устройство
     *
     */
    public void onUploadProfile()
    {
        //проверим что действительно все файлы сгенерированны.
        //проверим что прибор подключен

        if(!getConnectedDevice()){
            showWarningDialog(res.getString("app.title87"), "", res.getString("app.title96"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }
        if(tableProfile.getSelectionModel().getSelectedItem()==null) return;

        if(getModel().isNeedGenerateFilesInProfile(tableProfile.getSelectionModel().getSelectedItem()))
        {
            showWarningDialog(res.getString("app.title87"), "", res.getString("app.title97"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }

        if(getDevicePath()==null )
        {
            showErrorDialog(res.getString("app.title87"),"",res.getString("app.title98"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }

        //проверим нет ли мп3 с неверными путями
        List<TherapyProgram> failedMp3=new ArrayList<>();
        long count = getModel().mp3ProgramInProfile(tableProfile.getSelectionModel().getSelectedItem()).stream().filter(itm ->
        {
            File ff=new File(itm.getFrequencies());//в частотах прописан путь к файлу
            if(!ff.exists()) { failedMp3.add(itm); return true;}
            else return false;
        }).count();

        //напишим в окне список путей невверных
        if(count>0) {
            String failed = failedMp3.stream().map(tt -> tt.getFrequencies()).collect(Collectors.joining("\n"));
            updateProfileTime(tableProfile.getSelectionModel().getSelectedItem());
            showWarningDialog(res.getString("app.title87"),failed,res.getString("app.ui.mp3_not_avaliable"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

            return;
        }

        File f=new   File( getDevicePath().toFile(),"profile.txt");
        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.uppload_"), res.getString("app.title99"), res.getString("app.title100"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if(buttonType.isPresent())
        {
            if(buttonType.get()==okButtonType)
            {
                //здесь стирание всего и запись нового
                ForceCopyProfile fcp=new ForceCopyProfile();



                Task<Boolean> task =new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        fcp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                        fcp.setFailHandler(failMessage -> failed());
                        boolean r=false;
                        try
                        {
                            r=  fcp.forceCopyProfile(f,false);
                        }catch (Exception e)
                        {
                            logger.error("",e);
                        }
                        return r;

                    }
                };



                CalcLayer.openLayer(() -> Platform.runLater(() ->
                {
                    //отмена генерации. Что успело нагенериться то успело
                    //скрытие произойдет по окончании работы  ниже
                    CalcLayer.setInfoLayer(res.getString("app.title76"));
                    fcp.cancel();

                }), getApp().getMainWindow(),false);


                task.progressProperty().addListener((observable, oldValue, newValue) -> getProgressAPI().setProgressBar(newValue.doubleValue(),res.getString("app.title101"),""));
                task.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title102"),""));

                task.setOnSucceeded(event ->
                {
                    CalcLayer.closeLayer();

                    if (task.getValue()) {
                        getProgressAPI().hideProgressBar(false);
                        getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

                    } else {

                        getProgressAPI().hideProgressBar(false);
                        if(fcp.isCancel()) getProgressAPI().setProgressIndicator(res.getString("app.title104"));
                        else getProgressAPI().setProgressIndicator(res.getString("app.title93"));
                    }
                    getProgressAPI().hideProgressIndicator(true);
                });

                task.setOnFailed(event -> {
                    CalcLayer.closeLayer();
                    getProgressAPI().hideProgressBar(false);
                    getProgressAPI().setProgressIndicator(res.getString("app.title93"));
                    getProgressAPI().hideProgressIndicator(true);


                });


                Thread threadTask=new Thread(task);
                threadTask.setDaemon(true);
                getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");

                threadTask.start();
                CalcLayer.showLayer();







            }else return;

        }else
        {

            showErrorDialog(res.getString("app.title87"),"","Ошибка выбора",getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }

    }



    class ForceCopyProfile
    {
        private UpdateHandler updateHandler;
        private FailHandler failHandler;

        private boolean cancel=false;

        public synchronized void cancel()
        {
            cancel=true;

        }

        public boolean isCancel() {
            return cancel;
        }

        private void callFail(String msg){if(failHandler!=null)failHandler.onFail(msg);}
        private void callUpdate(int progress,int total){if(updateHandler!=null)updateHandler.onUpdate(progress,total);}


        public FailHandler getFailHandler() {
            return failHandler;
        }

        public void setFailHandler(FailHandler failHandler) {
            this.failHandler = failHandler;
        }

        public UpdateHandler getUpdateHandler() {
            return updateHandler;
        }

        public void setUpdateHandler(UpdateHandler updateHandler) {
            this.updateHandler = updateHandler;
        }

        private boolean forceCopyProfile(File profile,boolean inDir) throws Exception
        {
            File prof =null;
            cancel = false;
            if (ProfileTable.getInstance().getSelectedItem() == null)
            {
                callFail("Не выбран профиль!");
                return false;
            }
            int total = (int) getModel().countTherapyPrograms(ProfileTable.getInstance().getSelectedItem()) + 2;
            int progress = 1;

            if (!inDir)
            {
                //здесь стирание всего и запись нового

                //на всякий проверим наличие lib файлов

                File f2 = new File(getDevicePath().toFile(), "ASDKDD.LIB");
                boolean flg = true;
                if (!f2.exists()) flg = false;
                else flg = true;

                if (flg == false)
                {
                    f2 = new File(getDevicePath().toFile(), "asdkdd.lib");
                    if (!f2.exists()) flg = false;
                    else flg = true;

                }

                if (flg == false)
                {

                    Platform.runLater(() -> showWarningDialog(res.getString("app.title87"), res.getString("app.title88"), res.getString("app.title89"), getApp().getMainWindow(), Modality.WINDOW_MODAL));


                    callFail("Ошибка идентификации диска прибора.");
                    return false;

                }


                //теперь все сотрем нафиг
                if (FilesProfileHelper.recursiveDelete(getDevicePath().toFile(), "asdkdd.lib"))
                {
                    Platform.runLater(() -> showErrorDialog(res.getString("app.title87"), "", res.getString("app.title90"), getApp().getMainWindow(), Modality.WINDOW_MODAL));

                    callFail("Ошибка удаления файлов");
                    return false;

                }

                prof=profile;

            }else
            {
                prof=new File(profile,"profile.txt");
            }
            callUpdate(progress++,total);


            if(cancel)return false;

            Map<Long,String> cMap=new LinkedHashMap<>();
            Map<Long,Integer> cMap2=new LinkedHashMap<>();


            TableColumn<TherapyComplex, ?> timeTableColumn = ComplexTable.getInstance().getTimeColoumn();//с учетом что время это 4 колонка!!!


            //TODO:   ///  нельзя использовать в именах  \ / ? : * " > < |  +=[]:;«,./?'пробел'     Нужно это выфильтровывать
            //генерируем список названий папкок для комплексов.
            ComplexTable.getInstance().getAllItems().forEach(itm ->
                    {

                        cMap.put(itm.getId(), TextUtil.replaceWinPathBadSymbols(itm.getName()) + " (" + DateUtil.replaceTime(timeTableColumn.getCellObservableValue(itm).getValue().toString(),res) + ")");
                        cMap2.put(itm.getId(), itm.getTimeForFrequency());
                    }
            );



            //

            if(cancel)return false;

//запись файла профиля
            try(PrintWriter writer = new PrintWriter(prof,"UTF-8")) {




                writer.println(ProfileTable.getInstance().getSelectedItem().getId().toString());//id профиля
                // writer.write("\n");

                writer.println(ProfileTable.getInstance().getSelectedItem().getUuid());//uuid профиля. Чтобы не перепутать с профилем записанном на другой программе
                // writer.write("\n");
/*
            for (Map.Entry<Long, String> entry : cMap.entrySet())
            {
                writer.write(entry.getKey()+"@"+entry.getValue()+"#");

            }
            writer.write("\n");
*/
                writer.print(ProfileTable.getInstance().getSelectedItem().getName());//название профиля
            }catch (IOException e)
            {
                logger.error("",e);
                Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title91"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));

                callFail("Ошибка записи файлов профиля.");
                return false;
            }
            catch (Exception e) {
                logger.error("",e);
                Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title92"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));

                callFail("Ошибка записи файлов профиля.");
                return false;
            }
            callUpdate(progress++,total);


            File bDir=null;
            if(inDir)bDir=profile;
            else bDir=getDevicePath().toFile();

            //запишем файлы и папки
            int cnt=0;
            File tempFile=null;
            if(cancel)return false;
            try
            {
                for (Map.Entry<Long, String> entry : cMap.entrySet())
                {
                    if(cancel)return false;
                    // URI outputURI = new URI(("file:///"+getDevicePath(). output.replaceAll(" ", "%20")));
                    //File outputFile = new File(outputURI);
                    tempFile= new File(bDir,(++cnt)+"-"+entry.getValue());//папка комплекса записываемого


                    //System.out.print("Создание папки комплекса: " + tempFile.getAbsolutePath() + "...");
                    // System.out.println("OK");

                    FilesProfileHelper.copyDir(tempFile);


                    int count2=0;
                    TherapyComplex therapyComplex=null;
                    for (TherapyProgram therapyProgram : getModel().findTherapyPrograms(entry.getKey()))
                    {
                        if (cancel) return false;
                        therapyComplex = getModel().findTherapyComplex(entry.getKey());

                        if (!therapyProgram.isMp3())
                        {
                            //если программа не MP3
                            String timeP = DateUtil.convertSecondsToHMmSs(therapyComplex.getTimeForFrequency());


                            String nameFile = (++count2) + "-" + TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()) + " (" + DateUtil.replaceTime(timeP,res) + ")";

                            // System.out.print("Создание программы: " + nameFile + ".bss...");

                            FilesProfileHelper.copyBSS(new File(getApp().getDataDir(), therapyProgram.getId() + ".dat"),
                                    new File(tempFile, nameFile + ".bss"));
                            //System.out.println("OK");
                            // System.out.print("Создание текста программы: " + nameFile + ".txt...");
                            FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), cMap2.get(entry.getKey()), therapyProgram.getId().longValue(),
                                    therapyProgram.getUuid(), entry.getKey(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),
                                    false, new File(tempFile, nameFile + ".txt"), therapyProgram.isMultyFreq(),therapyProgram.getSrcUUID(),therapyComplex.getSrcUUID());
                            // System.out.println("OK");
                        }else
                        {
                            //если программа MP3

                            Mp3File mp3file = null;
                            try {
                                mp3file = new Mp3File(therapyProgram.getFrequencies());
                            } catch (Exception e) {mp3file=null;}

                            if(mp3file!=null)
                            {
                                //если файл существует
                                String timeP =DateUtil.convertSecondsToHMmSs(mp3file.getLengthInSeconds());

                                String nameFile = (++count2) + "-" + TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()) + " (" + DateUtil.replaceTime(timeP,res) + ")";

                                FilesProfileHelper.copyBSS(new File(therapyProgram.getFrequencies()),new File(tempFile, nameFile + ".bss"));

                                FilesProfileHelper.copyTxt(therapyProgram.getFrequencies(), cMap2.get(entry.getKey()), therapyProgram.getId().longValue(),
                                        therapyProgram.getUuid(), entry.getKey(),therapyComplex.getBundlesLength(), TextUtil.replaceWinPathBadSymbols(therapyProgram.getName()),
                                        true, new File(tempFile, nameFile + ".txt"), therapyProgram.isMultyFreq(),therapyProgram.getSrcUUID(),therapyComplex.getSrcUUID());




                            }
                        }


                        callUpdate(progress++, total);

                    }

                    //System.out.println("COMPLETED");
                }
            }catch (IOException e)
            {

                logger.error("",e);
                Platform.runLater(() -> showWarningDialog(res.getString("app.title87"), "", res.getString("app.title115"), getApp().getMainWindow(), Modality.WINDOW_MODAL));
                callFail("Ошибка копирования файлов.");
                return false;
            }
            catch (Exception e)
            {
                logger.error("",e);
                Platform.runLater(() -> showExceptionDialog(res.getString("app.title87"), "", res.getString("app.title93"), e, getApp().getMainWindow(), Modality.WINDOW_MODAL));
                callFail("Ошибка копирования файлов.");
                return false;
            }


            return true;
        }




    }
    interface UpdateHandler
    {
        public void onUpdate(int progress,int total);

    }
    interface FailHandler
    {
        public void onFail(String failMessage);

    }



    class RemoveProfileFiles
    {
        private UpdateHandler updateHandler;
        private FailHandler failHandler;
        private boolean cancell=false;

        public  boolean isCancell() {
            return cancell;
        }

        public synchronized void cancel() {
            this.cancell=true;
        }

        private void callFail(String msg){if(failHandler!=null)failHandler.onFail(msg);}
        private void callUpdate(int progress,int total){if(updateHandler!=null)updateHandler.onUpdate(progress,total);}


        public FailHandler getFailHandler() {
            return failHandler;
        }

        public void setFailHandler(FailHandler failHandler) {
            this.failHandler = failHandler;
        }

        public UpdateHandler getUpdateHandler() {
            return updateHandler;
        }

        public void setUpdateHandler(UpdateHandler updateHandler) {
            this.updateHandler = updateHandler;
        }


        public boolean remove()
        {
            boolean ret=true;

            cancell=false;

            File f = null;
            List<TherapyProgram> allTherapyProgram = getModel().getAllTherapyProgram(ProfileTable.getInstance().getSelectedItem()).
                    stream().filter(therapyProgram -> !therapyProgram.isMp3()).collect(Collectors.toList());
            int total=allTherapyProgram.size();
            int progress=1;
            for (TherapyProgram itm : allTherapyProgram)
            {
                if(isCancell()){
                    ret=false;
                    break;
                }

                f = new File(getApp().getDataDir(), itm.getId() + ".dat");
                if (f.exists()) f.delete();
                itm.setChanged(true);
                try
                {
                    getModel().updateTherapyProgram(itm);
                } catch (Exception e)
                {
                    logger.error("",e);
                    showExceptionDialog("Ошибка обновления данных терапевтических программ.", "Очистка произошла с ошибкой", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    ret=false;
                    callFail("Очистка произошла с ошибкой");
                    break;
                }
                callUpdate(progress++,total);
            }




            return ret;
        }

    }

    private ProgressAPI getProgressAPI(){
        return AppController.getProgressAPI();
    }

    public void onPrintProfile(){
        printProfile();
    }

    @Override
    public void removeProfileFiles()
    {
        if (ProfileTable.getInstance().getSelectedItem() == null) return;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title73"), res.getString("app.title74"), res.getString("app.title75"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if (buttonType.isPresent())
        {
            if (buttonType.get() == okButtonType)
            {
                RemoveProfileFiles rp=new RemoveProfileFiles();

                Task<Boolean> task=new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {

                        rp.setUpdateHandler((progress, total) -> updateProgress(progress, total));
                        rp.setFailHandler(failMessage -> failed());
                        return rp.remove();

                    }
                };


                CalcLayer.openLayer(() -> Platform.runLater(() ->
                {
                    //отмена генерации. Что успело нагенериться то успело
                    //скрытие произойдет по окончании работы  ниже
                    CalcLayer.setInfoLayer(res.getString("app.title76"));
                    rp.cancel();

                }), getApp().getMainWindow(),false);



                task.setOnScheduled(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title77"), res.getString("app.title78")));

                task.setOnFailed(event ->
                        Platform.runLater(() ->
                        {
                            CalcLayer.closeLayer();
                            getProgressAPI().setProgressBar(100.0, res.getString("app.title79"), res.getString("app.title78"));

                            if (ProfileTable.getInstance().getSelectedItem() != null)
                            {
                                Profile p = ProfileTable.getInstance().getSelectedItem();
                                int i = ProfileTable.getInstance().getAllItems().indexOf(p);
                                ProfileTable.getInstance().getAllItems().set(i, null);
                                ProfileTable.getInstance().getAllItems().set(i, p);
                                Platform.runLater(() -> ProfileTable.getInstance().select(i));
                                p = null;
                            }


                            getProgressAPI().hideProgressBar(true);

                            rp.setFailHandler(null);
                            rp.setUpdateHandler(null);





                        })
                );
                task.setOnSucceeded(event ->
                        Platform.runLater(() ->
                        {

                            if (task.getValue())
                                getProgressAPI().setProgressBar(100.0, res.getString("app.title80"), res.getString("app.title78"));
                            else {
                                if (rp.isCancell()) getProgressAPI().setProgressBar(100.0,  res.getString("app.cancel"), res.getString("app.title78"));
                                else getProgressAPI().setProgressBar(0.0,  res.getString("app.error"), res.getString("app.title78"));
                            }


                            if (ProfileTable.getInstance().getSelectedItem() != null)
                            {
                                Profile p = ProfileTable.getInstance().getSelectedItem();
                                int i = ProfileTable.getInstance().getAllItems().indexOf(p);
                                ProfileTable.getInstance().getAllItems().set(i, null);
                                ProfileTable.getInstance().getAllItems().set(i, p);
                                Platform.runLater(() -> ProfileTable.getInstance().select(i));
                                p = null;
                            }


                            CalcLayer.closeLayer();
                            getProgressAPI().hideProgressBar(true);

                            rp.setFailHandler(null);
                            rp.setUpdateHandler(null);



                        })
                );

                task.progressProperty().addListener((observable, oldValue, newValue) ->
                {

                    Platform.runLater(() -> getProgressAPI().setProgressBar(newValue.doubleValue(), res.getString("app.title81"), res.getString("app.title78")));

                });
                Thread thread=new Thread(task);
                thread.setDaemon(true);
                thread.start();


                CalcLayer.showLayer();


            }
        }


    }
    @Override
    public void removeProfile()
    {
        Profile selectedItem = ProfileTable.getInstance().getSelectedItem();

        if(selectedItem==null) return;


        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title64"), "", res.getString("app.title65"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

        if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
        {
            try {



                List<Long> profileFiles = getModel().getProfileFiles(selectedItem);
                File temp=null;
                for (Long file : profileFiles)
                {
                    temp=new File(getApp().getDataDir(),file+".dat");
                    if(temp.exists())temp.delete();
                }

                getModel().removeProfile(selectedItem);
                ProfileTable.getInstance().getAllItems().remove(selectedItem);
                selectedItem=null;


            } catch (Exception e) {
                selectedItem=null;
                logger.error("",e);
                showExceptionDialog("Ошибка удаления профиля","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
            }

        }

    }
    /*** API **/
    @Override
    public void disableGenerateBtn() {
        btnGenerate.setDisable(true);
    }

    @Override
    public void enableGenerateBtn() {
        btnGenerate.setDisable(false);
    }
    /**
     * перерсчтет времени на профиль. Профиль инстанс из таблицы.
     * @param p
     */
    @Override
    public void updateProfileTime(Profile p) {
        p.setTime(p.getTime() + 1);
    }
    /**
     * Изменяет состояние кнопки загрузки в прибор. Стоит проверить при изменении состояния устройства и изменеии состояния кнопки загрузки
     */
    @Override
    public void checkUpploadBtn()
    {
        //свойство заставит сработать проверку доступности кнопки btnUpload.disableProperty().bind(new BooleanBinding()
        checkUppload.set(!checkUppload.get());

    }

    @Override
    public void printProfile()
    {
        if(tableProfile.getSelectionModel().getSelectedItem()==null)return;

        try {
            openDialog(getApp().getMainWindow(),"/fxml/PrintContent.fxml",res.getString("app.menu.print_profile"),true,StageStyle.DECORATED,0,0,0,0,tableProfile.getSelectionModel().getSelectedItem().getId(),1);


        } catch (IOException e) {
            logger.error("",e);
        }
    }

    @Override
    public  void pasteProfile() {
        Profile profile = ProfileTable.getInstance().getSelectedItem();
        if(profile==null) return;
        int dropIndex= ProfileTable.getInstance().getSelectedIndex();

        Clipboard clipboard= Clipboard.getSystemClipboard();
        if(!clipboard.hasContent(ProfileTable.PROFILE_CUT_ITEM_ID)) return;
        if(!clipboard.hasContent(ProfileTable.PROFILE_CUT_ITEM_INDEX)) return;


        Integer ind = (Integer) clipboard.getContent(ProfileTable.PROFILE_CUT_ITEM_INDEX);
        if (ind == null) return;
        else {
            if (dropIndex == ind) return;
            else {
                Profile movedProfile = ProfileTable.getInstance().getAllItems().get(ind);
                if(movedProfile==null) {
                    clipboard.clear();
                    return;
                }
                ProfileTable.getInstance().getAllItems().remove(movedProfile);
                ProfileTable.getInstance().getAllItems().add(dropIndex,movedProfile);
                Profile tmp;
                try {
                    for (int i=0; i<ProfileTable.getInstance().getAllItems().size();i++){
                        tmp = ProfileTable.getInstance().getAllItems().get(i);
                        tmp.setPosition(i);
                        getModel().updateProfile(tmp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    clipboard.clear();
                    return;
                }



            }
        }

        clipboard.clear();
    }


    @Override
    public   void cutProfileToBuffer() {
        Profile profile = ProfileTable.getInstance().getSelectedItem();
        if(profile==null) return;
        Clipboard clipboard=Clipboard.getSystemClipboard();
        clipboard.clear();
        ClipboardContent content = new ClipboardContent();
        content.put(ProfileTable.PROFILE_CUT_ITEM_ID, profile.getId());
        content.put(ProfileTable.PROFILE_CUT_ITEM_INDEX, ProfileTable.getInstance().getSelectedIndex());
        clipboard.setContent(content);
    }

}
