package ru.biomedis.biomedismair3.utils.Files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anama on 27.10.2015.
 */
public class ProgramFileData
{
   private  long id;
   private String  uuid;
   private long idComplex;
   private long timeForFreq;
   private String freqs;
    private File txtFile;
    private File bssFile;
    private String name;
    private boolean mp3;


    public ProgramFileData(long id, long idComplex, long timeForFreq, String uuid, String freqs,String name, File txtFile,File bssFile,boolean mp3) {
        this.id = id;
        this.idComplex = idComplex;
        this.timeForFreq = timeForFreq;
        this.uuid = uuid;
        this.freqs=freqs;
        this.txtFile=txtFile;
        this.bssFile=bssFile;
        this.name=name;
        this.mp3=mp3;

    }


    public boolean isMp3()
    {
        return mp3;
    }

    public void setMp3(boolean mp3)
    {
        this.mp3 = mp3;
    }

    public File getBssFile() {
        return bssFile;
    }

    public void setBssFile(File bssFile) {
        this.bssFile = bssFile;
    }

    public File getTxtFile() {
        return txtFile;
    }

    public void setTxtFile(File txtFile) {
        this.txtFile = txtFile;
    }

    public String getFreqs() {
        return freqs;
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdComplex() {
        return idComplex;
    }

    public void setIdComplex(long idComplex) {
        this.idComplex = idComplex;
    }

    public long getTimeForFreq() {
        return timeForFreq;
    }

    public void setTimeForFreq(long timeForFreq) {
        this.timeForFreq = timeForFreq;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setFreqs(String freqs) {
        this.freqs = freqs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
