package ru.biomedis.biomedismair3.utils.Disk;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.utils.OS.ExecCommand;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;



@Slf4j
public class DiskDetector
{

    private  static  String whoami="";
    static {
        if(OSValidator.isUnix()) {
            try {
                whoami = ExecCommand.execCmd("whoami");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Имя пользователя в системе Linux
     * @return
     */
    public static String whoami(){return whoami;}


            private static  List<FileStore> getAllFileStores()
            {
                 List<FileStore> arr=new ArrayList<>();

                for (FileStore fileStore : FileSystems.getDefault().getFileStores())arr.add(fileStore);
            return arr;

            }

    /**
     * Список примонтированных устройств, пути их корневым директориям
     * @return
     * @throws Exception
     */
    public static List<Path> getAllDisk() throws Exception
    {
        List<Path> disks=new ArrayList<>();
        Path tPath=null;
        for (FileStore fileStore : FileSystems.getDefault().getFileStores())
        {

            tPath= getRootPath(fileStore);
           if(tPath!=null) disks.add(tPath);
        }

        return disks;
    }


    public static List<FileStore> getAllSore() throws Exception
    {
        List<FileStore> disks=new ArrayList<>();

        for (FileStore fileStore : FileSystems.getDefault().getFileStores())
        {


            if( isRootPath(fileStore) ) disks.add(fileStore);
        }

        return disks;
    }
    /**
     * Получить по пути FileStore объект. Позволит получить данные о диске
     * @param path
     * @return
     */
public static FileStore getStorage(Path path)
{
    FileStore ret=null;
    try {
        for (FileStore fs : FileSystems.getDefault().getFileStores()) {
            if (Files.getFileStore(path).equals(fs)) {
                ret = fs;
                break;
            }
        }
    }catch (Exception ex)
    {
        log.error("",ex);
    }
return ret;
}


    /**
     * ПРоблема в том что из FileStore не получить путь и приходиться его выковыривать вот так
     * @param fs
     * @return
     * @throws Exception
     */
    public   static Path getRootPath(FileStore fs) throws Exception {




        if(OSValidator.isWindows()) {

            // Windows
            Exception ex = null;
            for (Path p : FileSystems.getDefault().getRootDirectories()) {
                try {
                    if (Files.getFileStore(p).equals(fs)) {
                        return p;
                    }
                } catch (Exception e) {
                    ex = e;
                }
            }
            if (ex != null) {
                log.error("Ощибка определения ОС. Ожидался Windows",ex);
                throw ex;
            }

        }else if(OSValidator.isMac())
        {

            Path media = Paths.get("/Volumes");
            if (media.isAbsolute() && Files.exists(media)) { // OS X
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                    for (Path p : stream) {
                        if (Files.getFileStore(p).equals(fs)) {
                            return p;
                        }
                    }
                }
            }else {

                throw new Exception("Ощибка определения ОС. Ожидался OS X");
            }


        }else if(OSValidator.isUnix())
        {

            Path media = Paths.get("/media");
            boolean flag = media.isAbsolute() && Files.exists(media);
            if(media.isAbsolute() && Files.exists(media))
            {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                    for (Path p : stream) {
                        if (Files.getFileStore(p).equals(fs)) {
                            return p;
                        }
                    }


                }
                //если не нашли, посмотрим в поддиректории с именем пользователя

                try{
                    media=Paths.get("/media/"+whoami());
                }catch (InvalidPathException e){
                    return null;
                }
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                    for (Path p : stream) {
                        if (Files.getFileStore(p).equals(fs)) {
                            return p;
                        }
                    }


                }
            }
            else
            {

                media=Paths.get("/mnt");

                if(media.isAbsolute() && Files.exists(media)){
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                        for (Path p : stream) {
                            if (Files.getFileStore(p).equals(fs)) {
                                return p;
                            }
                        }
                    }
                }else   if(!flag) throw new Exception("Ощибка определения ОС. Ожидался Linux");
            }




        }



