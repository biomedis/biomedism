package ru.biomedis.biomedismair3.utils.Other;

/**
 * Created by Anama on 08.09.2015.
 */
public class TimeMesure
{
    private   long startTime;
    private long timeSpent;
    private  String name;

    public TimeMesure(String name) {
        this.name = name;
    }

    public  void start()
    {
        startTime = System.currentTimeMillis();
    }

    public  void stop(){
         timeSpent = System.currentTimeMillis() - startTime;
        System.out.println("Время выполнения  -" +name+" " + timeSpent + " миллисекунд");
    }
}
