package ru.biomedis.starter.m2;


import ru.biomedis.starter.USB.ByteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.biomedis.starter.USB.ByteHelper.*;


public class M2Complex
{

   private List<M2Program> programs=new ArrayList<>();
   private int pauseBetweenPrograms;
   private int timeByFrequency;
   private static final  int MAX_PROGRAM_COUNT_IN_COMPLEX = (int)Math.pow(2,Short.SIZE)-1;
   private static final  int MAX_PAUSE = (int)Math.pow(2,Short.SIZE)-1;
   private static final  int MAX_TIME_BY_FREQ = (int)Math.pow(2,Short.SIZE)-1;
   private int lastComplexInArrayPosition;
   private static final int MAX_NAME_LENGTH=(int)Math.pow(2,Byte.SIZE)-1;
   private String name;
   private String langAbbr;

   public static final int BUNDLES_LENGTH =3;

    public  int getBundlesLength() {
        return BUNDLES_LENGTH;
    }

    /**
     *
     * @param pauseBetweenPrograms время паузы между программами в секундах
     * @param timeByFrequency время на частоту в секундах
     * @throws MaxPauseBoundException
     * @throws MaxTimeByFreqBoundException
     */
    public M2Complex(int pauseBetweenPrograms, int timeByFrequency, String name, String langAbbr) throws MaxPauseBoundException, MaxTimeByFreqBoundException {
        this.pauseBetweenPrograms = pauseBetweenPrograms;
        this.timeByFrequency = timeByFrequency;
        this.name = name;
        this.langAbbr = langAbbr;
        if(this.name.length()>MAX_NAME_LENGTH)this.name=this.name.substring(0,MAX_NAME_LENGTH);
        if(this.pauseBetweenPrograms>=MAX_PAUSE)  throw new MaxPauseBoundException();
        if(this.timeByFrequency>=MAX_TIME_BY_FREQ) throw new MaxTimeByFreqBoundException();

    }

    /**
     * Переводит байтовые данные в структуру данных
     * @param complexData
     * @param startPosition
     */
    public M2Complex(byte[] complexData, int startPosition) throws ComplexParseException {
        int position = startPosition;
        int countPrograms =0;

        /**
         * 1 байт размер строки названия комплекса
         * n байт строка
         * 1 байт id языка
         * 2 байта  - пауза между программами
         * 2 байта время на частоту
         * 2 байта колличество программ
         * - --программы
         */
        try {


            int countSymbols=byteArray1ToInt(complexData, position++);//уолличество символов в названии программы
            int lang= byteArray1ToInt(complexData,position+countSymbols);
            LanguageDevice language = LanguageDevice.getLanguage(lang);


            name = byteArrayToString(complexData,
                    position,
                    countSymbols,
                    ByteHelper.ByteOrder.BIG_TO_SMALL,
                    language.getEncodedType());

            langAbbr=language.getAbbr();
            position += countSymbols;//4 байта
            position++;//пропустим id языка



            // пауза между программами 1 байт
            this.pauseBetweenPrograms = byteArray2ToInt(complexData,position, ByteHelper.ByteOrder.BIG_TO_SMALL);
            position+=2;

            // время на частоту 1 байт
            this.timeByFrequency = byteArray2ToInt(complexData,position, ByteHelper.ByteOrder.BIG_TO_SMALL);
            position+=2;

            //количество программ в комплексе 1 байт
            countPrograms= byteArray2ToInt(complexData,position,ByteOrder.BIG_TO_SMALL);
            position+=2;

            //программы

            for(int i=0;i<countPrograms;i++){

                M2Program m2Program  = new M2Program(complexData, position);
                programs.add(m2Program);
                position = m2Program.getLastPositionInArray()+1;//укажет на след. стартовую поз. программы

            }

            lastComplexInArrayPosition=position-1;

        }catch (M2Program.ProgramParseException e) {
           throw new ComplexParseException(e);
        } catch (Exception e){
            throw new ComplexParseException(e);
        }



    }

