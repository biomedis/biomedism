/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.biomedis.biomedismair3;


import java.lang.reflect.Field;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;


@Slf4j
public abstract class BaseController implements Initializable {
    protected static App app = null;
    protected static AppController mdc = null;
    protected Stage window = null;


    public Stage getControllerWindow() {
        if(window==null) return mdc.window;
        else return window;
    }
    /**
     * Вызывается после завершиния инициализации контроллера
     */
    protected abstract void onCompletedInitialization();

    protected abstract void onClose(@NotNull WindowEvent event);

    public ModelDataApp getModel() {
        return app.getModel();
    }

    public void setWindow(@NotNull Stage win) {
        window = win;
    }

    /**
     * Установка базового класса приложения для доступа из контроллеров.
     *
     * @param lp
     */
    public static void setApp(@NotNull App lp) {
        BaseController.app = lp;
    }

    /**
     * Установка базового контролера для доступа из других контроллеров
     *
     * @param mc
     */
    public static void setMainController(@NotNull AppController mc) {
        mdc = mc;
    }


    /**
     * Возвращает объект главного класса приложения
     *
     * @return
     */
    @NotNull
    public static App getApp() {
        return BaseController.app;
    }


    /**
     * Данные переданые диалоговому окну как userData
     * @return
     */
    protected Object getInputDialogData(){
        return getControllerWindow().getScene().getRoot().getUserData();
    }
    /**
     * Создание окна. Параметры передаются после вызова initialize в контроллере!!! Это нужно учесть при проектировании контроллера. Те если что нужно инициализировать то именно в обработчике установки параметров
     *
     * @param ownerWindow - родительское окно. Может быть null для нового отдельного
     * @param fxml        - строка путь к файлу fxml
     * @param title       - заголовок окна
     * @param resizeble   - можно ли окно масштабировать
     * @param st          --стиль окна StageStyle
     * @param minH        - минимальная высота(0 для игнора)
     * @param minW        -минимальная ширина(0 для игнора)
     * @param maxH        - минимальная высота(0 для игнора)
     * @param maxW        - минимальная ширина(0 для игнора)
     * @param params      - параметры для контролеера.  Каждый контроллер должен раелизовать класс BaseController и реализовать метод setParams, для разбора параметров если нужно
     * @param userData    пользовотельский объект данных, он же и вернет. Объект доступен как getInputDialogData(). В контроллере необходимо, присвоить поля этому объекту, не создавать новый!
     * Не передавать через него, входные данные. Для этого есть params.
     * @throws IOException
     */
    @NotNull
    public static <T> T openDialogUserData(@NotNull Stage ownerWindow, @NotNull String fxml, @NotNull String title,  boolean resizeble, @NotNull StageStyle st, int minH, int minW, int maxH, int maxW, @NotNull T userData, Object... params) throws IOException {
        Stage dlg = new Stage(st);
        dlg.initOwner(ownerWindow);
        dlg.initModality(Modality.WINDOW_MODAL);

        Image image = ownerWindow.getIcons().get(0);
        dlg.getIcons().clear();
        dlg.getIcons().add(image);


        WeakReference<T> userDataRef = new WeakReference<>(userData);


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setClassLoader(app.getClass().getClassLoader());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(dlg);

        dlg.setOnCloseRequest(controller::onClose);

        root.setUserData(userData);
        controller.setParams(params);//до открытия окна в show, можно устанавливать любые параметры. initialize вызывается в контроллере до этого !!
        controller.onCompletedInitialization();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        dlg.setScene(scene);
        dlg.setTitle(title);
        dlg.setResizable(resizeble);
        if (minH != 0) dlg.setMinHeight(minH);
        if (minW != 0) dlg.setMinWidth(minW);
        if (maxH != 0) dlg.setMaxHeight(maxH);
        if (maxW != 0) dlg.setMaxWidth(maxW);

        dlg.showAndWait();
        dlg.setOnCloseRequest(null);

        root.setUserData(null); //теперь если очистить ссылку снаружи, сборщик мусора сможет забрать ресурсы окна
        return userDataRef.get();
    }

