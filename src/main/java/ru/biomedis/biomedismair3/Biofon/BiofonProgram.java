package ru.biomedis.biomedismair3.Biofon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.utils.USB.ByteHelper.*;


public class BiofonProgram {
    private List<Double> frequencies = new ArrayList<>();
    private List<Integer> frequenciesInDeviceFormat;
    private static  final  double FREQ_FORMAT_COEFF = 85899.34592;
    private static final  int MAX_FREQUENCIES_COUNT = (int) Math.pow(2, Byte.SIZE) - 1;
    private static final  int FREQ_NUM_BYTES = 4;
    private static final  int FREQ_PRECISE = 2;
    private static final  double MAX_FREQ_VALUE = 12500.0;
    private int lastPositionInArray;
    private int programID;
    private static final  int MAX_PROGRAM_ID_VALUE=(int)Math.pow(2,Byte.SIZE*3)-1;

    /**
     * @param frequencies
     * @param programID
     */
    public BiofonProgram(List<Double> frequencies, int programID) throws MaxFrequenciesBoundException, MinFrequenciesBoundException, MaxProgramIDValueBoundException {
            if(programID > MAX_PROGRAM_ID_VALUE) throw new MaxProgramIDValueBoundException();
                this.programID=programID;
        if(frequencies.size()==0) throw new MinFrequenciesBoundException();
        if (frequencies.size() >= MAX_FREQUENCIES_COUNT) throw new MaxFrequenciesBoundException();
        this.frequencies.addAll(frequencies.stream().map(f-> f.doubleValue() > MAX_FREQ_VALUE ? MAX_FREQ_VALUE:f ).collect(Collectors.toList()));
        //преобразование в формат прибора
        frequenciesInDeviceFormat = this.frequencies.stream()
                                                    .map(BiofonProgram::freqToDeviceFormat)
                                                    .collect(Collectors.toList());

    }

    /**
     * Создает программу из байтового представления прибора
     *
     * @param programInBytes
     */
    public BiofonProgram(byte[] programInBytes, int startPosition) throws ProgramParseException {
        int position = startPosition;
        frequenciesInDeviceFormat = new ArrayList<>();

        try {
            int countFreq = byteArray1ToInt(programInBytes, position++);//первый байт содержит количество частот
            if (countFreq == 0) throw new ZeroFrequenciesException();

            for (int i = 0; i < countFreq; i++) {

                frequenciesInDeviceFormat.add(byteArray4ToInt(programInBytes, position, ByteOrder.BIG_TO_SMALL));
                position += FREQ_NUM_BYTES;//4 байта

            }
            //последняя позиция в массиве программы
            lastPositionInArray=position-1;
            frequenciesInDeviceFormat.forEach(f -> frequencies.add(freqFromDeviceFormat(f)));


        } catch (ZeroFrequenciesException e) {
            throw new ProgramParseException(e);
        } catch (Exception e) {
            throw new ProgramParseException(e);
        }


    }

    protected int getLastPositionInArray(){return lastPositionInArray;}

    /**
     * Преобразует частоту в формат прибора
     *
     * @param freq
     * @return
     */
    private static int freqToDeviceFormat(double freq) {
        return (int) Math.round(freq * FREQ_FORMAT_COEFF);
    }

    /**
     * Преобразует частоту из формата прибора в обычную частоту.
     *
     * @param freq
     * @return
     */
    private static double freqFromDeviceFormat(int freq) {
        double f = freq / FREQ_FORMAT_COEFF;
        return new BigDecimal(f).setScale(FREQ_PRECISE, RoundingMode.UP).doubleValue();

    }


    public List<Double> getFrequencies() {
        return Collections.unmodifiableList(this.frequencies);
    }

    public List<Integer> getFrequenciesInDeviceFormat() {
        return Collections.unmodifiableList(frequenciesInDeviceFormat);
    }

    public int getCountFrequencies() {
        return frequencies.size();
    }


    protected void setProgramID(int programID) {
        this.programID = programID;
    }

    /**
     * ID програмы в программе(из какой программы была получена в программе эта программа)
     *
     * @return
     */
    public int getProgramID() {
        return programID;
    }

    /**
     * Преобразует программу в байтовое представление для прибора
     *
     * @return
     */
    protected List<Byte> toByteList() {

        //програма1 - количество частот 1байт, по 4 байта сами частоты
        List<Byte> res = new ArrayList<>();

        res.add((byte) frequencies.size());
        for (Integer freq : frequenciesInDeviceFormat) {
            res.addAll(intToByteList(freq, ByteOrder.BIG_TO_SMALL));
        }


        return res;
    }


    /**
     * Программа содержит нулевое поле числа частот
     */
    public static class ZeroFrequenciesException extends Exception {
        public ZeroFrequenciesException() {
        }
    }

    /**
     * Ошибка парсинга байтового масиива программы
     */
    public static class ProgramParseException extends Exception {
        public ProgramParseException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Указывает на превышение количества частот програме заданого предела(255)
     */
    public static class MaxFrequenciesBoundException extends Exception {
        protected MaxFrequenciesBoundException() {
            super();
        }
    }
    /**
     * Указывает на то , что в программе нет частот
     */
    public static class MinFrequenciesBoundException extends Exception {
        protected MinFrequenciesBoundException() {
            super();
        }
    }

    public static class MaxProgramIDValueBoundException extends Exception {
        protected MaxProgramIDValueBoundException() {
            super();
        }
    }

    @Override
    public String toString() {
        return "BiofonProgram{\n" +
                "countFrequencies = "+frequencies.size()+
                "\n frequenciesInDeviceFormat=" + frequenciesInDeviceFormat +
                "\n lastPositionInArray=" + lastPositionInArray +
                ", programID=" + programID +
                " \nfrequencies = [ " + frequencies.stream().map(f->f.toString()).collect(Collectors.joining("; ")) +" ]\n}\n";

    }
}
