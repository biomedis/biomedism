/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.Tests.TestsFramework;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;
import ru.biomedis.biomedismair3.App;

/**
 *
 * @author Anama
 */
public class TestsManager
{
    private App app;
    private String testsPackage;
    private Map<String, BaseTest> testsmap=new HashMap<>();
/**
 * Все классы производные от BaseTest должны аннотироваться @TestClass
 * @param app Задаем контекст приложения
 * @param testsPackage полное имя пакета где будут искаться тесты
 */
    public TestsManager(App app,String testsPackage)
    {
        this.app = app;
        
        this.testsPackage=testsPackage;
        
        Reflections reflections = new Reflections(testsPackage);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(TestClass.class);
        
         try
         {
             //найдем все аннатированные классы и инстанцируем их установив параметры.
             TestClass annotation=null;
                for (Class<?> clazz : annotated) {

                        BaseTest newInstance = (BaseTest)clazz.newInstance();
                        annotation = clazz.getAnnotation(TestClass.class);
                       
                        newInstance.setApp(app);
                        newInstance.setName(annotation.name());
                        newInstance.setIgnore(annotation.ignore());
                        
                        testsmap.put(annotation.name(), newInstance);
       

                    }
         } catch (InstantiationException ex) {
                Logger.getLogger(TestsManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(TestsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public Map<String, ? extends BaseTest> getTestsMap() {
        return testsmap;
    }
    
    /**
     * Запустит тесты набора name
     * @param name Имя набора тестов(имя передаваемое в аннотацию TestClass)
     */
    public void runtTest(String name)
    {
         if( getTestsMap().containsKey(name))
         {
             getTestsMap().get(name).runTests();
         }
    }
    
    /**
     * Запуститьь все тесты
     */
    public void runAllTests()
    {
       
        for(Map.Entry<String, ? extends BaseTest> itm :  getTestsMap().entrySet())
        {
            itm.getValue().runTests();
        }
        
    }

    
    
    
}
