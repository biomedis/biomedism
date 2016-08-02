package ru.biomedis.biomedismair3.utils.Files;

/**
 * Created by Anama on 29.10.2015.
 */
public class OldComplexTypeException extends Exception
{
        String complexName;

    public OldComplexTypeException(String message, String complexName) {
        super(message);
        this.complexName = complexName;
    }

    public String getComplexName() {
        return complexName;
    }
}
