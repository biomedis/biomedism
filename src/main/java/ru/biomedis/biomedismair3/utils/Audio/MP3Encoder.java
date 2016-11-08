package ru.biomedis.biomedismair3.utils.Audio;


import javafx.concurrent.Task;
import javafx.stage.Modality;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Profile;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.utils.Audio.Generators.SineOscillatorProgram;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;
import ru.biomedis.biomedismair3.utils.Other.TimeMesure;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.Log.logger;

//TODO хорошо бы при импорте все , заменитт на . в частотах
/**
 * Created by Anama on 30.09.2015.
 */
public class MP3Encoder extends Task<Boolean>
{


private static final boolean debug=false;
    public static enum CODEC_TYPE{EXTERNAL_CODEC,INTERNAL_CODEC};
    private CODEC_TYPE codecType;
    private int sampleRate;
    private Profile encodingProfile;
    private ActionListener actionListener;
    private String currentName="";
    private List<TherapyProgram> toGenProgram;
    private boolean getProfile = true;


    public synchronized String getCurrentName(){return currentName;}
    private  synchronized void setCurrentName(String val){currentName=val;}

    private String codecPath="";

    public MP3Encoder(Profile encodingProfile,CODEC_TYPE codecType,int sampleRate)
    {
        this.encodingProfile = encodingProfile;
        this.codecType=codecType;
        this.sampleRate= sampleRate;



        if(OSValidator.isWindows()) codecPath= App.getDataDir_()+"\\codec\\lame.exe";
        else if(OSValidator.isMac()) codecPath="."+File.separator+"codec"+File.separator+"lame_mac";
        else if(OSValidator.isUnix()) codecPath="lame";




            ModelDataApp mda= BaseController.getApp().getModel();
        try {
           String p= mda.getOption("codec.path");
            if(!p.isEmpty()) codecPath=p;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public MP3Encoder(List<TherapyProgram> toGenProgram, MP3Encoder.CODEC_TYPE codecType, int sampleRate) {
        this.toGenProgram = toGenProgram;
        this.getProfile = false;
        this.codecType = codecType;
        this.sampleRate = sampleRate;

        if(OSValidator.isWindows()) codecPath= App.getDataDir_()+"\\codec\\lame.exe";
        else if(OSValidator.isMac())  codecPath="."+File.separator+"codec"+File.separator+"lame_mac";
        else if(OSValidator.isUnix()) codecPath="lame";

        ModelDataApp mda = BaseController.getApp().getModel();

        try {
            String e = mda.getOption("codec.path");
            if(!e.isEmpty()) {
                this.codecPath = e;
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private boolean encode(CODEC_TYPE codecType,int sampleRate,TherapyProgram programm)
   {
        boolean res=false;
       FreqsContainer fk=new FreqsContainer(programm.getFrequencies(),programm.getTherapyComplex().getTimeForFrequency(),sampleRate,programm.getTherapyComplex().isMulltyFreq(),programm.getTherapyComplex().getBundlesLength(),16);

       System.out.print("Temp File Generating...");
       File	outputWavFile;
       if(OSValidator.isMac())outputWavFile = new File("/var/tmp/temp.wav");
       else if(OSValidator.isWindows())outputWavFile = new File(App.getTmpDir_()+"\\temp.wav");
       else outputWavFile = new File("/var/tmp/temp.wav");

       TimeMesure tm=new TimeMesure("WAV");
       tm.start();
       AudioInputStream oscillator = new SineOscillatorProgram(fk);

       try {
           AudioSystem.write(oscillator, AudioFileFormat.Type.WAVE, outputWavFile);
           res=true;
       } catch (IOException e) {
           System.out.println("FAIL");
           logger.error("",e);
        res=false;
       }catch (Exception e)
       {
           System.out.println("FAIL");
           logger.error("",e);
           res=false;
       }
       finally {

       }
       tm.stop();

       if(res==false) return false;
       System.out.println("OK");

       if(isStop())return true;//остановка по требованию

    try {
        System.out.print("MP3 File Generating...");
        String fName;

        if(OSValidator.isWindows())fName=App.getDataDir_()+ "\\"+ programm.getId() + ".dat";
        else fName="." + File.separator +"data"+ File.separator + programm.getId() + ".dat";

       switch (codecType)
        {
            case EXTERNAL_CODEC:



              if(wavToMP3_128(outputWavFile.getAbsolutePath(), fName)!=0) res=false;
                else res=true;


                break;
            case INTERNAL_CODEC:


                break;

        }
        File file = new File(fName);
        if(file.exists()) res=true;
        else  res=false;

    }catch (Exception e)
    {
        System.out.println("FAIL");
        logger.error("",e);
        BaseController.showErrorDialog("Ошибка", "", "Возможно указан не правильный путь к исполняемому файлу кодека!", BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);
        res=false;
    }

    finally {
       if(outputWavFile.exists())outputWavFile.delete();
        if(res) System.out.println("OK");
        else  System.out.println("FAIL");

    }
    return res;
   }

    private  int  wavToMP3(String waveFileName, String mp3FileName, String param) throws Exception
    {
        Runtime runtime = Runtime.getRuntime();
        File lame=new File(codecPath);
        Process proc = null;
               if(OSValidator.isWindows()) proc = runtime.exec(lame.getAbsolutePath()+" "+param+"  --silent  "+waveFileName+" "+mp3FileName);
                else if(OSValidator.isMac() )   proc = runtime.exec(codecPath +" "+param+"  --silent "+waveFileName+" "+mp3FileName);
               else if( OSValidator.isUnix())  proc =runtime.exec(codecPath +" "+param+"  --silent "+waveFileName+" "+mp3FileName);
                else {BaseController.showErrorDialog("Ошибка","","Операционная система не поддерживается",BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);throw new RuntimeException();}
        InputStream stderr = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        while ( (line = br.readLine()) != null)
        {
            if(debug)System.out.println(line);
        }


        int exitVal = proc.waitFor();
        if(debug)System.out.println("Process exitValue: " + exitVal);
        return exitVal;


    }

    private  int wavToMP3_128(String waveFileName, String mp3FileName) throws Exception
    {

        Runtime runtime = Runtime.getRuntime();
        File lame=new File(codecPath);

        Process proc = null;
        if(OSValidator.isWindows())  proc =runtime.exec(lame.getAbsolutePath()+" --preset 128  --silent  "+waveFileName+" "+mp3FileName);
        else if(OSValidator.isMac())   proc = runtime.exec(codecPath +" --preset 128  --silent "+waveFileName+" "+mp3FileName);
        else if( OSValidator.isUnix()) proc = runtime.exec(codecPath +" --preset 128  --silent "+waveFileName+" "+mp3FileName);

        else {BaseController.showErrorDialog("Ошибка","","Операционная система не поддерживается",BaseController.getApp().getMainWindow(), Modality.WINDOW_MODAL);throw new RuntimeException();}

        InputStream stderr = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        while ( (line = br.readLine()) != null)
        {
            if(debug)System.out.println(line);
        }


        int exitVal = proc.waitFor();
        if(debug)System.out.println("Process exitValue: " + exitVal);



      return exitVal;

    }


    @Override
    protected Boolean call() throws Exception
    {
        //получим список программ для генерации

        boolean res=true;
        ModelDataApp mda= BaseController.getApp().getModel();
        List<TherapyProgram> progs ;
        if(getProfile)progs = mda.findNeedGenerateList(encodingProfile).stream().filter(s->!s.isMp3()).collect(Collectors.toList());
        else progs=toGenProgram;

        long progress=0;
       if(progs.size()!=0) {
           setCurrentName(progs.get(0).getName());
           updateProgress(0, 100);
       }
        for(int i=0;i<progs.size();i++)
        {
            setCurrentName(progs.get(i).getName());
            if(i==(progs.size()-1) ) progress=100;
            else progress=  (long)Math.floor( (  (double)(i+1) / (double)progs.size() )*100);

            if(encode(this.codecType,sampleRate,progs.get(i)) ==false)
            {
                this.failed();
                return false;
            }
            if(isStop())
            {
                res=true;
                if (actionListener!=null)actionListener.onProgramEncoded(progs.get(i).getId(),true);
                break;
            }else
            {
                if (actionListener!=null)actionListener.onProgramEncoded(progs.get(i).getId(),false);
            }

            updateProgress(progress,100);
        }




        return res;
    }
    private boolean stop=false;




    synchronized public boolean isStop() {
        return stop;
    }

    synchronized  private void setStop(boolean stop) {
        this.stop = stop;
    }

    public void stopEncode()
    {

        setStop(true);

    }

    public void removeActionListener(){actionListener=null;}
    public void setActionListener(ActionListener a){actionListener=a;}

   public interface ActionListener
   {
       /**
        *
        * @param id тер.программы
        * @param isCanceled  если была отменена обработка
        */
       public void onProgramEncoded(long id,boolean isCanceled);
   }


}
/*
 File	outputFile = new File("temp.wav");
        int fSampleRate=44100;
       int  nDuration=180;



        AudioInputStream	oscillator = new SineOscilator(17000, 0.99F,fSampleRate, Math.round(nDuration * fSampleRate));

        try {
            AudioSystem.write(oscillator, AudioFileFormat.Type.WAVE, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }


 int bufferSize = 4096*2;
        byte buffer[] = new byte[bufferSize];



        SineOscilator so=new SineOscilator(15000,0.9F,44100,Math.round(3*60 * 44100));
        //Oscillator so=new Oscillator(Oscillator.WAVEFORM_SINE,1000,0.9F,44100,3*60 * 44100);
        //quality setting 0=best, 9=worst default=5

        LameEncoder encoder=new LameEncoder( so.getFormat(),128000, MPEGMode.MONO,0,false);

        byte[] encoded = new byte[encoder.getMP3BufferSize()];

        TimeMesure tm=new TimeMesure("Dhtvz");
        try(  BufferedOutputStream fis=new BufferedOutputStream(new FileOutputStream("res.mp3")) ) {
            int len=0;

            tm.start();
            while ( (len= so.read(buffer, 0, buffer.length))>0)
            {
                //System.out.print("read-lean = " + len + " avaliable()=" + so.available());

                int encodedCount = encoder.encodeBuffer(buffer, 0,  len, encoded);
                //System.out.println(" , encodedCount = "+encodedCount);
                fis.write(encoded, 0, encodedCount);
            }
            int encodedCount = encoder.encodeFinish(encoded);

                fis.write(encoded, 0, encodedCount);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            tm.stop();
            encoder.close();

        }
 */