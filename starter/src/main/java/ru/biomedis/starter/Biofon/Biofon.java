package ru.biomedis.starter.Biofon;


import java.util.Arrays;
import org.hid4java.HidDevice;
import ru.biomedis.starter.USB.ByteHelper;
import ru.biomedis.starter.USB.USBHelper;
import ru.biomedis.starter.USB.USBHelper.USBException;


public class Biofon
{
    public static final  int vendorId=0xfc58;
    public static final  int productId=0x0001;
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

        HidDevice device=null;
        BiofonBinaryFile biofonBinaryFile=null;
        try {
            device = USBHelper.openDevice(productId, vendorId);
            byte[] commandRead = new byte[DATA_PACKET_SIZE];
            commandRead[0]=READ_COMMAND;
            USBHelper.write(device, commandRead);

            //читаем
            byte[] bytes2 = new byte[DATA_PACKET_SIZE];
            USBHelper.read(device, bytes2, REQUEST_TIMEOUT_MS);


            if(debug) System.out.println(ByteHelper.bytesToHex(bytes2,DATA_PACKET_SIZE,' '));

            int size = ByteHelper.byteArray2ToInt(bytes2,1, ByteHelper.ByteOrder.BIG_TO_SMALL);
            if(debug) System.out.println("Size = "+ size);



            int packets = (int)Math.ceil(size / DATA_PACKET_SIZE);
            if(debug) System.out.println("packets = "+packets);
            if(debug) System.out.println("___________________");

            byte[] deviceData = new byte[DATA_PACKET_SIZE*packets];
            byte[]  data = new byte[DATA_PACKET_SIZE];
            int realReading;
            for(int i=0;i<packets;i++){

                USBHelper.write(device,commandRead);
                //читаем
                realReading = USBHelper.read(device, data, REQUEST_TIMEOUT_MS);
                if(realReading<DATA_PACKET_SIZE) throw new Exception("Прочитанный пакет меньше "+DATA_PACKET_SIZE);
                copyToBuffer(deviceData,data, realReading, i*DATA_PACKET_SIZE);

                if(debug)System.out.println("Packet: "+i);


            }
            if(debug) System.out.println(ByteHelper.bytesToHex(deviceData,16,' '));
            if(debug) System.out.print("Parse data...");



            biofonBinaryFile = new BiofonBinaryFile(deviceData);

        } catch (BiofonBinaryFile.FileParseException e) {
            throw new ReadFromDeviceException(e);
        } catch (USBHelper.USBException e) {
            throw new ReadFromDeviceException(e);
        }
        catch (Exception e){
            throw new ReadFromDeviceException(e);
        }finally {

            try {
                USBHelper.closeDevice(device);
            } catch (USBHelper.USBException e) {

            }
        }

        return biofonBinaryFile;



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
    /**
     * Запись комплексов
     * @param data
     */
    public static void writeToDevice(BiofonBinaryFile data, boolean debug) throws BiofonBinaryFile.MaxBytesBoundException, BiofonComplex.ZeroCountProgramBoundException, WriteToDeviceException {

        byte[] dataToWrite = data.getData();
        writeToDevice(dataToWrite,debug);


    }


    private static void writeToDevice(byte[] dataToWrite, boolean debug) throws WriteToDeviceException {
        HidDevice device=null;
        try{


            device = USBHelper.openDevice(productId, vendorId);
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=WRITE_COMMAND;

            byte[] lenBytes = ByteHelper.intTo2ByteArray(dataToWrite.length, ByteHelper.ByteOrder.BIG_TO_SMALL);
            commandWrite[1]=lenBytes[0];
            commandWrite[2]=lenBytes[1];
            if(debug) printPacket("Write command",commandWrite);

            //команда на запись
            USBHelper.write(device,commandWrite);
            byte[] writeResponse =  new byte[DATA_PACKET_SIZE];
            USBHelper.read(device, writeResponse, REQUEST_TIMEOUT_MS);
            if(debug) printPacket("Response",writeResponse);
            if(debug)System.out.println("Размер записи "+dataToWrite.length/DATA_PACKET_SIZE);
            if(debug) printPacket("Writing data",dataToWrite);
            //запись всего пакета в прибор по 64 байта. Нужно не забыть проверять ответ и статус записи, чтобы отловить ошибки
            for(int i=0;i < dataToWrite.length/DATA_PACKET_SIZE;i++){

                USBHelper.write(device, Arrays.copyOfRange(dataToWrite,DATA_PACKET_SIZE*i,DATA_PACKET_SIZE*i+DATA_PACKET_SIZE));

                USBHelper.read(device, writeResponse, REQUEST_TIMEOUT_MS);
                if(debug) printPacket("Response",writeResponse);
            }

        } catch (USBHelper.USBException e) {
            throw new WriteToDeviceException(e);
        }finally {
            try {
                USBHelper.closeDevice(device);
            } catch (USBHelper.USBException e) {

            }
        }
    }

    /**
     * Очистка устройства
     */
    public static void clearDevice() throws WriteToDeviceException {
        HidDevice device=null;
        BiofonBinaryFile biofonBinaryFile=null;
        try {
            device = USBHelper.openDevice(productId, vendorId);
            byte[] commandRead = new byte[DATA_PACKET_SIZE];
            commandRead[0]=READ_COMMAND;
            USBHelper.write(device, commandRead);

        } catch (USBException e) {
           throw new  WriteToDeviceException(e);
        } finally {

            try {
                USBHelper.closeDevice(device);
            } catch (USBHelper.USBException e) {

            }
        }
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

    private static void printPacket(String name, byte[] packet){
        System.out.println(name+" = ");

        for (int i = 0; i < packet.length; i++) {

            System.out.print((packet[i] < 0 ? packet[i] + 256 : packet[i]) + ", ");

        }
        System.out.println();
    }
}
