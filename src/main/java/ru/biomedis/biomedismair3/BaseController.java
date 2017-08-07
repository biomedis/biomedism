/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.biomedis.biomedismair3;


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


/**
 * @author Anama
 */
public abstract class BaseController implements Initializable {
    protected static App app = null;
    protected static AppController mdc = null;
    protected Stage window = null;


    public Stage getControllerWindow() {
        return window;
    }
    /**
     * Вызывается после завершиния инициализации контроллера
     */
    protected abstract void onCompletedInitialise();

    public ModelDataApp getModel() {
        return app.getModel();
    }

    public void setWindow(Stage win) {
        window = win;
    }

    /**
     * Установка базового класса приложения для доступа из контроллеров.
     *
     * @param lp
     */
    public static void setApp(App lp) {
        BaseController.app = lp;
    }

    /**
     * Установка базового контролера для доступа из других контроллеров
     *
     * @param mc
     */
    public static void setMainController(AppController mc) {
        mdc = mc;
    }


    /**
     * Возвращает объект главного класса приложения
     *
     * @return
     */
    public static App getApp() {
        return BaseController.app;
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
     * @param userData    пользовотельский объект данных, он же и вернет. Объект доступен как root.getUserData()
     * @throws IOException
     */
    public static <T> T openDialogUserData(Stage ownerWindow, String fxml, String title, boolean resizeble, StageStyle st, int minH, int minW, int maxH, int maxW, T userData, Object... params) throws IOException {
        Stage dlg = new Stage(st);
        dlg.initOwner(ownerWindow);
        dlg.initModality(Modality.WINDOW_MODAL);

        Image image = ownerWindow.getIcons().get(0);
        dlg.getIcons().clear();
        dlg.getIcons().add(image);


        WeakReference<T> userDataRef = new WeakReference<>(userData);


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(dlg);

        root.setUserData(userData);
        controller.setParams(params);//до открытия окна в show, можно устанавливать любые параметры. initialize вызывается в контроллере до этого !!
        controller.onCompletedInitialise();

        Scene scene = new Scene(root);

        dlg.setScene(scene);
        dlg.setTitle(title);
        dlg.setResizable(resizeble);
        if (minH != 0) dlg.setMinHeight(minH);
        if (minW != 0) dlg.setMinWidth(minW);
        if (maxH != 0) dlg.setMaxHeight(maxH);
        if (maxW != 0) dlg.setMaxWidth(maxW);

        dlg.showAndWait();

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
    public static void openDialog(Stage ownerWindow, String fxml, String title, boolean resizeble, StageStyle st, int minH, int minW, int maxH, int maxW, Object... params) throws IOException {
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
        controller.onCompletedInitialise();

        Scene scene = new Scene(root);

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
    public static void openDialogNotModal(Stage ownerWindow, String fxml, String title, boolean resizeble, StageStyle st, int minH, int minW, int maxH, int maxW, Object... params) throws IOException {
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
    public static void openDialogNotModal(Stage ownerWindow, String fxml, String title, boolean resizeble, StageStyle st, int minH, int minW, int maxH, int maxW,String cssResourcePath, Object... params) throws IOException {
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
        controller.onCompletedInitialise();

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
    private void sceneResizeListner(Scene scene) {
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
    public Initializable replaceContent(String fxml, AnchorPane content, Object... params) throws Exception {


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(this.window);
        controller.setParams(params);//до открытия окна в show можно устанавливать любые параметры
        controller.onCompletedInitialise();

        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        ObservableList<Node> children = content.getChildren();
        children.clear();
        children.add(root);


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
    public Initializable addContentHBox(String fxml, HBox content, Object... params) throws Exception {


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(this.window);
        controller.setParams(params);//до открытия окна в show можно устанавливать любые параметры
        controller.onCompletedInitialise();

        ObservableList<Node> children = content.getChildren();
        children.add(root);
        return (Initializable) controller;

    }

    public Initializable replaceContentScrollPane(String fxml, ScrollPane scrlbr, Object... params) throws Exception {


        URL location = app.getClass().getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader(location, app.strings);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        BaseController controller = (BaseController) fxmlLoader.getController();
        controller.setWindow(this.window);
        controller.setParams(params);//до открытия окна в show можно устанавливать любые параметры
        controller.onCompletedInitialise();

        scrlbr.setContent(root);


        return (Initializable) controller;


    }


    /**
     * Метод должен быть заменен реализайией. Он позволяет получать необходимые параметры при инициализации конкретного
     * экземпляра контроллера
     *
     * @param params
     */
    public abstract void setParams(Object... params);


    public static void showInfoDialog(String title, String header, String content, Window owner, Modality modal) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);
        alert.showAndWait();
    }

    public static void showInfoDialogNoHeader(String title, String content, Window owner, Modality modal) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.showAndWait();
    }

    public static void showWarningDialog(String title, String header, String content, Window owner, Modality modal) {

        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.showAndWait();
    }

    public static void showErrorDialog(String title, String header, String content, Window owner, Modality modal) {

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(owner);
        alert.initModality(modal);

        alert.showAndWait();
    }

    public static void showExceptionDialog(String title, String header, String content, Exception ex, Window owner, Modality modal) {

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
    public static Optional<ButtonType> showConfirmationDialog(String title, String header, String content, Window owner, Modality modal) {
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
    public static Optional<ButtonType> showInfoConfirmDialog(String title, String header, String content, Window owner, Modality modal) {

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
    public static String showTextInputDialog(String title, String header, String content, String initText, Window owner, Modality modal) {
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
    public static String showChoiceDialog(String title, String header, String content, List<String> choiceList, String defaultChoice, Window owner, Modality modal) {

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
    public static String replaceSpecial(String text) {


        return text.replaceAll("[\\\\!\"#$%&()*+',./:;<=>?@\\[\\]^_{|}~]", "");


    }

    /**
     * Удалит спец символы и всякие ковычки итп
     *
     * @param text
     * @param regexp регулярное выражение соответствующее удаляемым символам
     * @return
     */
    public static String replaceSpecial(String text, String regexp) {


        return text.replaceAll(regexp, "");


    }


    public static boolean muchSpecials(String text, String regexp) {

        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(text);
        return m.find();
    }


    public static boolean muchSpecials(String text) {
        Pattern p = Pattern.compile("[\\\\!\"#$%&()*+',./:;<=>?@\\[\\]^_{|}~]");
        Matcher m = p.matcher(text);
        return m.find();

    }

}

