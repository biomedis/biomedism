/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.Tests.TestsFramework;
import java.lang.annotation.*;

/**
 *
 * @author Anama
 */
@Target(value=ElementType.TYPE)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface TestClass {
    
    /**
     * Имя набора тестов
     * @return 
     */
    String name();
    /**
     * Игнорировать или нет набор
     * @return 
     */
    boolean ignore() default false;
    
    
}
