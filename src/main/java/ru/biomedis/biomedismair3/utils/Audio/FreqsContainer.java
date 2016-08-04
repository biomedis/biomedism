package ru.biomedis.biomedismair3.utils.Audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Anama on 16.10.2015.
 */
public class FreqsContainer
{

    private  List<Freq> fList=new ArrayList<>();//список элементов в которых или 1 частота или несколько(тогда они паралельны)
    private   int timeForFreq=0;//время на частоту
    private  int sampleRate;
    private long lengthInSample=0;//колличество сэмплов на частоту(за время ее воспроизведения)
    private boolean mullty=false;
    private int sampleSiseInBits=0;
    private int bundlesLength=1;

    public FreqsContainer(String freqs,int timeForFreq, int sampleRate,boolean mullty,int bundlesLength,int sampleSiseInBits) {
        this.timeForFreq = timeForFreq;
        this.sampleRate = sampleRate;
        this.lengthInSample=timeForFreq*sampleRate;
        this.sampleSiseInBits=sampleSiseInBits;
        this.bundlesLength=bundlesLength;
        String[] split=null;



        if( mullty  )
        {
            //чисто паралельные частоты
            List<Double> fl=new ArrayList<>();
            split = freqs.split(";");

            for (String s : split)
            {
                if(s.contains("+"))
                {
                    String[] split1 = s.split("\\+");
                    for (String s1 : split1)  fl.add(Double.parseDouble(s1));

                }else fl.addAll(Arrays.asList(Double.parseDouble(s)));

            }
            if(bundlesLength >1)
            {
                //пачки частот
                int bundlesCount=(int)Math.ceil((float)fl.size()/(float)bundlesLength);
                int cEnd=0;
                int cEndT=0;
                for(int i=0;i<bundlesCount;i++)
                {
                    cEndT = (i+1)*bundlesLength;
                    if(cEndT<=fl.size())cEnd=cEndT;
                    else cEnd=fl.size()-1;
                    //разбиваем по пачкам

                    addFreq(fl.subList(i*bundlesLength,cEnd));
                }


            }else addFreq(fl);



        }else
        {
            //последовательные частоты
            split = freqs.split(";");

            for (String s : split)
            {
                if(s.contains("+"))
                {
                    String[] split1 = s.split("\\+");
                    List<Double> fl=new ArrayList<>();
                    for (String s1 : split1)  fl.add(Double.parseDouble(s1));
                    addFreq(fl);

                }else addFreq(Arrays.asList(Double.parseDouble(s)));

            }
        }



    }

    public boolean isMullty() {
        return mullty;
    }

    public int getTimeForFreq() {
        return timeForFreq;
    }


    public int getSampleRate() {
        return sampleRate;
    }


    private void addFreq(List<Double> freqs)
    {
        Freq freq = new Freq(freqs);
        if(!isMullty())freq.setEndPosition((fList.size()+1) * lengthInSample - 1);//позиция считается от нуля
        else{
            if(bundlesLength>1)freq.setEndPosition((fList.size()+1) * lengthInSample - 1);
             else  freq.setEndPosition(lengthInSample - 1);
        }

        fList.add(freq);

    }

    /**
     * Список частот для обработки. Нужно учитывать мультичастотность  isMullty(), тк если он включен то позиции концов частот все будут одинаковы и лежать на конце массива
     * @return
     */
    public final List<Freq> getFreqs(){return this.fList;}

    /**
     * Размер всего потока в семплах
     * @return
     */
public long getLength()
{

    if(isMullty())return lengthInSample;
        else  return fList.size()*lengthInSample;

}





    /**
     * Представляет частоту или частоты паралельные.
     */
    public  class Freq
    {
        List<Double> freqs=new ArrayList<>();//список частот паралельных. если частота одна то в массиве 1 элемент
        long endPosition=0;//позиция конца данной частоты в сэмплах
        long endPositionByte=0;//позиция конца данной частоты в байтах
        double koeff[];//коэффициенты частот
        double  Am ;//нормируем паралельные частоты, так чтобы в сумме не более максимума

        private Freq(List<Double> freqs)
        {
            this.freqs = freqs;
            koeff=new double[freqs.size()];
            for(int i=0;i<freqs.size();i++)koeff[i]=2*Math.PI * freqs.get(i)/ (double)sampleRate;


            Am =   (Math.pow(2, sampleSiseInBits-1)-1)/(double)freqs.size();

        }

        public final double getAm(){return Am;}
        public final double[] getKoeff(){return koeff;}
        public int countMulltyFreq(){return freqs.size();}



        public final List<Double> getFreqs() {
            return freqs;
        }

        /**
         * Позиция считается от 0
         * позиция конца данной частоты в сэмплах
         * @return
         */
        public final long getEndPositionFreqInSamples(){return endPosition;}

        /**
         * Позиция считается от 0
         * позиция конца данной частоты в байтах
         * @return
         */
        public final long getEndPositionFreqInByte(){return endPositionByte;}



        private void setEndPosition(long position) {
            this.endPosition = position;
            endPositionByte=this.endPosition*2;
        }
    }

}
