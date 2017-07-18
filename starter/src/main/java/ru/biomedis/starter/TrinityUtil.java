package ru.biomedis.starter;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.usb4java.Device;
import ru.biomedis.starter.USB.PlugDeviceListener;
import ru.biomedis.starter.USB.USBHelper;
import ru.biomedis.starter.m2.M2;
import ru.biomedis.starter.m2.M2BinaryFile;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;


public class TrinityUtil extends BaseController {
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
             plugDeviceListener = new PlugDeviceListener() {
                @Override
                public void onAttachDevice() {
                    System.out.println("Device connected");
                    m2Connected.set(true);
                }

                @Override
                public void onDetachDevice() {
                    System.out.println("Device disconnected");
                    m2Connected.set(false);
                }
            };
            USBHelper.initContext();
            USBHelper.addPlugEventHandler(M2.productId, M2.vendorId,plugDeviceListener );
            USBHelper.startHotPlugListener(4);

        } catch (USBHelper.USBException e) {
            e.printStackTrace();
        }
    }

    private void buttonBinding() {
        clearBtn.disableProperty().bind(m2Connected.not());
        infoBtn.disableProperty().bind(m2Connected.not());
        readBtn.disableProperty().bind(m2Connected.not());
    }

    private void closeAction() {
        getControllerWindow().setOnCloseRequest(event -> {
            System.setErr(err);
            System.setOut(out);
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
        onClearText();
        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                    M2.clearDevice(true);
                    return null;

            }
        };
        task.setOnScheduled(event -> Platform.runLater(() -> Waiter.openLayer(getControllerWindow(),true)));
        task.setOnSucceeded(event -> {
            Waiter.closeLayer();
            System.out.println("OK!");
        });
        task.setOnFailed(event -> {
            Waiter.closeLayer();
            event.getSource().getException().printStackTrace();
        });
        Thread t=new Thread(task);
        t.setDaemon(true);
        t.start();

    }

    public void onInfo(ActionEvent actionEvent) {
        onClearText();
        try {
            String name = M2.readDeviceName(true);
            System.out.println("\n");
            System.out.println("Device info: "+name);
            System.out.println("\n");
            Device device = USBHelper.findDevice(M2.vendorId, M2.productId);
            if(device==null) throw new Exception("Device not found!");
            USBHelper.dumpDevice(device);
        } catch (M2.WriteToDeviceException e) {
            e.printStackTrace();
        } catch (USBHelper.USBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onClearText() {
        UtilPrintStream out = (UtilPrintStream) System.out;
        out.clear();

    }

    public void onReadDevice(ActionEvent actionEvent) {
        onClearText();
        try {
            M2BinaryFile m2BinaryFile = M2.readFromDevice(true);
            System.out.println(m2BinaryFile);
        } catch (M2.ReadFromDeviceException e) {
            e.printStackTrace();
        }
    }


    public static class UtilPrintStream extends PrintStream
    {
        private final TextArea area;
        private static StringBuilder strb=new StringBuilder();

        public void clear(){
            area.setText("");
            strb=new StringBuilder();
        }

        public UtilPrintStream(OutputStream out, TextArea area) {
            super(out);
            this.area = area;
        }

        @Override
        public void println(String x) {
            strb.append("\n").append(x);
            area.setText(strb.toString());
        }
        @Override
        public void print(String x) {
            strb.append(x);
            area.setText(strb.toString());
        }
        @Override
        public void println(Object x) {
            strb.append("\n").append(x.toString());
            area.setText(strb.toString());
        }
        @Override
        public void print(Object x) {
            strb.append(x.toString());
            area.setText(strb.toString());
        }

    }
}
