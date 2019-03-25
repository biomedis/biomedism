package ru.biomedis.tests;


import org.junit.Assert;
import org.junit.Test;
import ru.biomedis.biomedismair3.m2.LanguageDevice;

public class CommonTests {
    @Test
    public void CodePointTest(){
        Assert.assertEquals("en",LanguageDevice.langByCodePoint("??? ??? sf").getAbbr());
    }
}