    /**
     * Создание окна. Параметры передаются после вызова initialize в контроллере!!! Это нужно учесть при проектировании контроллера. Те если что нужно инициализировать то именно в обработчике установки параметров
     *
     * @param ownerWindow - родительское окно. Может быть null для нового отдельного
     * @param fxml        - строка путь к файлу fxml
     * @param title       - заголовок окна
     * @param resizeble   - можно ли окно масштабировать
     * @param st          --стиль окна StageStyle
     * @param minH        - минимальная высота(0 для игнора)
     * @param minW        -минимальная ширина(0 для игнора)
     * @param maxH        - минимальная высота(0 для игнора)
     * @param maxW        - минимальная ширина(0 для игнора)
     * @param params      - параметры для контролеера.  Каждый контроллер должен раелизовать класс BaseController и реализовать метод setParams, для разбора параметров если нужно
     * @throws IOException
     */
    public static void openDialog(@NotNull Stage ownerWindow, @NotNull String fxml, @NotNull String title, boolean resizeble, @NotNull StageStyle st, int minH, int minW, int maxH, int maxW, Object... params) throws IOException {
        Stage dlg = new Stage(st);
        dlg.initOwner(ownerWindow);
        dlg.initModality(Modality.WINDOW_MODAL);

        Image image = ownerWindow.getIcons().get(0);
        dlg.getIcons().clear();
        dlg.getIcons().add(image);


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(dlg);
        controller.setParams(params);//до открытия окна в show, можно устанавливать любые параметры
        controller.onCompletedInitialization();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        dlg.setScene(scene);
        dlg.setTitle(title);
        dlg.setResizable(resizeble);
        if (minH != 0) dlg.setMinHeight(minH);
        if (minW != 0) dlg.setMinWidth(minW);
        if (maxH != 0) dlg.setMaxHeight(maxH);
        if (maxW != 0) dlg.setMaxWidth(maxW);

        dlg.showAndWait();


    }

    /**
     * Создание окна. Параметры передаются после вызова initialize в контроллере!!! Это нужно учесть при проектировании контроллера. Те если что нужно инициализировать то именно в обработчике установки параметров
     *
     * @param ownerWindow - родительское окно. Может быть null для нового отдельного
     * @param fxml        - строка путь к файлу fxml
     * @param title       - заголовок окна
     * @param resizeble   - можно ли окно масштабировать
     * @param st          --стиль окна StageStyle
     * @param minH        - минимальная высота(0 для игнора)
     * @param minW        -минимальная ширина(0 для игнора)
     * @param maxH        - минимальная высота(0 для игнора)
     * @param maxW        - минимальная ширина(0 для игнора)
     * @param params      - параметры для контролеера.  Каждый контроллер должен раелизовать класс BaseController и реализовать метод setParams, для разбора параметров если нужно
     * @throws IOException
     */
    public static void openDialogNotModal(@NotNull Stage ownerWindow, @NotNull String fxml, @NotNull String title, boolean resizeble, @NotNull StageStyle st, int minH, int minW, int maxH, int maxW, Object... params) throws IOException {
        openDialogNotModal( ownerWindow,  fxml,  title,  resizeble, st, minH,
         minW,  maxH,  maxW,  "", params);
    }

    /**
     * Создание окна. Параметры передаются после вызова initialize в контроллере!!! Это нужно учесть при проектировании контроллера. Те если что нужно инициализировать то именно в обработчике установки параметров
     *
     * @param ownerWindow - родительское окно. Может быть null для нового отдельного
     * @param fxml        - строка путь к файлу fxml
     * @param title       - заголовок окна
     * @param resizeble   - можно ли окно масштабировать
     * @param st          --стиль окна StageStyle
     * @param minH        - минимальная высота(0 для игнора)
     * @param minW        -минимальная ширина(0 для игнора)
     * @param maxH        - минимальная высота(0 для игнора)
     * @param maxW        - минимальная ширина(0 для игнора)
     * @param cssResourcePath - ресурсный путь к css файлу
     * @param params      - параметры для контролеера.  Каждый контроллер должен раелизовать класс BaseController и реализовать метод setParams, для разбора параметров если нужно
     * @throws IOException
     */
    public static void openDialogNotModal(@NotNull Stage ownerWindow, @NotNull String fxml, @NotNull String title, boolean resizeble, @NotNull StageStyle st, int minH, int minW, int maxH, int maxW,@NotNull String cssResourcePath, Object... params) throws IOException {
        Stage dlg = new Stage(st);
        dlg.initOwner(ownerWindow);
        dlg.initModality(Modality.NONE);

        Image image = ownerWindow.getIcons().get(0);
        dlg.getIcons().clear();
        dlg.getIcons().add(image);


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(dlg);
        controller.setParams(params);//до открытия окна в show, можно устанавливать любые параметры
        controller.onCompletedInitialization();

        Scene scene = new Scene(root);
        if(cssResourcePath!="")scene.getStylesheets().add(cssResourcePath);
        dlg.setScene(scene);
        dlg.setTitle(title);
        dlg.setResizable(resizeble);
        if (minH != 0) dlg.setMinHeight(minH);
        if (minW != 0) dlg.setMinWidth(minW);
        if (maxH != 0) dlg.setMaxHeight(maxH);
        if (maxW != 0) dlg.setMaxWidth(maxW);

        dlg.show();


    }


