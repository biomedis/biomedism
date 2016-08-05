package ru.biomedis.biomedismair3.utils.Files;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * Created by Anama on 27.10.2015.
 */
@Data
@AllArgsConstructor
public class ProgramFileData
{
    private  long id;
    private long idComplex;
    private long timeForFreq;
    private String  uuid;
    private String freqs;
    private String name;
    private File txtFile;
    private File bssFile;
    private boolean mp3;
    private int bundlesLenght;

}
