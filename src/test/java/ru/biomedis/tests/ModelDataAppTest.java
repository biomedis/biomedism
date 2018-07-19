package ru.biomedis.tests;

import org.junit.*;

import java.util.Calendar;


public class ModelDataAppTest {
    

    @Test
     public void hello() 
     {
         Assert.assertEquals(true, Calendar.getInstance() == Calendar.getInstance());
     }


}