        return null;
    }


    private  static boolean isRootPath(FileStore fs) throws Exception {

        if(OSValidator.isWindows()){

            // Windows
            Exception ex = null;
            for (Path p : FileSystems.getDefault().getRootDirectories()) {
                try {
                    if (Files.getFileStore(p).equals(fs)) {
                        return true;
                    }
                } catch (Exception e) {
                    ex = e;
                }
            }
            if (ex != null) {
                log.error("Ощибка определения ОС. Ожидался Windows",ex);
                throw ex;
            }

        }else if(OSValidator.isMac())
        {

            Path media = Paths.get("/Volumes");
            if (media.isAbsolute() && Files.exists(media)) { // OS X
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                    for (Path p : stream) {
                        if (Files.getFileStore(p).equals(fs)) {
                            return true;
                        }
                    }
                }
            }else {

                throw new Exception("Ощибка определения ОС. Ожидался OS X");
            }


        }
        else if(OSValidator.isUnix())
        {


            Path media = Paths.get("/media");
            boolean flag = media.isAbsolute() && Files.exists(media);
            if(media.isAbsolute() && Files.exists(media))
            {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                    for (Path p : stream) {
                        if (Files.getFileStore(p).equals(fs)) {
                            return true;
                        }
                    }


                }
                //если не нашли, посмотрим в поддиректории с именем пользователя

               try{
                   media=Paths.get("/media/"+whoami());
               }catch (InvalidPathException e){
                   return false;
               }
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                    for (Path p : stream) {
                        if (Files.getFileStore(p).equals(fs)) {
                            return true;
                        }
                    }


                }
            }
            else
            {

                media=Paths.get("/mnt");

                if(media.isAbsolute() && Files.exists(media)){
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                        for (Path p : stream) {
                            if (Files.getFileStore(p).equals(fs)) {
                                return true;
                            }
                        }
                    }
                }else   if(!flag) throw new Exception("Ощибка определения ОС. Ожидался Linux");
            }






        }
        return false;
    }

    public static DiskSpaceData getDiskSpace(FileStore fs)
    {
        DiskSpaceData ds=null;

        try {
            long totalSpace = fs.getTotalSpace() ;
            long usableSpace = fs.getUsableSpace();
            long usedSpace = (fs.getTotalSpace() - fs.getUnallocatedSpace());
             ds=new DiskSpaceData(totalSpace,usableSpace,usedSpace);

        }catch (Exception ex)
        {
            log.error("",ex);
            ds=null;
        }
        return ds;
    }


   /******* Детектирование  подключения*******/

   private static boolean stopService=false;
    private static boolean statusService=false;

   synchronized public static void stopDetectingService()
    {
        stopService=true;
    }
    synchronized  private static boolean isStop()
    {
        return stopService;
    }


    /**
     * Следит за изменениями  колличества подключенных дисков
     * @param periodSec
     * @param changeaction
     * @throws Exception
     */
    public static void waitForNotifying(int periodSec,Runnable changeaction)  throws Exception
    {
        if(statusService) throw new Exception("Сервис уже запущен");



        Thread  t = new Thread(new Runnable() {
            public void run()
            {
                List<Path> drivesPath=null;
                try
                {
                    drivesPath=getAllDisk();

                } catch (Exception ex) {
                    log.error("",ex);
                    return;
                }

                while (true)
                {
                    try {
                        if (Thread.interrupted())throw new InterruptedException();
                        if(isStop())throw new InterruptedException();
                        Thread.sleep(periodSec * 1000);

                        //если у нас изменилось колличество дисков
                       if(drivesPath.size()!=getAllDisk().size())
                       {
                           drivesPath=getAllDisk();
                           changeaction.run();
                       }


                    } catch (InterruptedException e) {
                        log.error("",e);
                        statusService=false;
                        return;
                    }catch (Exception ex)
                    {
                        log.error("",ex);
                        ex.printStackTrace();
                        statusService=false;
                        return;
                    }





                }
            }
        });

        stopService=false;
        t.start();
    }


    private static String nameDiskStore="";

    synchronized  private static String getNameDiskStore(){return nameDiskStore;}
   synchronized public static void setNameDiskStore(String name){ nameDiskStore=name;}

    /**
     * Следит за   изменением состояния конкретного диска конкретного диска с заданным именем.
     * @param periodSec
     * @param action выполниться при откл и подкл данного диска nameDisk
     * @param nameDisk имя диска ( не буква)
     *  @param         action вызовется при изменении состояния указанного диска, передастся true для наличия.
     * @throws Exception
     */
    public static void waitForDeviceNotifying(int periodSec,String nameDisk, BiConsumer<Boolean,FileStore> action)  throws Exception
    {
        if(statusService) throw new Exception("Сервис уже запущен");

        setNameDiskStore(nameDisk);

        Thread  t = new Thread(new Runnable() {
            public void run() {

               boolean status=false;
                try {
                    //проверим сразу при подключении

                    List<FileStore> allFileStores = getAllFileStores();

                    long count = allFileStores.stream().filter(fileStore -> fileStore.name().contains(getNameDiskStore()) || fileStore.name().equalsIgnoreCase(getNameDiskStore()) || fileStore.toString().contains(getNameDiskStore())).count();

                    if (count != 0) {
                        //обнаружен прибор, вернем еще его FileStore
                        status=true;
                        List<FileStore> collect = allFileStores.stream().
                                filter(fileStore -> fileStore.name().equalsIgnoreCase(getNameDiskStore()) || fileStore.name().contains(getNameDiskStore()) || fileStore.toString().contains(getNameDiskStore())).collect(Collectors.toList());
                        if(collect!=null ? !collect.isEmpty(): false)  action.accept(true,collect.get(0));
                       else  action.accept(true,null);

                    } else {
                        status=false;
                        action.accept(false,null);
                    }
                }catch (Exception ex)
                {
                    log.error("",ex);
                    return;
                }


                while (true)
                {
                    try {
                        if (Thread.interrupted())throw new InterruptedException();
                        if(isStop())throw new InterruptedException();
                        Thread.sleep(periodSec * 1000);

                        List<FileStore> allFileStores = getAllFileStores();
                        long count = allFileStores.stream().filter(fileStore ->


                             fileStore.name().equalsIgnoreCase(getNameDiskStore()) || fileStore.name().contains(getNameDiskStore()) || fileStore.toString().contains(getNameDiskStore())
                        ).count();

                        if(count!=0)
                        {

                            //обнаружен прибор, вернем еще его FileStore
                            status=true;

                            List<FileStore> collect = allFileStores.stream().
                                    filter(fileStore -> fileStore.name().contains(getNameDiskStore()) || fileStore.name().equalsIgnoreCase(getNameDiskStore()) || fileStore.toString().contains(getNameDiskStore())).collect(Collectors.toList());
                            if(collect!=null ? !collect.isEmpty(): false)  action.accept(true,collect.get(0));
                            else  action.accept(true,null);
                        }
                        else
                        {
                            if(status==true)action.accept(false,null);//сообщени об отключении
                            status=false;
                        }


                    } catch (InterruptedException e) {
                        log.error("",e);
                        statusService=false;
                        return;
                    }catch (Exception e)
                    {
                        log.error("",e);
                        statusService=false;
                        return;
                    }





                }
            }
        });

        stopService=false;
        t.setDaemon(true);
        t.start();
    }
}
