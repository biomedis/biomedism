package ru.biomedis.biomedismair3.utils.Audio.Generators;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Anama on 05.10.2015.
 */
public class SineOscilator extends AudioInputStream
{

    private int position=0;//позиция в short не в байтах
    private int totalBytes;
    private double Am= Short.MAX_VALUE/2.0;
    private double PI2=2*Math.PI;
    private double koeff=0;


    /**
     * Выдает 16 бит мого PCM_SIGNED little-endian
     * @param fSignalFrequency
     * @param fAmplitude
     * @param sampleRate
     * @param lLength
     */
    public SineOscilator(float fSignalFrequency, float fAmplitude, int sampleRate, int lLength)
    {
        super(new ByteArrayInputStream(new byte[0]),new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sampleRate,16,1,2,sampleRate,false),lLength);


        totalBytes = lLength*2;//2 байта на значение
        Am =  (fAmplitude * Math.pow(2, getFormat().getSampleSizeInBits() - 1));
        koeff=PI2 * fSignalFrequency  / (double)sampleRate;






            /// System.out.println(fValue);





    }

    public AudioFormat getAudioFormat()
    {
        return  this.getFormat();
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
        int len=0;
        if(nLength < totalBytes) len=nLength;
        else len= totalBytes;

        for(int i=0;i<nLength;i+=2)
        {

            //little endian
            nValue = (short)Math.round(Am * Math.sin(koeff * (position++)));

            abData[i] = (byte) (nValue & 0xFF);
            abData[i + 1] = (byte) ((nValue >>> 8) & 0xFF);


        }

            totalBytes -= len;

       // System.out.print("READ: totalBytes="+totalBytes+" Pos="+position+" ----- ");


        return len;
    }



    private static void out(String strMessage)
    {
        System.out.println(strMessage);
    }
}
