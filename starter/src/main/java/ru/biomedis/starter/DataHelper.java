package ru.biomedis.starter;


import org.anantacreative.updater.Pack.Exceptions.PackException;
import org.anantacreative.updater.Pack.Packer;
import org.anantacreative.updater.Version;

import javax.persistence.EntityManager;
import java.io.File;
import java.util.Calendar;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataHelper {


    private static App getApp(){
       return BaseController.getApp();
    }

    //TODO доработать для Мак
    public static void ZipDBToBackup() throws PackException {

        File rootDirApp = AutoUpdater.getAutoUpdater().getRootDirApp();
        File backupDir = new File(rootDirApp, "backup_db");
        if (!backupDir.exists()) backupDir.mkdir();
        File dbDir = null;
        if (AutoUpdater.isIDEStarted()) {
            dbDir = rootDirApp;
        } else if (OSValidator.isUnix()) {
            dbDir = new File(rootDirApp, "app");

        } else if (OSValidator.isWindows()) {
            dbDir = new File(rootDirApp, "assets");

        }

        Packer.packFiles(Stream.of(dbDir.listFiles((dir, name) -> name.endsWith(".db"))).collect(Collectors.toList()),
                new File(backupDir, Calendar.getInstance().getTimeInMillis() + ".zip"));


    }

    /**
     * Получает значение версии обновления. Если ее вообще нет создасто нулевую
     * Установит значение в  this.updateVersion
     * @return вернет созданную или полученную опцию
     */
    public static Version selectUpdateVersion() throws Exception {
        return new Version(4,selectMinor(),selectFix());
    }


    private static  int selectFix() throws Exception {
        return selectOptionIntValue("updateFixVersion");
    }

    private static  int selectMinor() throws Exception {
        return selectOptionIntValue("updateVersion");
    }

    /**
     * Извлекает опцию ште по имени
     * @param name
     * @return
     * @throws Exception
     */
    private static int  selectOptionIntValue(String name) throws Exception {
        EntityManager entityManager=null;
        Integer res;
        try
        {
            entityManager = getApp().getEntityManagerFactory().createEntityManager();
            String minor = (String)entityManager
                    .createNativeQuery("SELECT p.value FROM PROGRAMOPTIONS as p WHERE p.name = ?")
                    .setMaxResults(1)
                    .setParameter(1,name)
                    .getSingleResult();

            res = Integer.valueOf(minor);


        }catch (Exception e){
            throw new Exception("Ошибка получения версии",e);

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }

    /**
     * Извлекает опцию boolean по имени
     * @param name
     * @return
     * @throws Exception
     */
    private static boolean  selectOptionBooleanValue(String name) throws Exception {
        EntityManager entityManager=null;
        Boolean res;
        try
        {
            entityManager = getApp().getEntityManagerFactory().createEntityManager();
            String minor = (String)entityManager
                    .createNativeQuery("SELECT p.value FROM PROGRAMOPTIONS as p WHERE p.name = ?")
                    .setMaxResults(1)
                    .setParameter(1,name)
                    .getSingleResult();

            res = Boolean.valueOf(minor);


        }catch (Exception e){
            throw new Exception("Ошибка получения версии",e);

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }



}
