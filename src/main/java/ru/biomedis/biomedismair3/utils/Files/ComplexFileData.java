package ru.biomedis.biomedismair3.utils.Files;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * Created by Anama on 27.10.2015.
 */
@Data
@AllArgsConstructor
public class ComplexFileData
{
    private long id;//-1 для пустой папки в которой не обнаружены программы
    private String name;
    private long timeForFreq;
    private boolean mullty;
    private int bundlesLength;
    private File file;




}
