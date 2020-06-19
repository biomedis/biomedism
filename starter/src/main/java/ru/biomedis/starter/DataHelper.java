package ru.biomedis.starter;


import org.anantacreative.updater.Pack.Exceptions.PackException;
import org.anantacreative.updater.Pack.Packer;


import javax.persistence.EntityManager;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.anantacreative.updater.VersionCheck.Version;

public class DataHelper {


    private static App getApp(){
       return BaseController.getApp();
    }


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

        }else if(OSValidator.isMac()){
            dbDir = rootDirApp;
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
        int updateFixVersion ;
        try {
            updateFixVersion = selectOptionIntValue("updateFixVersion");
        }catch (Exception e) {
            updateFixVersion =0;
        }
        return updateFixVersion;
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

    /**
     * Список доступных языков программы
     * @return
     * @throws Exception
     */
    public static List<String> selectAvailableLangs() throws Exception {
        EntityManager entityManager=null;
        List<String> res;
        try
        {
            entityManager = getApp().getEntityManagerFactory().createEntityManager();
            res = entityManager
                    .createNativeQuery("SELECT l.abbr FROM `LANGUAGE` as l WHERE l.AVALIABLE  = ?")
                    .setParameter(1,true)
                    .getResultList();

        }catch (Exception e){
            throw new Exception("Ошибка получения языков",e);

        }finally {
            if(entityManager!=null)entityManager.close();
        }


        return res;
    }

}
