package ru.biomedis.starter;

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
import ru.biomedis.starter.USB.PlugDeviceListener;
import ru.biomedis.starter.USB.USBHelper;
import ru.biomedis.starter.m2.LanguageDevice;
import ru.biomedis.starter.m2.M2;
import ru.biomedis.starter.m2.M2BinaryFile;
import ru.biomedis.starter.m2.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;


public class TrinityUtil extends BaseController {
    public @FXML  Button writeBtn;
    private @FXML  Button  clearBtn;
    private @FXML  Button  infoBtn;
    private @FXML  Button  readBtn;
    private @FXML  TextArea outputArea;
    private final PrintStream out = System.out;
    private final PrintStream err = System.err;

    private BooleanProperty m2Connected = new SimpleBooleanProperty(false);

    private  PlugDeviceListener plugDeviceListener;
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
                    m2Connected.set(true);
                }

                @Override
                public void onDetachDevice(HidDevice device) {
                    System.out.println("Device disconnected");
                    m2Connected.set(false);
                }

                 @Override
                 public void onFailure(USBHelper.USBException e) {
                     Log.logger.error(e);
                     showExceptionDialog("USB подключение","Ошибка!","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);

                 }
            };

            USBHelper.addPlugEventHandler(M2.productId, M2.vendorId,plugDeviceListener );
            USBHelper.startHotPlugListener();

        } catch (USBHelper.USBException e) {
            e.printStackTrace();
        }
    }

    private void buttonBinding() {
        clearBtn.disableProperty().bind(m2Connected.not());
        infoBtn.disableProperty().bind(m2Connected.not());
        readBtn.disableProperty().bind(m2Connected.not());
        writeBtn.disableProperty().bind(m2Connected.not());
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

    public void onClear(ActionEvent actionEvent) {


        System.out.println("-------- Стирание  устройства/Clearing device ---------");
        //onClearText();
        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                    M2.clearDevice(true);
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

    public void onInfo(ActionEvent actionEvent) {
        System.out.println("-------- Чтение информации с устройства/Reading information about device ---------");
        //onClearText();
        try {
            String name = M2.readDeviceName(true);
            System.out.println("\n");
            System.out.println("Device info: "+name);
            System.out.println("\n");

            HidDevice device = USBHelper.findDevice(M2.vendorId, M2.productId);
            if(device==null) throw new Exception("Устройство не обнаружено хотя присутствует на шине USB/ Device not found!");
            USBHelper.dumpDevice(device);
        } catch (M2.WriteToDeviceException e) {
            System.out.println("USB ошибка в процессе отправки команды чтения");
            System.out.println(e.getMessage());
            //e.printStackTrace();
        } catch (USBHelper.USBException e) {
            System.out.println("USB ошибка в процессе выполнения операций");
           // e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка в процессе выполнения операций: "+e.getMessage());
            //e.printStackTrace();
            System.out.println(e.getMessage());
        }
        System.out.println("---------------------------------------\n");
    }

    public void onClearText() {
        UtilPrintStream out = (UtilPrintStream) System.out;
        out.clear();

    }

    public void onReadDevice(ActionEvent actionEvent) {

        System.out.println("-------- Чтение с устройства/Reading device ---------");
        //onClearText();
        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                M2BinaryFile m2BinaryFile = M2.readFromDevice(true);
                System.out.println(m2BinaryFile);
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
            System.out.println("Ошибка в процессе чтения устройства:"+ event.getSource().getMessage());
            event.getSource().getException().printStackTrace();
            System.out.println("---------------------------------------\n");
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();


    }

    public void onWriteDevice(ActionEvent actionEvent) {


        System.out.println("-------- Запись тестовых данных устройства/Writing test data to device ---------");
        //onClearText();
        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                M2BinaryFile m2BinaryFile = Test.testData();
                M2.writeToDevice(m2BinaryFile, LanguageDevice.getDeviceLang("en").getDeviceLangID(),true);
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
            System.out.println("Ошибка в процессе записи устройства:"+ event.getSource().getMessage());
            event.getSource().getException().printStackTrace();
            System.out.println("---------------------------------------\n");
        });

        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();


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
