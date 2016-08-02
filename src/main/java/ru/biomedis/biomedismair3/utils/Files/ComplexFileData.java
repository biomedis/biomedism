package ru.biomedis.biomedismair3.utils.Files;

import java.io.File;

/**
 * Created by Anama on 27.10.2015.
 */

public class ComplexFileData
{
    private long id;//-1 для пустой папки в которой не обнаружены программы
    private String name;
    private long timeForFreq;
    private File file;
    private boolean mullty;

    public ComplexFileData(long id, String name, long timeForFreq,boolean mullty, File file) {
        this.id = id;
        this.name = name;
        this.timeForFreq = timeForFreq;
        this.file=file;
        this.mullty=mullty;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeForFreq() {
        return timeForFreq;
    }

    public void setTimeForFreq(long timeForFreq) {
        this.timeForFreq = timeForFreq;
    }

    public boolean isMullty() {
        return mullty;
    }

    public void setMullty(boolean mullty) {
        this.mullty = mullty;
    }
}
