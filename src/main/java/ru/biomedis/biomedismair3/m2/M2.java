package ru.biomedis.biomedismair3.m2;


import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.USB.ByteHelper;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class M2
{
    public static final  short vendorId=(short)0xFC82;
    public static final  short productId=(short)0x0001;
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



        USBHelper.USBDeviceHandle usbDeviceHandle=null;
        M2BinaryFile m2BinaryFile=null;
        try {
            usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
            byte[] commandRead = new byte[DATA_PACKET_SIZE];
            commandRead[0]=READ_COMMAND;
            USBHelper.write(usbDeviceHandle,commandRead,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            Response response = readResponseBuffer(usbDeviceHandle,debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

            int size= ByteHelper.byteArray4ToInt(response.getPayload(),0, ByteHelper.ByteOrder.BIG_TO_SMALL);
            int langID=ByteHelper.byteArray1ToInt(response.getPayload(),4);

            if(size==0){
                //если прибор пустой, то создадим пустой файл
               return new M2BinaryFile();

            }


            int packets = (int)Math.ceil(size / DATA_PACKET_SIZE);
            if(debug) System.out.println("packets = "+packets);
            if(debug) System.out.println("___________________");

            byte[] deviceData = new byte[DATA_PACKET_SIZE*packets];
            ByteBuffer  data;
            for(int i=0;i<packets;i++){

                //читаем
                data = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, REQUEST_TIMEOUT_MS);
                data.position(0);

                data.get(deviceData,i*DATA_PACKET_SIZE,DATA_PACKET_SIZE);

            }
            if(debug) System.out.println(ByteHelper.bytesToHex(deviceData,16,' '));
            if(debug) System.out.print("Parse data...");



            m2BinaryFile = new M2BinaryFile(deviceData,langID);

        }  catch (USBHelper.USBException e) {
            throw new ReadFromDeviceException(e);
        } catch (M2BinaryFile.FileParseException e) {
            throw new ReadFromDeviceException(e);
        } catch (DeviceFailException e) {
            throw new ReadFromDeviceException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
            } catch (USBHelper.USBException e) {

            }
        }

        return m2BinaryFile;

    }


    public static String readDeviceName(boolean debug) throws WriteToDeviceException {
        USBHelper.USBDeviceHandle usbDeviceHandle=null;
       String str="";
        try{


            usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=READ_DEVICE_NAME;

            //команда на запись
            USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            Response response = readResponseBuffer(usbDeviceHandle,debug);
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
            throw new WriteToDeviceException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
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
        USBHelper.USBDeviceHandle usbDeviceHandle=null;
        clearDevice(debug);
        try{
            System.out.print("Write command send..");

            usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=WRITE_COMMAND;

            byte[] lenBytes = ByteHelper.intToByteArray(dataToWrite.length, ByteHelper.ByteOrder.BIG_TO_SMALL);
            commandWrite[1]=lenBytes[0];
            commandWrite[2]=lenBytes[1];
            commandWrite[3]=lenBytes[2];
            commandWrite[4]=lenBytes[3];

            commandWrite[5]=(byte)langID;
            commandWrite[6]=(byte)countComplexes;

            if(debug) {
                System.out.print("COMMAND PACKET = ");

                for (int i = 0; i < commandWrite.length; i++) {

                    System.out.print((commandWrite[i] < 0 ? commandWrite[i] + 256 : commandWrite[i]) + ", ");

                }
                System.out.println("OUT_END_POINT=" + OUT_END_POINT + " IN_END_POINT=" + IN_END_POINT);
            }

            //команда на запись
            USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            Response response = readResponseBuffer(usbDeviceHandle,debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);


            if(debug)System.out.println("WRITE DATA...");

            //запись всего пакета в прибор по 64 байта. Нужно не забыть проверять ответ и статус записи, чтобы отловить ошибки
            for(int i=0;i < dataToWrite.length/DATA_PACKET_SIZE;i++){

                USBHelper.write(usbDeviceHandle, Arrays.copyOfRange(dataToWrite,DATA_PACKET_SIZE*i,DATA_PACKET_SIZE*i+DATA_PACKET_SIZE),OUT_END_POINT,REQUEST_TIMEOUT_MS);            //читаем
                 response = readResponseBuffer(usbDeviceHandle,debug);
                if(response.status==false) throw new DeviceFailException(response.errorCode);
            }

        } catch (USBHelper.USBException e) {
            throw new WriteToDeviceException(e);
        } catch (DeviceFailException e) {
            throw new WriteToDeviceException(e);
        } finally {
            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
            } catch (USBHelper.USBException e) {

            }
        }
    }


    /**
     * Читает буффер ответа
     * @param usbDeviceHandle
     * @return Response
     * @throws USBHelper.USBException
     */
    private static Response readResponseBuffer( USBHelper.USBDeviceHandle usbDeviceHandle,boolean debug) throws USBHelper.USBException {

        if(debug)System.out.print("READ RESPONSE...");
        ByteBuffer  response = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, REQUEST_TIMEOUT_MS);
        response.position(0);
        byte[] bytes = new byte[DATA_PACKET_SIZE];
        response.get(bytes);

        if(debug)System.out.println("Device response: "+ByteHelper.bytesToHex(bytes,64,' '));

        Response resp=new Response(bytes[0]==0?true:false,bytes[1]);
        int j=0;
        for(int i=2;i<DATA_PACKET_SIZE;i++)resp.getPayload()[j++]=bytes[i];

        return resp;
    }

    /**
     * Очистка устройства
     */
    public static void clearDevice(boolean debug) throws WriteToDeviceException {
        if(debug)System.out.print("CLEAR_DEVICE...");
        USBHelper.USBDeviceHandle usbDeviceHandle=null;
        try{

            usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=CLEAR_COMMAND;

            //команда на запись
            USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            if(debug)System.out.print("READ RESPONSE...");

            Response response = readResponseBuffer(usbDeviceHandle,debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

        } catch (USBHelper.USBException e) {
            throw new WriteToDeviceException(e);
        } catch (DeviceFailException e) {
            throw new WriteToDeviceException(e);
        } finally {
            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
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
                str="Data TimeOut Elapsed";
                break;
            case 2:
                str="Data TimeOut Elapsed";
                break;
            case 3:
                str="Data TimeOut Elapsed";
                break;
            case 4:
                str="Data TimeOut Elapsed";
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

    private static String getLang(TherapyComplex tc){
        return  getLangByOname(tc.getOname());
    }

    private static String getLang(TherapyProgram tc){
        return getLangByOname( tc.getOname());
    }

    private static String getLangByOname( String oname) {
        ModelDataApp mda = App.getStaticModel();
        String lang;
        if(oname.isEmpty()) lang=mda.getProgramLanguage().getAbbr();
        else {
            try {
                lang = mda.getOption("app.lang_insert_complex");
                if(lang==null) lang=mda.getDefaultLanguage().getAbbr();
                else if(lang.isEmpty())lang=mda.getProgramLanguage().getAbbr();
            } catch (Exception e) {
                lang=mda.getProgramLanguage().getAbbr();
            }
        }
        return lang;
    }


    public static void uploadProfile(Profile profile,boolean debug) throws M2Complex.MaxTimeByFreqBoundException, M2Complex.MaxPauseBoundException, M2Program.ZeroValueFreqException, M2Program.MaxProgramIDValueBoundException, M2Program.MinFrequenciesBoundException, M2Complex.MaxCountProgramBoundException, M2BinaryFile.MaxBytesBoundException, M2Complex.ZeroCountProgramBoundException, LanguageDevice.NoLangDeviceSupported, WriteToDeviceException {
        ModelDataApp mda = App.getStaticModel();
        M2BinaryFile bf=new M2BinaryFile();
        M2Complex m2c;

        for (TherapyComplex tc : mda.findAllTherapyComplexByProfile(profile))
        {

            m2c= new M2Complex(PAUSE_BETWEEN_PROGRAM,tc.getTimeForFrequency(),tc.getOname().isEmpty()?tc.getName():tc.getOname(),getLang(tc));

            for (TherapyProgram tp : mda.findTherapyPrograms(tc)) {

                m2c.addProgram(new M2Program(tp.parseFreqs(),tp.getId().intValue(),tp.getOname().isEmpty()?tp.getName():tp.getOname(),getLang(tp)));

            }
            bf.addComplex(m2c);
        }


        byte[] data = bf.getData();



        LanguageDevice deviceLang = LanguageDevice.getDeviceLang(mda.getProgramLanguage().getAbbr());



        if(deviceLang==null) throw new LanguageDevice.NoLangDeviceSupported(mda.getProgramLanguage().getAbbr());
        if(debug)System.out.println("Начало записи");
        writeToDevice(data,deviceLang.getDeviceLangID(),bf.getComplexesList().size(),debug);
    }


}
