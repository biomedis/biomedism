package ru.biomedis.biomedismair3.m2;


import org.hid4java.HidDevice;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.USB.ByteHelper;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public static M2BinaryFile readFromDevice(final boolean debug) throws ReadFromDeviceException, WriteToDeviceException {

        HidDevice device = null;
        M2BinaryFile m2BinaryFile = null;
        try {
            device = USBHelper.openDevice(productId, vendorId);
            if(device == null) throw new ReadFromDeviceException(new NullPointerException("Device == NULL"));
            byte[] commandRead = new byte[DATA_PACKET_SIZE];
            commandRead[0]=READ_COMMAND;
            if(debug)printPacket("Reading command",  commandRead);
            try {
                USBHelper.write(device, commandRead);
            }catch (USBHelper.USBException e){
                throw new WriteToDeviceException(e);
            }

            Response response =readResponseBuffer( device, 200, 3, debug);
            //Response response = readResponseBuffer(device, 200, debug);
            if(response.status==false) throw new DeviceFailException(response.errorCode);

            int size= ByteHelper.byteArray4ToInt(response.getPayload(),0, ByteHelper.ByteOrder.BIG_TO_SMALL);
            int langID=ByteHelper.byteArray1ToInt(response.getPayload(),4);
            System.out.println("Размер посылки: "+size);
            byte[]  data = new byte[DATA_PACKET_SIZE];
            if(size==0){
                //если прибор пустой, то создадим пустой файл
                return new M2BinaryFile();

            }else if(size > M2BinaryFile.MAX_FILE_BYTES   || size < 0){
                //вычитывание левых буфферов из винды. Если пришел левый размер
                int cnt = M2BinaryFile.MAX_FILE_BYTES/64;
                while(USBHelper.read(device, data, 5)!=0  &&  cnt > 0) {
                    cnt--;
                    System.out.println("flush.. ");
                }
                try {
                    USBHelper.closeDevice(device);
                } catch (USBHelper.USBException e) {

                }
                return readFromDevice(debug);
            }


            int packets = (int)Math.ceil(size / DATA_PACKET_SIZE);
            if(debug) System.out.println("packets = "+packets);
            if(debug) System.out.println("___________________");

            byte[] deviceData = new byte[DATA_PACKET_SIZE*packets];

            int realReading;
            for(int i=0;i<packets;i++){
                //читаем
                realReading = USBHelper.read(device, data, 200);
                if(realReading<DATA_PACKET_SIZE) throw new Exception("Прочитанный пакет меньше "+DATA_PACKET_SIZE);
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
        } catch (WriteToDeviceException e) {
            e.printStackTrace();
            throw  e;
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


    public static String readDeviceName(boolean debug) throws WriteToDeviceException,ReadFromDeviceException {
        HidDevice device=null;
        String str="";
        try{


            device = USBHelper.openDevice(productId, vendorId);
            if(device == null) throw new ReadFromDeviceException(new NullPointerException("Device == NULL"));
            byte[] commandWrite = new byte[DATA_PACKET_SIZE];
            commandWrite[0]=READ_DEVICE_NAME;

            //команда на запись
            try {
                USBHelper.write(device, commandWrite);
            }catch (USBHelper.USBException e){
                throw new WriteToDeviceException(e);
            }

            Response response =readResponseBuffer( device, 500, 3, debug);
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


    private static Response readResponseBuffer(HidDevice device,int timeout,int tryCount, boolean debug)throws USBHelper.USBException{
       return readResponseBuffer( device, timeout,timeout,  tryCount,  debug);
    }

    /**
     *
     * @param device
     * @param timeout таймаут первого запроса
     * @param timeout2  таймаут последующих запросов
     * @param tryCount
     * @param debug
     * @return
     * @throws USBHelper.USBException
     */
    private static Response readResponseBuffer(HidDevice device,int timeout, int timeout2, int tryCount, boolean debug)throws USBHelper.USBException{
        if(device == null) throw new USBHelper.USBException("Device == NULL");
        if(debug)System.out.print("READ RESPONSE...");
        byte[] bytes = new byte[DATA_PACKET_SIZE];
        USBHelper.USBException ex = new USBHelper.USBException("No data reading!!!");
        int cnt=tryCount;

        while (cnt > 0){

            int read = USBHelper.read(device, bytes, cnt < tryCount? timeout2 : timeout);
            if(read  < DATA_PACKET_SIZE){
                cnt--;
                if(debug)System.out.println("Retry reading: "+(tryCount-cnt));
            }else {
                cnt=0;
                ex=null;
            }
        }

        if(ex != null)throw ex;

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

           // Response response = request( commandWrite,  device ,debug, 20000);
            // response = readResponseBuffer(device,timeoutRead, debug);
            USBHelper.write(device, commandWrite);
            Response response=null;
            try {
                 response = readResponseBuffer(device, 25000, 3000, 3, debug);
            }catch (USBHelper.USBException e){
                USBHelper.write(device, commandWrite);
                response = readResponseBuffer(device, 25000, 3000, 3, debug);
            }

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


    public static M2BinaryFile uploadProfile(Profile profile, boolean debug) throws M2Complex.MaxTimeByFreqBoundException, M2Complex.MaxPauseBoundException, M2Program.ZeroValueFreqException, M2Program.MaxProgramIDValueBoundException, M2Program.MinFrequenciesBoundException, M2Complex.MaxCountProgramBoundException, M2BinaryFile.MaxBytesBoundException, M2Complex.ZeroCountProgramBoundException, LanguageDevice.NoLangDeviceSupported, WriteToDeviceException {
        ModelDataApp mda = App.getStaticModel();
        M2BinaryFile bf=new M2BinaryFile();
        M2Complex m2c;
        List<M2Program> pList=new ArrayList<>();

        for (TherapyComplex tc : mda.findAllTherapyComplexByProfile(profile))
        {
            String cName= tc.getName().replace("?","");
            String lancTC = LanguageDevice.langByCodePoint(cName).getAbbr();

            for (TherapyProgram tp : mda.findTherapyPrograms(tc).stream().filter(i->!i.isMp3()).collect(Collectors.toList())) {
                String pName= tp.getName().replace("?","");
                pList.add(new M2Program(tp.parseFreqs(),tp.getId().intValue(), pName, LanguageDevice.langByCodePoint(pName).getAbbr()));

            }

            m2c= new M2Complex(PAUSE_BETWEEN_PROGRAM,tc.getTimeForFrequency(),cName,lancTC);
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
}
