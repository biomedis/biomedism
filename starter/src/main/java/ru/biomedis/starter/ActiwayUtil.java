package ru.biomedis.starter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hid4java.HidDevice;
import ru.biomedis.starter.Biofon.Biofon;
import ru.biomedis.starter.Biofon.BiofonBinaryFile;
import ru.biomedis.starter.Biofon.BiofonComplex;
import ru.biomedis.starter.Biofon.BiofonProgram;
import ru.biomedis.starter.USB.PlugDeviceListener;
import ru.biomedis.starter.USB.USBHelper;
import ru.biomedis.starter.m2.LanguageDevice;
import ru.biomedis.starter.m2.M2;
import ru.biomedis.starter.m2.M2BinaryFile;
import ru.biomedis.starter.m2.Test;


public class ActiwayUtil extends BaseController {

    private @FXML  Button  clearBtn;
    private @FXML  TextArea outputArea;
    private final PrintStream out = System.out;
    private final PrintStream err = System.err;

    private BooleanProperty actiwayConnected = new SimpleBooleanProperty(false);

    private  PlugDeviceListener plugDeviceListener;
    private ResourceBundle res;

    @Override
    protected void onCompletedInitialise() {
        closeAction();
        usbInit();
        resizeFix();

    }

    private void resizeFix() {
        getControllerWindow().setWidth(getControllerWindow().getWidth()+10);
    }


    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        buttonBinding();
        initStream();

    }
    private void usbInit() {
        try {

            USBHelper.initContext();

             plugDeviceListener = new PlugDeviceListener() {
                @Override
                public void onAttachDevice(HidDevice device) {
                    System.out.println("Device connected");
                    actiwayConnected.set(true);
                }

                @Override
                public void onDetachDevice(HidDevice device) {
                    System.out.println("Device disconnected");
                    actiwayConnected.set(false);
                }

                 @Override
                 public void onFailure(USBHelper.USBException e) {
                     Log.logger.error(e);
                     showExceptionDialog("USB подключение","Ошибка!","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);

                 }
            };

            USBHelper.addPlugEventHandler(Biofon.productId, Biofon.vendorId,plugDeviceListener );
            USBHelper.startHotPlugListener();

        } catch (USBHelper.USBException e) {
            e.printStackTrace();
        }
    }

    private void buttonBinding() {
        clearBtn.disableProperty().bind(actiwayConnected.not());

    }

    private void closeAction() {
        getControllerWindow().setOnCloseRequest(event -> {
            System.setErr(err);
            System.setOut(out);
            try {
                USBHelper.stopHotPlugListener();
            } catch (USBHelper.USBException e) {
                Log.logger.error(e);
            }
            USBHelper.removePlugEventHandler(plugDeviceListener);
            USBHelper.closeContext();
        });
    }

    private void initStream() {
        UtilPrintStream newOutStream = new UtilPrintStream(System.out,outputArea);
        System.setOut(newOutStream);

        UtilPrintStream newErrStream = new UtilPrintStream(System.err,outputArea);
        System.setErr(newErrStream);

    }

    public void onClear() {

        showInfoDialog(
            res.getString("app.menu.clear"),
            res.getString("app.device_will_be_creared"),
            res.getString("app.device_will_bee_cleared_info"),
            getControllerWindow(),
            Modality.WINDOW_MODAL);
        System.out.println("-------- Стирание  устройства/Clearing device ---------");
        //onClearText();
        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                    //Biofon.clearDevice();
                BiofonProgram p = new BiofonProgram(Arrays.asList(1.0,2.0,3.0,4.0,5.0), 1);
                BiofonComplex c = new BiofonComplex(10, 100);
                c.addProgram(p);
                c.addProgram(p);
               Biofon.writeToDevice(new BiofonBinaryFile(c,c,c), true);
                    return null;

            }
        };
        Stage stage =new Stage(StageStyle.TRANSPARENT);
        task.setOnScheduled( event-> openWaiter(stage));
        task.setOnSucceeded(event -> {
            closeWaiter(stage);
            System.out.println("OK!");
            System.out.println("---------------------------------------\n");
        });
        task.setOnFailed(event -> {
            closeWaiter(stage);
            System.out.println("Ошибка в процессе стирания устройства");
            event.getSource().getException().printStackTrace();
            System.out.println("---------------------------------------\n");
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();






    }

    private void closeWaiter(Stage stage) {
        Platform.runLater(stage::close);
    }

    private void openWaiter(Stage stage) {
        try {
            openDialog(stage, getControllerWindow(),"/fxml/waiter.fxml","",false,0,0,0,0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void onClearText() {
        UtilPrintStream out = (UtilPrintStream) System.out;
        out.clear();

    }

    public static class UtilPrintStream extends PrintStream
    {
        private  final TextArea area;

        public void clear(){
            area.setText("");
        }

        public UtilPrintStream(OutputStream out, TextArea area) {
            super(out);
            this.area = area;
        }

        @Override
        public void println(String x) {
           addText(x,"\n");
        }

        @Override
        public void print(String x) {
            addText(x,"");
        }

        @Override
        public void println(Object x) {
            addText(x.toString(),"\n");

        }

        @Override
        public void print(Object x) {
            addText(x.toString(),"");
        }

        private void addText(String text, String prefix){
            Platform.runLater(() -> {
                if(prefix.isEmpty())area.appendText(text);
                else area.appendText(prefix+text);
                area.setScrollTop(Double.MAX_VALUE);
                area.setScrollLeft(0);
            });
        }

    }
}
