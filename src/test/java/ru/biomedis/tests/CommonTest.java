package ru.biomedis.tests;

import org.junit.*;

import java.util.Calendar;


public class CommonTest {



    @Test
    public void calendarTest(){
        Assert.assertEquals(true, Calendar.getInstance() == Calendar.getInstance());
    }
}