    protected int getLastComplexInArrayPosition() {
        return lastComplexInArrayPosition;
    }

    public List<M2Program> getPrograms() {
        return Collections.unmodifiableList(programs);
    }

    /**
     * Добавляет програму в комплекс. Может выбросить исключение при привышение лимита в 255 программ.
     * @param p
     * @throws MaxCountProgramBoundException
     */
    public void addProgram(M2Program p) throws MaxCountProgramBoundException {
        if(programs.size() >= MAX_PROGRAM_COUNT_IN_COMPLEX )throw new MaxCountProgramBoundException();
        programs.add(p);
    }

    public void addPrograms(List<M2Program> programList) throws MaxCountProgramBoundException {
        if(programList.size()+programs.size() >= MAX_PROGRAM_COUNT_IN_COMPLEX )throw new MaxCountProgramBoundException();
        programs.addAll(programList);
    }

    public int getPauseBetweenPrograms() {
        return pauseBetweenPrograms;
    }

    public int getTimeByFrequency() {
        return timeByFrequency;
    }

    public String getName() {
        return name;
    }

    public String getLangAbbr() {
        return langAbbr;
    }

    public int getCountPrograms(){return programs.size();}

    protected List<Byte> toByteList() throws ZeroCountProgramBoundException, LanguageDevice.NoLangDeviceSupported {

        if(programs.size()==0) throw new ZeroCountProgramBoundException();

        List<Byte> res=new ArrayList<>();
        List<Byte> bytesName =  LanguageDevice.getBytesInDeviceLang(name,langAbbr);


        /**
         * 1 байт размер строки названия комплекса
         * n байт строка
         * 1 байт id языка
         * 2 байта  - пауза между программами
         * 2 байта время на частоту
         * 2 байта колличество программ
         * - --программы
         */
        res.add((byte)bytesName.size());//размер строки названия
        res.addAll(bytesName);//имя программы
        res.add((byte)LanguageDevice.getDeviceLang(langAbbr).getDeviceLangID());//ID языка
        res.addAll(intTo2ByteList(getPauseBetweenPrograms(), ByteOrder.BIG_TO_SMALL));
        res.addAll(intTo2ByteList(getTimeByFrequency(), ByteOrder.BIG_TO_SMALL));
        res.addAll(intTo2ByteList(getCountPrograms(), ByteOrder.BIG_TO_SMALL));

        for (M2Program program : programs)   res.addAll(program.toByteList());

        return res;
    }



    /**
     * Ошибка парсинга байтового масиива комплекса
     */
    public static class ComplexParseException extends Exception {
        public ComplexParseException(Throwable cause) {
            super( cause);
        }
    }


    /**
     * Если пауза больше возможной(255)
     */
    public static class MaxPauseBoundException extends Exception{
        protected MaxPauseBoundException() {
            super();
        }
    }
    /**
     * Если время на частоту больше возможной(255)
     */
    public static class MaxTimeByFreqBoundException extends Exception{
        protected MaxTimeByFreqBoundException() {
            super();
        }
    }
    /**
     * Указывает на то что превышено, количество программ в комплексе
     */
    public static class MaxCountProgramBoundException extends Exception{
        protected MaxCountProgramBoundException() {
            super();
        }
    }

    public static class ZeroCountProgramBoundException extends Exception{
        protected ZeroCountProgramBoundException() {
            super();
        }
    }

    @Override
    public String toString() {
        String res= "\nM2Complex{\n"+
                "countPrograms = "+programs.size()+
                "  pauseBetweenPrograms=" +pauseBetweenPrograms+
                ", timeByFrequency=" + timeByFrequency +
                ", name="+name+
                ", lastComplexInArrayPosition=" + lastComplexInArrayPosition +
                "\nprograms={\n";

        for (M2Program program : programs) {
            res+=program.toString();
        }

        res+="\n}\n";
        return res;
    }
}
