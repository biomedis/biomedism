/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *ИСПОЛЬЗОВАТЬ ТОЛЬКО НА ПУСТОЙ БАЗЕ - ТК ДАННЫЕ В БАЗЕ ПЕРЕЗ ТЕСТОМ УНИЧТОЖАЮТСЯ!!!
 * @author Anama
 */
public class ModelDataAppTest {
    
    public ModelDataAppTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Начало тестов  ModelDataApp");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
     public void hello() 
     {
         System.err.println("Hello test");
         //assertTrue("Фигня", false);
     }
}
