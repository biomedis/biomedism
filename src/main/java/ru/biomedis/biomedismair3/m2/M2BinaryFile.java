package ru.biomedis.biomedismair3.m2;


import ru.biomedis.biomedismair3.utils.USB.ByteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class M2BinaryFile {

    private final List<M2Complex> complexesList=new ArrayList<>();
    public static final int MAX_FILE_BYTES=786432;
    private static final int ALIGN_FILE_BYTE_SIZE=64;//длина файла должна быть кратна ALIGN_FILE_BYTE_SIZE
    private  byte[] rawReadedData;
    private int systemLangID;


public M2BinaryFile() {
    }

    public int getSystemLangID() {
        return systemLangID;
    }

    public void addComplex(M2Complex complex){complexesList.add(complex);}

    public byte[] getRawReadedData() {
        return rawReadedData;
    }

    /**
     * Переводит байтовые данные в структуру данных
     * @param fileData
     */
    public M2BinaryFile(byte[] fileData,int systemLangID) throws FileParseException {
        rawReadedData = fileData;
        this.systemLangID=systemLangID;

        //4 байта - позиция перед началом первого комплекса

        try {
            int position= ByteHelper.byteArray4ToInt(fileData,0, ByteHelper.ByteOrder.BIG_TO_SMALL);
            int countComplexes=position/4;

            M2Complex m2Complex;
         for (int i=0;i<countComplexes;i++){

              m2Complex = new M2Complex(fileData, position);
             complexesList.add(m2Complex);
             position=m2Complex.getLastComplexInArrayPosition()+1;

         }


        }  catch (M2Complex.ComplexParseException e) {
            throw new FileParseException(e);
        } catch (Exception e) {
            throw new FileParseException(e);
        }


    }

    public List<M2Complex> getComplexesList() {
        return Collections.unmodifiableList(complexesList);
    }



    public  byte [] getData() throws MaxBytesBoundException, M2Complex.ZeroCountProgramBoundException, LanguageDevice.NoLangDeviceSupported {


        final List<Byte> res=new ArrayList<>();
        final List<List<Byte>> complexesData=new ArrayList<>();



        for (M2Complex m2Complex : complexesList) {
            complexesData.add(m2Complex.toByteList());
        }



        int currentAdr=4*complexesList.size();

        for (int i=0; i<complexesData.size();i++) {


            res.addAll(ByteHelper.intToByteList(currentAdr, ByteHelper.ByteOrder.BIG_TO_SMALL));
            currentAdr += complexesData.get(i).size();
        }

        for (List<Byte> cData : complexesData) {
            res.addAll(cData);
        }


        if(res.size()> MAX_FILE_BYTES)  throw new MaxBytesBoundException();

        int additionalBytes = ALIGN_FILE_BYTE_SIZE -res.size() % ALIGN_FILE_BYTE_SIZE;

        final byte[] result=new byte[res.size()+additionalBytes];
       for(int i=0;i<res.size();i++)result[i]=res.get(i);
       return result;
    }

    public static class MaxBytesBoundException extends Exception{
        protected MaxBytesBoundException() {
            super();
        }
    }

    public static class FileParseException extends Exception{
        public FileParseException(Throwable cause) {
            super(cause);
        }
    }



    @Override
    public String toString() {
        return "M2BinaryFile:\n" +
                  complexesList.stream().map(c->c.toString()).collect(Collectors.joining("\n"));
    }
}
