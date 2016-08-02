/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.Tests.TestsFramework;

import ru.biomedis.biomedismair3.App;

/**
 *
 * @author Anama
 */
public abstract class TestItem implements ITestItemContent
{
   private String name;
   private boolean ignore;






   
   
    public TestItem(String name, boolean ignore) {
        this.name = name;
        this.ignore = ignore;
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
   
    
    public void run(App context)
    {
           System.out.println(" [TEST-ITEM "+ this.getName()+" ]");
        try
        {
            
          payload(context);
            
        }catch(AssertionError ae)
        {
            
            
            
            System.out.println("    Message: " +ae.getMessage());   
           
            
            
           
        }catch(Exception ex){ex.printStackTrace();}
        finally
        {
            System.out.println(" [/TEST-ITEM]");
            System.out.println("");
            
        }
       
    }
    
    
}
