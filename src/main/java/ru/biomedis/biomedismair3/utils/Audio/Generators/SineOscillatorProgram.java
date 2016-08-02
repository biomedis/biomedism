package ru.biomedis.biomedismair3.utils.Audio.Generators;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import ru.biomedis.biomedismair3.utils.Audio.FreqsContainer;


/**
 * Created by Anama on 05.10.2015.
 */
public class SineOscillatorProgram extends AudioInputStream
{

    private long position=0;//позиция в short не в байтах
    private long totalBytes;
    private double PI2=2*Math.PI;
    private double koeff=0;
    private double tabeSin[]=new double[362];
    private double gradKoef = 180.0/Math.PI;
    private FreqsContainer.Freq currentFreq;
    private FreqsContainer fk;





    /**
     * Выдает 16 бит мого PCM_SIGNED little-endian
     */
    public SineOscillatorProgram(FreqsContainer fk)
    {

        super(new ByteArrayInputStream(new byte[0]),new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fk.getSampleRate(),16,1,2,fk.getSampleRate(),false),fk.getLength());
        this.fk=fk;

        totalBytes = getFrameLength()*2;//2 байта на значение

        currentFreq=fk.getFreqs().get(0);


        double krad=Math.PI/180.0;
        for(int i=0;i<=361;i++) tabeSin[i]=Math.sin(krad*i);












    }

    /**
     * Углы всегда должны быть положительными
     * @param v1
     * @return
     */
    private double fastSin(double v1)
    {

        double v=v1*gradKoef;//переведем в градусы
        if(v>360) v =v % 360.0;
        int v_int=(int)v;
        double v_frac=v-v_int;

        return  (tabeSin[v_int]+v_frac*(tabeSin[v_int+1]-tabeSin[v_int]));




    }

    /**	Returns the number of bytes that can be read without blocking.
     Since there is no blocking possible here, we simply try to
     return the number of bytes available at all. In case the
     length of the stream is indefinite, we return the highest
     number that can be represented in an integer. If the length
     if finite, this length is returned, clipped by the maximum
     that can be represented.
     */
    public int available()
    {
        int	nAvailable = 0;
        if (totalBytes == AudioSystem.NOT_SPECIFIED)
        {
            nAvailable = Integer.MAX_VALUE;
        }
        else
        {
            long	lBytesAvailable = totalBytes;
            nAvailable = (int) Math.min(lBytesAvailable, (long) Integer.MAX_VALUE);
        }
        return nAvailable;
    }



    /*
      this method should throw an IOException if the frame size is not 1.
      Since we currently always use 16 bit samples, the frame size is
      always greater than 1. So we always throw an exception.
    */
    public int read()
            throws IOException
    {

        throw new IOException("cannot use this method currently");
    }



    public int read(byte[] abData, int nOffset, int nLength)
            throws IOException
    {



        if(totalBytes<=0)return -1;

        if (nLength % getFormat().getFrameSize() != 0)
        {
            throw new IOException("length must be an integer multiple of frame size");
        }
        int	nConstrainedLength = Math.min(available(), nLength);
        int	nRemainingLength = nConstrainedLength;
        short	nValue=0;
        long len=0;
        if(nLength < totalBytes) len=nLength;
        else len= totalBytes;

        for(int i=0;i<len;i+=2)
        {

            //little endian
            nValue = getValue();
            position++;
            abData[i] = (byte) (nValue & 0xFF);
            abData[i + 1] = (byte) ((nValue >>> 8) & 0xFF);


        }

        totalBytes -= len;

        //System.out.print("READ: totalBytes="+totalBytes+" Pos="+position+" ----- ");


        return (int)len;
    }





    /**
     * Считает значение для данного набора частот
     * @return
     */
   private short getValue()
    {
        //переключим текущий набор частот при переходе через время. Тут мы учитываем и надеемся на то что у нас верно расчитаны размеры и мы не получим ислючение о конце списка!!!!
        if(position > currentFreq.getEndPositionFreqInSamples())   currentFreq=fk.getFreqs().get( fk.getFreqs().indexOf(currentFreq)+1 );
        double res=0;
        for(int i=0;i<currentFreq.getKoeff().length;i++)res+=currentFreq.getAm() * fastSin(currentFreq.getKoeff()[i] * position);
      return   (short)Math.round(res);

    }





    private static void out(String strMessage)
    {
        System.out.println(strMessage);
    }



}