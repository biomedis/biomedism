/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.Tests.TestsFramework;


import ru.biomedis.biomedismair3.App;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 *Моэно исользовать ассерты Junit
 *тесты исолняются в порядке добавления testItem
 * @author Anama
 */
public class BaseTest  
{
    private App app;
    private List<TestItem> testItems=new ArrayList<>();
    private String name;
    private boolean ignore=false;
    
    
    public BaseTest()
    {      
      
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

   
    
    
    
    protected void addTestItem(TestItem itm)
    {
        testItems.add(itm);

    }
    
    public void runTests()
    {
        if(ignore)return;
        
        System.out.println("");
        System.out.println("");
        System.out.println("[TEST "+ this.getName()+" ]"); 
        System.out.println("");
        assertNotNull("Контекст приложения is null", app);
        assertNotNull("Отсутствует модель is null", app.getModel());
            
          for(TestItem ti : testItems)if(!ti.isIgnore())ti.run(app);
            
        
        System.out.println("[/TEST]");
        System.out.println("");
        System.out.println("");
        
       
        
       
    }
     
    
    static public void message(String message)
    {
        System.out.println("  "+message);
    }
    
  
}