    /**
     * Корректирует некоторые элементы дизайна при изменении формы окна
     *
     * @param scene
     */
    private void sceneResizeListner(@NotNull Scene scene) {
        scene.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
            System.out.println("Width: " + newSceneWidth);
        });
        scene.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
            System.out.println("Height: " + newSceneHeight);
        });
    }


    /**
     * Метод который есть у каждого контроллера. Он уже инициализирован своим окном
     *
     * @param fxml
     * @param content
     * @param params
     * @return
     * @throws Exception
     */
    @NotNull
    public Initializable replaceContent(@NotNull String fxml, @NotNull AnchorPane content, Object... params) throws Exception {


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(this.window);
        controller.setParams(params);//до открытия окна в show можно устанавливать любые параметры
        controller.onCompletedInitialization();

        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        ObservableList<Node> children = content.getChildren();
        children.clear();
        children.add(root);
        if(controller==null) throw new RuntimeException("Controller must be not null");
        return (Initializable) controller;
    }

    /**
     * Метод который есть у каждого контроллера. Он уже инициализирован своим окном
     *
     * @param fxml
     * @param content
     * @param params
     * @return
     * @throws Exception
     */
    @NotNull
    public Initializable addContentHBox(@NotNull String fxml, @NotNull HBox content, Object... params) throws Exception {


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(this.window);
        controller.setParams(params);//до открытия окна в show можно устанавливать любые параметры
        controller.onCompletedInitialization();

        ObservableList<Node> children = content.getChildren();
        children.add(root);
        if(controller==null) throw new RuntimeException("Controller must be not null");
        return (Initializable) controller;

    }

    @NotNull
    public Initializable replaceContentScrollPane(@NotNull String fxml, @NotNull ScrollPane scrlbr, Object... params) throws Exception {


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(this.window);
        controller.setParams(params);//до открытия окна в show можно устанавливать любые параметры
        controller.onCompletedInitialization();

        scrlbr.setContent(root);

        if(controller==null) throw new RuntimeException("Controller must be not null");
        return (Initializable) controller;


    }


    /**
     * Метод должен быть заменен реализайией. Он позволяет получать необходимые параметры при инициализации конкретного
     * экземпляра контроллера
     *
     * @param params
     */
    public abstract void setParams(@NotNull Object... params);

    @NotNull
    public static void showInfoDialog(@NotNull String title,@NotNull String header,@NotNull String content,@NotNull Window owner,@NotNull Modality modal) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);
        alert.showAndWait();
    }

    public static void showInfoDialogNoHeader(@NotNull String title, @NotNull String content,@NotNull  Window owner, @NotNull Modality modal) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.showAndWait();
    }

    public static void showWarningDialog(@NotNull String title,@NotNull String header,@NotNull String content,@NotNull Window owner,@NotNull Modality modal) {

        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.showAndWait();
    }

    public static void showErrorDialog(@NotNull String title,@NotNull String header,@NotNull String content, @NotNull Window owner, @NotNull Modality modal) {

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.showAndWait();
    }

    public static void showExceptionDialog(@NotNull String title, @NotNull String header, @NotNull String content,@NotNull  Exception ex, @NotNull Window owner, Modality modal) {

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    public static ButtonType okButtonType = null;
    public static ButtonType noButtonType = null;

    /**
     * @param title
     * @param header
     * @param content
     * @return okButtonType или noButtonType
     */
    @NotNull
    public static Optional<ButtonType> showConfirmationDialog(@NotNull String title,@NotNull  String header,@NotNull  String content, @NotNull Window owner, @NotNull Modality modal) {
        if (okButtonType == null) {
            //инициализация при первом использовании
            okButtonType = new ButtonType(getApp().getResources().getString("app.yes"));
            noButtonType = new ButtonType(getApp().getResources().getString("app.no"));

        }
        ButtonType cancellButtonType = new ButtonType(getApp().getResources().getString("app.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(AlertType.CONFIRMATION);

        //alert.getDialogPane().getButtonTypes().clear();
        alert.getDialogPane().getButtonTypes().setAll(cancellButtonType, okButtonType, noButtonType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        //чтобы на enter срабатовала
        Button okButton = (Button) alert.getDialogPane().lookupButton(okButtonType);
        okButton.setDefaultButton(true);


        Optional<ButtonType> result = alert.showAndWait();
        return result;
    }

    /**
     * показывает сообщение при этом приостанавливает программу.
     *
     * @param title
     * @param header
     * @param content
     * @return ButtonType.OK или ButtonType.CANCEL
     */
    @NotNull
    public static Optional<ButtonType> showInfoConfirmDialog(@NotNull String title,@NotNull String header,@NotNull String content,@NotNull Window owner,@NotNull Modality modal) {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.YES);

        Optional<ButtonType> result = alert.showAndWait();
        return result;
    }

    /**
     * @param title
     * @param header
     * @param content
     * @param initText
     * @return вернет строку или исходную строку если жали отмену
     */
    @NotNull
    public static String showTextInputDialog(@NotNull String title, @NotNull String header, @NotNull String content, @NotNull String initText, @NotNull Window owner, @NotNull Modality modal) {
        if (initText == null) initText = "";
        TextInputDialog dialog = new TextInputDialog(initText);

        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.initOwner(owner);
        dialog.initModality(modal);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) return result.get();
        else return initText;
    }


    /**
     * @param title
     * @param header
     * @param content
     * @param choiceList
     * @param defaultChoice
     * @return Вернет выбранныую строку или null если отмена
     */
    @NotNull
    public static String showChoiceDialog(@NotNull String title,@NotNull  String header,@NotNull String content,@NotNull List<String> choiceList, @NotNull String defaultChoice, @NotNull Window owner, @NotNull Modality modal) {

        if (!choiceList.contains(defaultChoice)) defaultChoice = choiceList.get(0);

        ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, choiceList);

        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.initOwner(owner);
        dialog.initModality(modal);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) return result.get();
        else return null;
    }


    /**
     * Удалит спец символы и всякие ковычки итп
     *
     * @param text
     * @return
     */
    @NotNull
    public static String replaceSpecial(@NotNull String text) {


        return text.replaceAll("[\\\\!\"#$%&()*+',./:;<=>?@\\[\\]^_{|}~]", "");


    }

    /**
     * Удалит спец символы и всякие ковычки итп
     *
     * @param text
     * @param regexp регулярное выражение соответствующее удаляемым символам
     * @return
     */
    @NotNull
    public static String replaceSpecial(@NotNull String text, @NotNull String regexp) {


        return text.replaceAll(regexp, "");


    }


    public static boolean muchSpecials(@NotNull String text, @NotNull String regexp) {

        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(text);
        return m.find();
    }


    public static boolean muchSpecials(@NotNull String text) {
        Pattern p = Pattern.compile("[\\\\!\"#$%&()*+',./:;<=>?@\\[\\]^_{|}~]");
        Matcher m = p.matcher(text);
        return m.find();

    }

    /**
     * Уменьшает время отклика Tooltip в мс
     * @param tooltip
     */
    public static void hackTooltipStartTiming(Tooltip tooltip,int startDelay,int hideDelay) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(startDelay)));

            ////

            Field fieldTimer1 = objBehavior.getClass().getDeclaredField("hideTimer");
            fieldTimer1.setAccessible(true);
            Timeline objTimer1 = (Timeline) fieldTimer1.get(objBehavior);

            objTimer1.getKeyFrames().clear();
            objTimer1.getKeyFrames().add(new KeyFrame(new Duration(hideDelay)));


        } catch (Exception e) {
            log.error("",e);
        }
    }

}

