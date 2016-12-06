package ru.biomedis.biomedismair3.Biofon;



import ru.biomedis.biomedismair3.utils.USB.ByteHelper;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class Biofon
{
    public static final  short vendorId=(short)0xfc58;
    public static final  short productId=(short)0x0001;
    private static final  byte IN_END_POINT=(byte)0x81;
    private static final  byte OUT_END_POINT=(byte)0x01;

    private static final  byte READ_COMMAND = 0x34;
    private static final  byte WRITE_COMMAND = 0x33;
    private static final  int REQUEST_TIMEOUT_MS = 10000;
    private static final  int DATA_PACKET_SIZE=64;




    /**
     * Чтение комплексов с прибора
     * @return
     */
    public static BiofonBinaryFile readFromDevice(final boolean debug) throws ReadFromDeviceException {

        USBHelper.USBDeviceHandle usbDeviceHandle=null;
        BiofonBinaryFile biofonBinaryFile=null;
        try {
         usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
        byte[] commandRead = new byte[DATA_PACKET_SIZE];
        commandRead[0]=READ_COMMAND;
        USBHelper.write(usbDeviceHandle,commandRead,OUT_END_POINT,REQUEST_TIMEOUT_MS);

        //читаем
        ByteBuffer data = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, REQUEST_TIMEOUT_MS);
        data.position(0);
        byte[] bytes2 = new byte[DATA_PACKET_SIZE];
        data.get(bytes2);

        if(debug) System.out.println(ByteHelper.bytesToHex(bytes2,DATA_PACKET_SIZE,' '));

        data.position(1);
        int size = data.getChar();
        if(debug) System.out.println("Size = "+ size);
        data.position(1);



        int packets = (int)Math.ceil(size / DATA_PACKET_SIZE);
        if(debug) System.out.println("packets = "+packets);
        if(debug) System.out.println("___________________");

        byte[] deviceData = new byte[DATA_PACKET_SIZE*packets];
        for(int i=0;i<packets;i++){

            USBHelper.write(usbDeviceHandle,commandRead,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            //читаем
            data = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, REQUEST_TIMEOUT_MS);
            data.position(0);

            data.get(deviceData,i*DATA_PACKET_SIZE,DATA_PACKET_SIZE);

        }
        if(debug) System.out.println(ByteHelper.bytesToHex(deviceData,16,' '));
        if(debug) System.out.print("Parse data...");



             biofonBinaryFile = new BiofonBinaryFile(deviceData);

        } catch (BiofonBinaryFile.FileParseException e) {
           throw new ReadFromDeviceException(e);
        } catch (USBHelper.USBException e) {
            throw new ReadFromDeviceException(e);
        } finally {

            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
            } catch (USBHelper.USBException e) {

            }
        }

        return biofonBinaryFile;



    }

    /**
     * Запись комплексов
     * @param data
     */
    public static void writeToDevice(BiofonBinaryFile data) throws BiofonBinaryFile.MaxBytesBoundException, BiofonComplex.ZeroCountProgramBoundException, WriteToDeviceException {

        byte[] dataToWrite = data.getData();
        writeToDevice(dataToWrite);


    }


    private static void writeToDevice(byte[] dataToWrite) throws WriteToDeviceException {
        USBHelper.USBDeviceHandle usbDeviceHandle=null;
        try{


             usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=WRITE_COMMAND;

            byte[] lenBytes = ByteHelper.intTo2ByteArray(dataToWrite.length, ByteHelper.ByteOrder.BIG_TO_SMALL);
            commandWrite[1]=lenBytes[0];
            commandWrite[2]=lenBytes[1];

            //команда на запись
            USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            ByteBuffer writeResponse = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, REQUEST_TIMEOUT_MS);

            //запись всего пакета в прибор по 64 байта. Нужно не забыть проверять ответ и статус записи, чтобы отловить ошибки
            for(int i=0;i < dataToWrite.length/DATA_PACKET_SIZE;i++){

                USBHelper.write(usbDeviceHandle, Arrays.copyOfRange(dataToWrite,DATA_PACKET_SIZE*i,DATA_PACKET_SIZE*i+DATA_PACKET_SIZE),OUT_END_POINT,REQUEST_TIMEOUT_MS);            //читаем
                writeResponse = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, REQUEST_TIMEOUT_MS);
            }

        } catch (USBHelper.USBException e) {
           throw new WriteToDeviceException(e);
        }finally {
            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
            } catch (USBHelper.USBException e) {

            }
        }
    }

    /**
     * Очистка устройства
     */
    public static void clearDevice() throws WriteToDeviceException {
        writeToDevice(new byte[BiofonBinaryFile.MAX_FILE_BYTES]);
    }



    public static class ReadFromDeviceException extends Exception{
        public ReadFromDeviceException(Throwable cause) {
            super(cause);
        }
    }
    public static class WriteToDeviceException extends Exception{
        public WriteToDeviceException(Throwable cause) {
            super(cause);
        }
    }
}
