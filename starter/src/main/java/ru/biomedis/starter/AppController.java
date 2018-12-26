package ru.biomedis.starter;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import org.anantacreative.updater.Pack.Exceptions.PackException;
import org.anantacreative.updater.Update.AbstractUpdateTaskCreator;
import org.anantacreative.updater.Update.UpdateException;
import org.anantacreative.updater.Update.UpdateTask;
import org.anantacreative.updater.Version;
import org.anantacreative.updater.VersionCheck.DefineActualVersionError;
import org.anantacreative.updater.VersionCheck.XML.XmlVersionChecker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class AppController extends BaseController {

    @FXML private Button installUpdatesBtn;
    @FXML private Button startProgramBtn;

    @FXML private ProgressIndicator versionCheckIndicator;
    @FXML private Label textInfo;
    @FXML private ProgressBar updateIndicator;
    @FXML private Hyperlink linkMain;
    @FXML private Hyperlink linkArticles;

    @FXML private Hyperlink linkVideo;
    @FXML private Hyperlink linkEducation;
    @FXML private Hyperlink linkVideoM;
    @FXML private Hyperlink linkContacts;
    @FXML private ImageView errorImage;
    @FXML private ImageView doneImage;
    @FXML private Label currentFileProgress;
    @FXML private HBox centerLayout;
    @FXML private Label vStarter;
    @FXML private Label  antivirInfo;
    @FXML private Hyperlink trinityUtil;

    private Version version=null;

    @Override
    public void setParams(Object... params) {

    }

    private ResourceBundle getRes() {
        return getApp().getResources();
    }

    @Override
    protected void onCompletedInitialise() {
        initLinks();
        try {
            version = DataHelper.selectUpdateVersion();
            getControllerWindow().setTitle(getRes().getString("app.name")+" "+version);
            System.out.println("Current Version: "+version);
            checkActualVersion();
            loadWebContent();
            vStarter.setText(getApp().getStarterVersion().toString());


        } catch (Exception e) {
            Log.logger.error("Ошибка определения версии программы", e);
            disableUpdateAndEnableStartProgram();
            setTextInfo(getRes().getString("define_version_program_error"));
            showErrorImage();
        }
    }

    private void loadWebContent() {
        CompletableFuture<List<NewsProvider.News>> news;
        if(isRussianLocale()) {
             news = NewsProvider.getRusNews(11);
        }else  news = NewsProvider.getEngNews(11);

        news.thenAccept(n-> addBlockToWebContent( buildNewsBlock(n) ))
            .exceptionally(ex ->{ reactionOnLoadWebContentException(ex); return null;} );

    }

    private void reactionOnLoadWebContentException(Throwable t){
       t.printStackTrace();

    }

    private void addBlockToWebContent(Parent block){

        Platform.runLater(() -> centerLayout.getChildren().add(block));

    }

    private Parent buildNewsBlock(List<NewsProvider.News> news){

        VBox vBox=new VBox(5);
        vBox.setMaxWidth(Double.MAX_VALUE);
            vBox.getChildren().addAll(news.stream().map(n->{
               return new NewsView(n.getUrl(),n.getTitle(),n.getDate(),getRes().getString("extended"),getApp()).getNode();
            }).collect(Collectors.toList()));
        ScrollPane sp = new ScrollPane(vBox);
        sp.setMaxWidth(Double.MAX_VALUE);
        sp.setStyle("-fx-border-color: white;");

        HBox.setHgrow(sp, Priority.ALWAYS);
        return sp;
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        disableUpdateAndStartProgram();
        initErrorImage();
        initDoneImage();
        hideErrorImage();
        hideVersionCheckIndicator();
        hideUpdateProgress();
        hideCurrentFileProgress();

        trinityUtilInit();

        initInfoStrings();


    }

    private void initInfoStrings() {

        antivirInfo.setGraphic(new ImageView(new Image(AppController.class.getResource("/images/warn.png").toExternalForm())));
    }

    private void trinityUtilInit() {
        trinityUtil.setOnAction(event -> {
            trinityUtil.setVisited(false);
            try {
                openDialog(getControllerWindow(),"/fxml/trinity_util.fxml","Trinity tool",true, StageStyle.DECORATED,0, 0,0,0);
            } catch (Exception e) {
                Log.logger.error("",e);
            }
        });


    }


    public void onInstallUpdates(){
        setTextInfo(getRes().getString("downloading_files"));
        disableUpdateAndStartProgram();
        showVersonCheckIndicator();
        AutoUpdater.getAutoUpdater().startUpdater(version, new AutoUpdater.Listener() {
            @Override
            public void taskCompleted() {


                Platform.runLater(() -> {
                    setTextInfo(getRes().getString("ready_for_update"));

                });

                performUpdate();
            }

            @Override
            public void error(Exception e) {

                Platform.runLater(() -> {
                    String cause="";
                    if(e instanceof MalformedURLException) cause =getRes().getString("get_update_error");
                    else   if(e instanceof DefineActualVersionError) cause =getRes().getString("define_update_version");
                    else   if(e instanceof DefineActualVersionError) cause =getRes().getString("define_update_version");
                    else   if(e instanceof AbstractUpdateTaskCreator.CreateUpdateTaskError) cause =getRes().getString("prepare_update_error");
                    else   if(e instanceof AutoUpdater.NotSupportedPlatformException) cause =getRes().getString("platform_updates_not_av");


                    setTextInfo(cause);
                    showErrorImage();
                    disableUpdateAndEnableStartProgram();
                    Waiter.closeLayer();
                });
            }

            @Override
            public void completeFile(String name) {

            }

            @Override
            public void currentFileProgress(float val) {
                Platform.runLater(() -> showCurrentFileProgress(val));
            }

            @Override
            public void nextFileStartDownloading(String name) {

            }

            @Override
            public void totalProgress(float val) {
                Platform.runLater(() -> setUpdateProgress(val));
            }
        });
    }

    private void performUpdate() {
        Platform.runLater(() -> {
            showVersonCheckIndicator();
            setTextInfo(getRes().getString("createDB_backup"));
            Waiter.openLayer(App.getAppController().getApp().getMainWindow(), true);
        });

        try {
            App.getAppController().getApp().closePersisenceContext();
            System.out.println("Make DB backup");
            DataHelper.ZipDBToBackup();
            System.out.println("DB backup completed");
        } catch (PackException e) {
            e.printStackTrace();
            Platform.runLater(() ->  {
                setTextInfo(getRes().getString("process_updateing_stoped")+" "+getApp().getResources().getString("backup_error"));
                showErrorImage();
                disableUpdateAndEnableStartProgram();
                Waiter.closeLayer();
            });

        }catch (Exception e){
            e.printStackTrace();
            Platform.runLater(() ->  {
                setTextInfo(getRes().getString("process_updateing_stoped")+" "+getApp().getResources().getString("backup_error"));
                showErrorImage();
                disableUpdateAndEnableStartProgram();
                Waiter.closeLayer();
            });
        }
        finally {
            getApp().reopenPersistentContext();
        }


        Platform.runLater(() -> setTextInfo(getRes().getString("files_install")));

        setUpdateProgress(0);
        try {
            AutoUpdater.getAutoUpdater().performUpdateTask(new UpdateTask.UpdateListener() {
                @Override
                public void progress(int i) {
                   Platform.runLater(() -> setUpdateProgress(i));
                }

                @Override
                public void completed() {
                    Platform.runLater(() -> {
                        setTextInfo(getRes().getString("complete_update"));
                        showDoneImage();
                        disableUpdateAndEnableStartProgram();
                        Waiter.closeLayer();
                    });

                }

                @Override
                public void error(UpdateException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setTextInfo(getRes().getString("processing_updating_files_error"));
                        showErrorImage();
                        disableUpdateAndEnableStartProgram();
                        Waiter.closeLayer();
                    });
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            if(e instanceof AutoUpdater.UpdateInProcessException) Log.logger.info("Обновление уже процессе.");
            else {
                Log.logger.error("",e);
                setTextInfo(getRes().getString("processing_updating_files_error"));
                showErrorImage();
                disableUpdateAndEnableStartProgram();
                Platform.runLater(() -> Waiter.closeLayer());
            }
        }
    }


    private enum LINKS{
        MAIN,
        ARTICLES,
        VIDEO,
        EDUCATION,
        VIDEO_M,
        CONTACTS
    }



    private Map<LINKS, String> links = new HashMap<>();


    public void initLinks(){
        initLinksURL();
        initLinkNames();
        initLinksAction();
    }


    private void initLinksAction(){
        linkMain.setOnAction(linkAction(LINKS.MAIN));
        linkArticles.setOnAction(linkAction(LINKS.ARTICLES));
        linkVideo.setOnAction(linkAction(LINKS.VIDEO));
        linkEducation.setOnAction(linkAction(LINKS.EDUCATION));
        linkVideoM.setOnAction(linkAction(LINKS.VIDEO_M));
        linkContacts.setOnAction(linkAction(LINKS.CONTACTS));
    }

    private void initLinkNames(){
        if(!isRussianLocale()){
            linkMain.setText("Biomedis company website");
            linkArticles.setText("Articles");
            linkContacts.setText("Contacts");
            linkEducation.setText("Education");
            linkVideo.setText("Videos");
            linkVideoM.setText("Video tutorials 'Trinity'");

        }
    }


    private void initLinksURL(){

        if(isRussianLocale()) {
            links.put(LINKS.MAIN, "http://biomedis.life");
            links.put(LINKS.ARTICLES, "http://biomedis.ru/allarticle.php");
            links.put(LINKS.VIDEO, "https://www.youtube.com/user/BiomedisRu");
            links.put(LINKS.EDUCATION, "https://www.biomedis.life/education.php");
            links.put(LINKS.VIDEO_M, "https://www.youtube.com/watch?v=cjneDSk-Vo8&list=PLhtxItJSvMn27NeC_VlrwsIu3C1YebdRs");
            links.put(LINKS.CONTACTS, "https://www.biomedis.life/contact.php");
        }else {
            links.put(LINKS.MAIN, "http://biomedis.life/en/");
            links.put(LINKS.ARTICLES, "http://biomedis.ru/en/allarticle.php");
            links.put(LINKS.VIDEO, "http://biomedis.ru/en/");
            links.put(LINKS.EDUCATION, "https://www.biomedis.life/education.php/en/");
            links.put(LINKS.VIDEO_M, "https://www.youtube.com/playlist?list=PLhtxItJSvMn1CRxxAN13fxLERDFc1q7cQ");
            links.put(LINKS.CONTACTS, "https://www.biomedis.life/en/contact.php");
        }
    }

    private boolean isRussianLocale(){
        return getApp().getProgramLocale().getLanguage().equals(new Locale("ru").getLanguage());
    }

    private EventHandler<ActionEvent> linkAction(LINKS type){
        return event -> {
            if(event.getSource() instanceof Hyperlink) ((Hyperlink)event.getSource()).setVisited(false);
            openLinkInBrowser(links.get(type));
        };
    }

    private void openLinkInBrowser(String link){
        DefaultBrowserCaller.openInBrowser(link,getApp());
    }



    private  void showCurrentFileProgress(float val){
        currentFileProgress.setVisible(true);
        currentFileProgress.setText(Math.round(val)+"%");
    }

    private void hideCurrentFileProgress(){
        currentFileProgress.setVisible(false);
    }

    public void hideErrorImage(){
        errorImage.setVisible(false);
    }

    public void showErrorImage(){
        errorImage.setVisible(true);
        hideVersionCheckIndicator();
        hideDoneImage();
        hideCurrentFileProgress();
    }

    public void hideDoneImage(){
        doneImage.setVisible(false);
    }

    public void showDoneImage(){
        doneImage.setVisible(true);
        hideVersionCheckIndicator();
        hideErrorImage();
        hideCurrentFileProgress();
    }
    private void initErrorImage(){
        URL errImgUrl = getClass().getResource("/images/error.png");
        errorImage.setImage(new Image(errImgUrl.toExternalForm()));
    }
    private void initDoneImage(){
        URL doneImgUrl = getClass().getResource("/images/correct.png");
        doneImage.setImage(new Image(doneImgUrl.toExternalForm()));
    }


    private void hideVersionCheckIndicator(){
        versionCheckIndicator.setProgress(-1);
        versionCheckIndicator.setVisible(false);
    }

    private void showVersonCheckIndicator(){
        versionCheckIndicator.setProgress(-1);
        versionCheckIndicator.setVisible(true);
        hideErrorImage();
        hideDoneImage();
        currentFileProgress.setText("");
    }

    private void hideUpdateProgress(){
        updateIndicator.setVisible(false);
    }

    private void setUpdateProgress(double persentVal){
        updateIndicator.setVisible(true);
        updateIndicator.setProgress(persentVal/100.0);
    }

    private void setTextInfo(String text){
        textInfo.setText(text);
    }
    public void onStartProgram(){
        disableUpdateAndStartProgram();
        getApp().startMainApp();
    }

    private Version getVersionFromFile(){
        //только для винды, проверит файл version.txt в директории assets и считает версию.
        // Для того чтобы при установке обновления инсталлерром не пытаться обновляться.

            TextFileLineReader tlr =new TextFileLineReader(new File("./version.txt"));
            try {
                String vString = tlr.readToString(1);
                String[] split = vString.split("\\.");
                if(split.length!=3) throw new Exception();

                Version v = new Version(Integer.valueOf(split[0]),Integer.valueOf(split[1]),Integer.valueOf(split[2]));
                return v;
            } catch (IOException e) {
                e.printStackTrace();
                return new Version(0,0,0);
            } catch (Exception e) {
                e.printStackTrace();
                return new Version(0,0,0);
            }

    }
    private void checkActualVersion(){
            showVersonCheckIndicator();
        Version fVersion =  getVersionFromFile();
        File distFile=new File("./dist.jar");
        try {
            Version useVersion;
            if(fVersion.lessThen(version) || distFile.exists()==false){
                //значит пользователь поставил поверх новой версии старую чрез инсталлер и сейчас нужно обязательно обновить!!! Запускать нельзя тк могут быть ошибки
                showErrorImage();
                setTextInfo(getRes().getString("files_corrupted"));
                hideVersionCheckIndicator();
                disableUpdateAndStartProgram();
                enableUpdate();
                getControllerWindow().setTitle(getRes().getString("app.name")+" "+fVersion);
                return;
            }
            else {
                useVersion =fVersion;
                getControllerWindow().setTitle(getRes().getString("app.name")+" "+useVersion);
            }

            final XmlVersionChecker versionChecker  = AutoUpdater.getAutoUpdater().getVersionChecker(useVersion);
            versionChecker.checkNeedUpdateAsync()
                          .thenAccept(v->{
                              System.out.println("Актуальная версия" +versionChecker.getActualVersion());

                              if(v){
                                  setTextInfo(getRes().getString("current_version")+": "+ version +". "+getRes().getString("actual_version")+": " + versionChecker.getActualVersion());
                                  hideVersionCheckIndicator();
                                  enableUpdateAndStartProgram();
                              }else {
                                      showDoneImage();
                                      setTextInfo(getRes().getString("updates_absent"));
                                      hideVersionCheckIndicator();
                                      disableUpdateAndEnableStartProgram();
                              }


                          })
                          .exceptionally(e->{
                              Log.logger.error("Ошибка определения актуальной версии",e);
                              disableUpdateAndEnableStartProgram();
                              showErrorImage();
                              printVersionCheckError();
                              return null;
                          });

        } catch (AutoUpdater.NotSupportedPlatformException e) {
            Log.logger.error("Ошибка определения актуальной версии",e);
            disableUpdateAndEnableStartProgram();
            showErrorImage();
            printVersionCheckError();
        } catch (MalformedURLException e) {
            Log.logger.error("Ошибка определения актуальной версии",e);
            disableUpdateAndEnableStartProgram();
            showErrorImage();
            printVersionCheckError();
        }
    }



    private void enableUpdate() {
        installUpdatesBtn.setDisable(false);
    }

    private void enableStartProgram() {
        startProgramBtn.setDisable(false);
    }
    private void disableUpdateAndEnableStartProgram(){
        installUpdatesBtn.setDisable(true);
        startProgramBtn.setDisable(false);
    }

    private void enableUpdateAndStartProgram(){
        installUpdatesBtn.setDisable(false);
        startProgramBtn.setDisable(false);
    }

    private void disableUpdateAndStartProgram(){
        installUpdatesBtn.setDisable(true);
        startProgramBtn.setDisable(true);
    }

    private void printVersionCheckError(){

        Platform.runLater(() -> setTextInfo(getRes().getString("define_update_version")));
    }
}
