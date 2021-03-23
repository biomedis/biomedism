package ru.biomedis.starter.m2;

import org.hid4java.HidDevice;
import ru.biomedis.starter.USB.ByteHelper;
import ru.biomedis.starter.USB.USBHelper;

import java.util.Arrays;

public class M2
{
    public static final  int vendorId=0xFC82;
    public static final  int productId=0x0001;
    private static final  byte IN_END_POINT=(byte)0x81;
    private static final  byte OUT_END_POINT=(byte)0x01;

    private static final  byte READ_COMMAND = 0x34;
    private static final  byte WRITE_COMMAND = 0x33;
    private static final  byte READ_DEVICE_NAME = 50;
    private static final  byte CLEAR_COMMAND = 53;
    private static final  byte LANG_LIST_COMMAND = 54;
    private static final  int REQUEST_TIMEOUT_MS = 10000;
    private static final  int DATA_PACKET_SIZE=64;
    public static final int PAUSE_BETWEEN_PROGRAM=5;



    /**
     * Чтение комплексов с прибора
     * @return
     */
    public static M2BinaryFile readFromDevice(final boolean debug) throws ReadFromDeviceException {

        HidDevice device = null;
        M2BinaryFile m2BinaryFile = null;
        try {
            device = USBHelper.openDevice(productId, vendorId);
            if(device == null) throw new ReadFromDeviceException(new NullPointerException("Device == NULL"));
            byte[] commandRead = new byte[DATA_PACKET_SIZE];
            commandRead[0]=READ_COMMAND;
            if(debug)printPacket("Reading command",  commandRead);
            USBHelper.write(device,commandRead);

            Response response = readResponseBuffer(device, REQUEST_TIMEOUT_MS, debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

            int size= ByteHelper.byteArray4ToInt(response.getPayload(),0, ByteHelper.ByteOrder.BIG_TO_SMALL);
            int langID=ByteHelper.byteArray1ToInt(response.getPayload(),4);
            System.out.println("Размер посылки: "+size);

            byte[]  data = new byte[DATA_PACKET_SIZE];
            if(size==0){
                //если прибор пустой, то создадим пустой файл
                return new M2BinaryFile();

            }else if(size > M2BinaryFile.MAX_FILE_BYTES  || size < 0){
                //вычитывание левых буфферов из винды. Если пришел левый размер
                int cnt = M2BinaryFile.MAX_FILE_BYTES/64;
              /*  while(USBHelper.read(device, data, 3)!=0 && cnt > 0) {
                    cnt--;
                    System.out.println("flush.. ");
                }
                */
                try {
                    USBHelper.closeDevice(device);
                } catch (USBHelper.USBException e) {

                }
                throw new ReadFromDeviceException("Не верный размер посылки");
                //return readFromDevice(debug);
            }


            int packets = (int)Math.ceil(size / DATA_PACKET_SIZE);
            if(debug) System.out.println("packets = "+packets);
            if(debug) System.out.println("___________________");

            byte[] deviceData = new byte[DATA_PACKET_SIZE*packets];

            int realReading;
            for(int i=0;i<packets;i++){
                //читаем
                if(debug) System.out.println("Packet: "+i);
                realReading = USBHelper.read(device, data, 200);
                if(realReading<DATA_PACKET_SIZE) {
                    throw new Exception("Прочитанный "+i+" пакет меньше "+DATA_PACKET_SIZE);

                }
                copyToBuffer(deviceData,data, realReading, i*DATA_PACKET_SIZE);


            }
            if(debug) System.out.println(ByteHelper.bytesToHex(deviceData,16,' '));
            if(debug) System.out.print("Parse data...");



            m2BinaryFile = new M2BinaryFile(deviceData,langID);

        }  catch (USBHelper.USBException e) {
            e.printStackTrace();
            throw new ReadFromDeviceException(e);
        } catch (M2BinaryFile.FileParseException e) {
            e.printStackTrace();
            throw new ReadFromDeviceException(e);
        } catch (DeviceFailException e) {
            e.printStackTrace();
            throw new ReadFromDeviceException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReadFromDeviceException(e);
        } finally {

            try {
                USBHelper.closeDevice(device);
            } catch (USBHelper.USBException e) {

            }
        }

        return m2BinaryFile;

    }

    /**
     * Копирует байтовый массив размера realReading из data В deviceData в позицию inDstPosition.
     * @param deviceData
     * @param data
     * @param realReading
     * @param inDstPosition
     */
    private static void copyToBuffer(byte[] deviceData, byte[] data, int realReading, int inDstPosition) {
        for(int i=0;i<realReading;i++) deviceData[inDstPosition+i] = data[i];

    }




    public static String readDeviceName(boolean debug) throws WriteToDeviceException {
        HidDevice device=null;
        String str="";
        try{


            device = USBHelper.openDevice(productId, vendorId);
            if(device == null) throw new ReadFromDeviceException(new NullPointerException("Device == NULL"));
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=READ_DEVICE_NAME;

            //команда на запись
            USBHelper.write(device,commandWrite);

            Response response = readResponseBuffer(device, REQUEST_TIMEOUT_MS, debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

            int strSize=0;
            for(int i=0;i<response.getPayload().length;i++){
                if(response.getPayload()[i]==0){
                    strSize=i;
                    break;
                }
            }

            str= ByteHelper.byteArrayToString(response.getPayload(),0,strSize, ByteHelper.ByteOrder.BIG_TO_SMALL,"Cp1250");


        } catch (USBHelper.USBException e) {
            e.printStackTrace();
            throw new WriteToDeviceException(e);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                USBHelper.closeDevice(device);
            } catch (USBHelper.USBException e) {

            }
        }
        return str;
    }
    /**
     * Запись комплексов
     * @param data
     */
    public static void writeToDevice(M2BinaryFile data,int langID,boolean debug) throws M2BinaryFile.MaxBytesBoundException, M2Complex.ZeroCountProgramBoundException, LanguageDevice.NoLangDeviceSupported, WriteToDeviceException {

        byte[] dataToWrite = data.getData();
        writeToDevice(dataToWrite,langID,data.getComplexesList().size(),debug);


    }




    private static void writeToDevice(byte[] dataToWrite, int langID,int countComplexes,boolean debug) throws WriteToDeviceException {
        HidDevice device=null;
        clearDevice(debug);

        try{
            Thread.sleep(3000);
            System.out.println("Start writing");


            device = USBHelper.openDevice(productId, vendorId);
            if(device == null) throw new ReadFromDeviceException(new NullPointerException("Device == NULL"));
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=WRITE_COMMAND;

            byte[] lenBytes = ByteHelper.intToByteArray(dataToWrite.length, ByteHelper.ByteOrder.BIG_TO_SMALL);
            commandWrite[1]=lenBytes[0];
            commandWrite[2]=lenBytes[1];
            commandWrite[3]=lenBytes[2];
            commandWrite[4]=lenBytes[3];

            commandWrite[5]=(byte)langID;
            commandWrite[6]=(byte)countComplexes;
            int packets = (int)Math.ceil((float)dataToWrite.length/(float)DATA_PACKET_SIZE);
            if(debug) {
                if(debug)printPacket("Command Write",commandWrite);
                System.out.println("Data size=" + dataToWrite.length);
                System.out.println("Number packets =" + packets);

                System.out.println("OUT_END_POINT=" + OUT_END_POINT + " IN_END_POINT=" + IN_END_POINT);
            }

            //команда на запись
            // USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);
            //Thread.sleep(200);
            // Response response = readResponseBuffer(usbDeviceHandle,debug);
            System.out.print("Write command send..");
            USBHelper.write(device,commandWrite);


            if(debug)System.out.println("WRITE DATA...");

            //запись всего пакета в прибор по 64 байта. Нужно не забыть проверять ответ и статус записи, чтобы отловить ошибки
            for(int i=0;i < packets;i++){

                USBHelper.write(device, Arrays.copyOfRange(dataToWrite,DATA_PACKET_SIZE*i,DATA_PACKET_SIZE*i+DATA_PACKET_SIZE));

                if(debug)System.out.println("N packet =" + (i+1));
            }

        } catch (USBHelper.USBException e) {
            e.printStackTrace();
            throw new WriteToDeviceException(e);
        } catch (Exception e){
            e.printStackTrace();
            throw new WriteToDeviceException(e);
        }
        finally {
            try {
                USBHelper.closeDevice(device);
            } catch (USBHelper.USBException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Читает буффер ответа
     * @param device
     * @param timeout
     * @return Response
     * @throws USBHelper.USBException
     */
    private static Response readResponseBuffer( HidDevice device, int timeout,  boolean debug) throws USBHelper.USBException {
        if(device == null) throw new USBHelper.USBException("Device == NULL");
        if(debug)System.out.print("READ RESPONSE...");
        byte[] bytes = new byte[DATA_PACKET_SIZE];
        int read = USBHelper.read(device, bytes, timeout);
        if(read ==0) throw new USBHelper.USBException("No data reading!!!");

        //if(debug)System.out.println("Device response: "+ByteHelper.bytesToHex(bytes,64,' '));

        Response resp=new Response(bytes[0]==0?true:false,bytes[1]);
        int j=0;
        for(int i=2;i<DATA_PACKET_SIZE;i++)resp.getPayload()[j++]=bytes[i];
        if(debug)printPacket("Response",  bytes);
        return resp;
    }

    /**
     * Очистка устройства
     */
    public static void clearDevice(boolean debug) throws WriteToDeviceException {
        if(debug)System.out.print("CLEAR_DEVICE...");
        HidDevice device = null;
        try{

            device = USBHelper.openDevice(productId, vendorId);
            if(device == null) throw new ReadFromDeviceException(new NullPointerException("Device == NULL"));
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=CLEAR_COMMAND;

            if(debug)printPacket("Clear device",  commandWrite);

            Response response = request( commandWrite,  device ,debug, 20000);
            if(response.status==false) throw new DeviceFailException(response.errorCode);
            System.out.println("Device cleared");

        } catch (USBHelper.USBException e) {
            e.printStackTrace();
            throw new WriteToDeviceException(e);
        } catch (DeviceFailException e) {
            e.printStackTrace();
            throw new WriteToDeviceException(e);
        } catch (Exception e){
            e.printStackTrace();
            throw new WriteToDeviceException(e);
        }
        finally {
            try {
                System.out.println("Device closing...");
                USBHelper.closeDevice(device);
                System.out.println("Device closed");
            } catch (USBHelper.USBException e) {
                e.printStackTrace();
            }

        }
    }

    private static Response request(byte[] commandWrite,  HidDevice device , boolean debug, int timeoutRead) throws USBHelper.USBException {
        int counter=0;
        USBHelper.USBException ex=null;
        Response  response=null;
        while(counter<2){
            try {
                System.out.println("Try "+counter);
                USBHelper.write(device, commandWrite);
                response = readResponseBuffer(device,timeoutRead, debug);
                counter=3;
            }catch (USBHelper.USBException e){
                ex = e;
                System.out.println("Try not complete"+counter);
                counter++;

            }
        }
        if(response==null) throw  ex;
        return response;
    }

    public static class ReadFromDeviceException extends Exception{
        public ReadFromDeviceException(Throwable cause) {
            super(cause);
        }

        public ReadFromDeviceException(String message) {
            super(message);
        }
    }
    public static class WriteToDeviceException extends Exception{

        public WriteToDeviceException(Throwable cause) {
            super(cause);
        }
        public WriteToDeviceException() {
            super();

        }

    }


    public static class DeviceFailException extends Exception{
        private int errorCode;
        private String descr;


        public DeviceFailException(int errorCode) {
            super();
            descr=getDeviceErrorDescription(errorCode);
        }

        public String getDescr() {
            return descr;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }


    private static String getDeviceErrorDescription(int errorCode){
        String str;
        switch (errorCode){
            case 1:
                str="Error 1";
                break;
            case 2:
                str="Error 2";
                break;
            case 3:
                str="Error 3";
                break;
            case 4:
                str="Error 4";
                break;
            default:
                str="Unknown error code!";


        }
        return str;
    }

    private static class Response{
        private boolean status;
        private int errorCode;
        private final byte[] payload=new byte[DATA_PACKET_SIZE];

        public Response(boolean status, int errorCode) {
            this.status = status;
            this.errorCode = errorCode;

        }

        public byte[] getPayload() {
            return payload;
        }

    }


    private static void printPacket(String name, byte[] packet){
        System.out.println(name+" = ");

        for (int i = 0; i < packet.length; i++) {

            System.out.print((packet[i] < 0 ? packet[i] + 256 : packet[i]) + ", ");

        }
        System.out.println();
    }


}
