package ru.biomedis.biomedismair3.m2;


import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.USB.ByteHelper;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            if(usbDeviceHandle==null) throw new Exception("Не удалось открыть устройство");

            byte[] commandRead = new byte[DATA_PACKET_SIZE];
            commandRead[0]=READ_COMMAND;
            if(debug)printPacket("Reading command",  commandRead);
            USBHelper.write(usbDeviceHandle,commandRead,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            Response response = readResponseBuffer(usbDeviceHandle,200,3,debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

            int size= ByteHelper.byteArray4ToInt(response.getPayload(),0, ByteHelper.ByteOrder.BIG_TO_SMALL);
            int langID=ByteHelper.byteArray1ToInt(response.getPayload(),4);
            System.out.println("Размер посылки: "+size);

            if(size==0){
                //если прибор пустой, то создадим пустой файл
               return new M2BinaryFile();

            }


            int packets = (int)Math.ceil(size / DATA_PACKET_SIZE);
            if(debug) System.out.println("packets = "+packets);
            if(debug) System.out.println("___________________");

            byte[] deviceData = new byte[DATA_PACKET_SIZE*packets];
            ByteBuffer data=null;
            USBHelper.USBException e=new USBHelper.USBException("Ошибка чтения",0);
            for(int i=0;i<packets;i++){
                //читаем
                data=null;
                int cnt =3;
                while (cnt > 0) {
                    try {
                        data = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, 200);
                        cnt=0;
                    }
                    catch (USBHelper.USBException ex){
                       e=ex;

                        cnt--;
                        if(debug) System.out.print("Try reading: "+(3-cnt));
                    }
                }
                if(data==null) throw e;
                data.position(0);
                data.get(deviceData,i*DATA_PACKET_SIZE,DATA_PACKET_SIZE);

            }
            if(debug) System.out.println(ByteHelper.bytesToHex(deviceData,16,' '));
            if(debug) System.out.print("Parse data...");



            m2BinaryFile = new M2BinaryFile(deviceData,langID);

        }  catch (USBHelper.USBException e) {
            Log.logger.error("",e);
            throw new ReadFromDeviceException(e);
        } catch (M2BinaryFile.FileParseException e) {
            Log.logger.error("",e);
            throw new ReadFromDeviceException(e);
        } catch (DeviceFailException e) {
            Log.logger.error("",e);
            throw new ReadFromDeviceException(e);
        } catch (Exception e) {
            Log.logger.error("",e);
            throw new ReadFromDeviceException(e);
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
            if(usbDeviceHandle==null) throw new Exception("Не удалось открыть устройство");
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=READ_DEVICE_NAME;

            //команда на запись
            USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);

            Response response = readResponseBuffer(usbDeviceHandle,200,3,debug);
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
            Log.logger.error("",e);
            throw new WriteToDeviceException(e);
        } catch (Exception e) {
            Log.logger.error("",e);
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
            if(usbDeviceHandle==null) throw new Exception("Не удалось открыть устройство");
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

            USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);


            if(debug)System.out.println("WRITE DATA...");

            //запись всего пакета в прибор по 64 байта. Нужно не забыть проверять ответ и статус записи, чтобы отловить ошибки
            for(int i=0;i < packets;i++){

                USBHelper.write(usbDeviceHandle, Arrays.copyOfRange(dataToWrite,DATA_PACKET_SIZE*i,DATA_PACKET_SIZE*i+DATA_PACKET_SIZE),OUT_END_POINT,REQUEST_TIMEOUT_MS);      //читаем

                if(debug)System.out.println("N packet =" + (i+1));
            }

        } catch (USBHelper.USBException e) {
            Log.logger.error("",e);
            throw new WriteToDeviceException(e);
        } catch (Exception e){
            Log.logger.error("",e);
            throw new WriteToDeviceException(e);
        }
        finally {

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
        USBHelper.USBDeviceHandle usbDeviceHandle=null;
        try{

            usbDeviceHandle = USBHelper.openDevice(productId, vendorId, 0);
            if(usbDeviceHandle==null) throw new Exception("Не удалось открыть устройство");
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=CLEAR_COMMAND;

            if(debug)printPacket("Clear device",  commandWrite);
            //команда на запись
            //USBHelper.write(usbDeviceHandle,commandWrite,OUT_END_POINT,REQUEST_TIMEOUT_MS);
            //Response response = readResponseBuffer(usbDeviceHandle,debug);

            Response  response = request( commandWrite,  usbDeviceHandle ,debug, 10000);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

        } catch (USBHelper.USBException e) {
            Log.logger.error("",e);
            throw new WriteToDeviceException(e);
        } catch (DeviceFailException e) {
            Log.logger.error("",e);
            throw new WriteToDeviceException(e);
        } catch (Exception e){
            Log.logger.error("",e);
            throw new WriteToDeviceException(e);
        }
        finally {

            try {
                USBHelper.closeDevice(usbDeviceHandle,0);
            } catch (USBHelper.USBException e) {

            }
        }
    }


    /**
     * Читает буффер ответа, с повтором по таймауту
     * @param usbDeviceHandle
     * @return Response
     * @throws USBHelper.USBException
     */
    private static Response readResponseBuffer( USBHelper.USBDeviceHandle usbDeviceHandle,int timeout,int tryCount, boolean debug) throws USBHelper.USBException {


        ByteBuffer  response = null;
        USBHelper.USBException ex=new USBHelper.USBException("Ошибка чтения",0);
        int cnt=tryCount;

        while (cnt > 0){
            try {
                response = USBHelper.read(usbDeviceHandle, DATA_PACKET_SIZE, IN_END_POINT, timeout);
                cnt=0;
            }catch (USBHelper.USBException e){
                ex=e;
                cnt--;
                if(debug)System.out.println("Retry reading: "+(3-cnt));
            }
        }

        if(response == null)throw ex;
        response.position(0);
        byte[] bytes = new byte[DATA_PACKET_SIZE];
        response.get(bytes);

        //if(debug)System.out.println("Device response: "+ByteHelper.bytesToHex(bytes,64,' '));

        Response resp=new Response(bytes[0]==0?true:false,bytes[1]);
        int j=0;
        for(int i=2;i<DATA_PACKET_SIZE;i++)resp.getPayload()[j++]=bytes[i];
        if(debug)printPacket("Response",  bytes);
        return resp;
    }


    private static Response request(byte[] commandWrite,  USBHelper.USBDeviceHandle usbDeviceHandle , boolean debug, int timeoutRead) throws USBHelper.USBException {
        int counter=0;
        USBHelper.USBException ex=null;
        Response  response=null;
        while(counter<2){
            try {
                USBHelper.write(usbDeviceHandle, commandWrite, OUT_END_POINT, timeoutRead);
                response = readResponseBuffer(usbDeviceHandle,timeoutRead,2, debug);
                counter=2;
            }catch (USBHelper.USBException e){
                ex = e;
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

    private static String getLang(TherapyComplex tc){
        return  getLangByOname(tc.getOname());
    }

    private static String getLang(TherapyProgram tc){
        return getLangByOname( tc.getOname());
    }

    /**
     * Если есть oName, то язык приложения отличается от языка вставки и нужно указать язык из опции языка вставки.
     * Иначе указывается язык приложения
     * @param oname
     * @return
     */
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


    public static M2BinaryFile uploadProfile(Profile profile,boolean debug) throws M2Complex.MaxTimeByFreqBoundException, M2Complex.MaxPauseBoundException, M2Program.ZeroValueFreqException, M2Program.MaxProgramIDValueBoundException, M2Program.MinFrequenciesBoundException, M2Complex.MaxCountProgramBoundException, M2BinaryFile.MaxBytesBoundException, M2Complex.ZeroCountProgramBoundException, LanguageDevice.NoLangDeviceSupported, WriteToDeviceException {
        ModelDataApp mda = App.getStaticModel();
        M2BinaryFile bf=new M2BinaryFile();
        M2Complex m2c;
        List<M2Program> pList=new ArrayList<>();

        for (TherapyComplex tc : mda.findAllTherapyComplexByProfile(profile))
        {
            String lancTC = LanguageDevice.langByCodePoint(tc.getName()).getAbbr();

            for (TherapyProgram tp : mda.findTherapyPrograms(tc).stream().filter(i->!i.isMp3()).collect(Collectors.toList())) {

                pList.add(new M2Program(tp.parseFreqs(),tp.getId().intValue(),tp.getName(), LanguageDevice.langByCodePoint(tp.getName()).getAbbr()));

            }

            m2c= new M2Complex(PAUSE_BETWEEN_PROGRAM,tc.getTimeForFrequency(),tc.getName(),lancTC);
            m2c.addPrograms(pList);
            bf.addComplex(m2c);
            pList.clear();
        }


        byte[] data = bf.getData();


        System.out.println("Размер посылки: "+data.length);
        LanguageDevice deviceLang = LanguageDevice.getDeviceLang(mda.getProgramLanguage().getAbbr());

        if(debug){
           // System.out.println(ByteHelper.bytesToHex(data,32,' '));

           // for (byte dt : data) {

            //    System.out.println(dt<0?dt+256:dt);
           // }

            try {
                M2BinaryFile bf2=new M2BinaryFile(data,deviceLang.getDeviceLangID());
                System.out.println(bf2);
            } catch (M2BinaryFile.FileParseException e) {
                e.printStackTrace();
            }

        }

        if(deviceLang==null) throw new LanguageDevice.NoLangDeviceSupported(mda.getProgramLanguage().getAbbr());
        if(debug)System.out.println("Начало записи");
        writeToDevice(data,deviceLang.getDeviceLangID(),bf.getComplexesList().size(),debug);
        return bf;
    }

        private static void printPacket(String name, byte[] packet){
            System.out.println(name+" = ");

            for (int i = 0; i < packet.length; i++) {

                System.out.print((packet[i] < 0 ? packet[i] + 256 : packet[i]) + ", ");

            }
            System.out.println();
        }
}
