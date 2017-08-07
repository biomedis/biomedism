package ru.biomedis.biomedismair3.Layouts.ProgressPanel;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ru.biomedis.biomedismair3.BaseController;

import java.net.URL;
import java.util.ResourceBundle;

public class ProgressPanelController extends BaseController implements ProgressAPI{

    @FXML private VBox progress1Pane;
    @FXML private VBox progress2Pane;
    @FXML private VBox  progress3Pane;
    @FXML private Label messageText;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressIndicatorLabel;
    @FXML private ProgressBar progressAction;
    @FXML private Label textInfo;
    @FXML private Label textActionInfo;

    @Override
    protected void onCompletedInitialise() {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progress1Pane.setVisible(false);
        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
    }


    /**
     * Отобразит строку информации
     * @param message
     */
    @Override
    public void setInfoMessage(String message)
    {
        progress1Pane.setVisible(false);
        progress2Pane.setVisible(false);
        progress3Pane.setVisible(true);

        messageText.setText(message);

        FadeTransition fadeTransition;
        fadeTransition = new FadeTransition(Duration.seconds(6), progress3Pane);
        fadeTransition.setFromValue(1);
        fadeTransition.setDelay(Duration.seconds(3));
        fadeTransition.setToValue(0.01);
        fadeTransition.setOnFinished(event -> {
            progress3Pane.setVisible(false);
            progress3Pane.setOpacity(1.0);
            Platform.runLater(() -> fadeTransition.setOnFinished(null));
        });
        fadeTransition.play();

    }

    /**
     *
     * @param value 0 - 1.0
     * @param textAction ниже textInfo
     * @param textInfo вверху
     */
    @Override
    public void setProgressBar(double value,String textAction,String textInfo)
    {
        progress1Pane.setVisible(false);
        progress2Pane.setVisible(true);
        progress3Pane.setVisible(false);

        if(value>1.0)value=1.0;
        progressAction.setProgress(value);
        textActionInfo.setText(textAction);
        this.textInfo.setText(textInfo);

        if(value==1.0)
        {
            progressIndicatorLabel.setText("");
            Text doneText = (Text) progressIndicator.lookup(".percentage");
            if(doneText!=null)doneText.setText(textAction);
        }




    }

    @Override
    public void hideProgressBar(boolean animation)
    {
        if(animation)
        {

            FadeTransition fadeTransition;
            fadeTransition = new FadeTransition(Duration.seconds(4), progress2Pane);
            fadeTransition.setFromValue(1);
            fadeTransition.setDelay(Duration.seconds(3));
            fadeTransition.setToValue(0.01);
            fadeTransition.setOnFinished(event -> {
                progress2Pane.setVisible(false);
                progress2Pane.setOpacity(1.0);
                Platform.runLater(() -> fadeTransition.setOnFinished(null) );
            });
            fadeTransition.play();

        }else progress2Pane.setVisible(false);




    }




    /**
     * Установит значение прогресса и текст, сделает все видимым
     * @param value
     * @param text
     */
    @Override
    public void setProgressIndicator(double value,String text)
    {

        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
        progress1Pane.setVisible(true);
        if(value>1.0)value=1.0;
        progressIndicator.setProgress(value);
        progressIndicatorLabel.setText(text);
        if(value==1.0)
        {
            progressIndicatorLabel.setText("");
            Text doneText = (Text) progressIndicator.lookup(".percentage");
            doneText.setText(text);
        }

    }

    @Override
    public void setProgressIndicator(double value)
    {
        System.out.println(value);

        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
        progress1Pane.setVisible(true);
        if(value>1.0)value=1.0;

        progressIndicator.setProgress(value);


        if (value==1.0)
        {
            progressIndicatorLabel.setText("");
            Text doneText = (Text) progressIndicator.lookup(".percentage");
            doneText.setText(progressIndicatorLabel.getText());
        }

    }
    /**
     * Установит неопределенное значение прогресса и текст. Все сделает видимым
     * @param text
     */
    @Override
    public void setProgressIndicator(String text)
    {
        progressIndicator.setVisible(true);
        progress2Pane.setVisible(false);
        progress3Pane.setVisible(false);
        progress1Pane.setVisible(true);
        progressIndicator.setProgress(-0.5);
        progressIndicatorLabel.setText(text);

    }

    /**
     * Скрывает круговой индикатор прогресса
     */
    @Override
    public void hideProgressIndicator(boolean animation)
    {
        if(animation)
        {

            FadeTransition fadeTransition;
            fadeTransition = new FadeTransition(Duration.seconds(4), progress1Pane);
            fadeTransition.setFromValue(1);
            fadeTransition.setDelay(Duration.seconds(3));
            fadeTransition.setToValue(0.01);
            fadeTransition.setOnFinished(event -> {
                progress1Pane.setVisible(false);
                progress1Pane.setOpacity(1.0);
                Platform.runLater(() -> fadeTransition.setOnFinished(null));
            });
            fadeTransition.play();

        }else progress1Pane.setVisible(false);


    }
}
