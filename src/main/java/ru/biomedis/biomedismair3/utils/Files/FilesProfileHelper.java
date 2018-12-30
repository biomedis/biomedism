package ru.biomedis.biomedismair3.utils.Files;

import ru.biomedis.biomedismair3.Log;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Создание директорий, удаление директорий в папке profiles
 * Created by Anama on 13.10.2015.
 */
public class FilesProfileHelper
{


    /**
     * Возвратит список программа в папке комплекса, распознает автоматически старый и новый тип
     * @param dir
     * @return вернет null в случае ошибки
     */
    public static  Map<Long, ProgramFileData> getProgrammsFromComplexDir(File dir)
    {
        Map<Long, ProgramFileData> programms=null;
        try {
            programms = FilesProfileHelper.getProgramms(dir);
        } catch (OldComplexTypeException e)
        {
            try {
                programms = FilesOldProfileHelper.getProgramms(dir, 300);
            } catch (Exception e1) {
                return null;
            }
        }catch (Exception e){
            Log.logger.error("",e);return null;}
        return programms;
    }


    /**
     * Список директорий комплексов
     * @param diskPath путь к диску
     * @return
     */
    public static List<ComplexFileData> getComplexes(File diskPath) throws OldComplexTypeException {

        List<ComplexFileData> res=new ArrayList<>();
        // до конца рекурсивного цикла
        if (!diskPath.exists())  return null;
        File textFile=null;



        File[] listFiles = diskPath.listFiles(dir -> dir.isDirectory() && dir.canWrite());//директории + дб.доступны для записи

        List<File> files = Arrays.stream(listFiles).sorted((f1, f2) -> {

            String[] f1_split = f1.getName().split("-");
            String[] f2_split = f2.getName().split("-");

            if(f1_split.length==0 || f2_split.length==0) return 0;

            int i1 = Integer.parseInt(f1_split[0]);
            int i2 = Integer.parseInt(f2_split[0]);
            if(i1<i2) return -1;
            else if(i1>i2) return 1;
            else return 0;


        }).collect(Collectors.toList());

        for (File file : files)
        {
            //найдем первый попавшийся текстовый файл
            textFile=null;

            for (File file1 : file.listFiles((dir, name) -> name.contains(".txt")))
            {
                textFile=file1;
                break;
            }

            //пустая папка
            if(textFile==null)
            {
                res.add(new ComplexFileData(-1, file.getName(), 0,1, file,""));
                continue;
            }
            //прочитаем файл

            List<String> progrParam = new ArrayList<String>();
            try( Scanner in = new Scanner(textFile,"UTF-8"))
            {
                while (in.hasNextLine()) progrParam.add(in.nextLine().replace("@",""));
            }catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }


            if(progrParam.size() < 8){
                throw new OldComplexTypeException("Обнаружен комплекс старого формата",textFile.getName().substring(textFile.getName().length()));
            }else if(progrParam.size() == 8){
                //если 8 параметров, значит это файл с версии обновления 0. И не содержит 9 строкой длину пачки частот
                res.add(new ComplexFileData(Long.parseLong(progrParam.get(2)), file.getName(), Long.parseLong(progrParam.get(3)),3, file,""));
            }else {
                int bundles = Integer.parseInt(progrParam.get(8));
                if(bundles>7) bundles=7;
                res.add(new ComplexFileData(Long.parseLong(progrParam.get(2)),
                        file.getName(),
                        Long.parseLong(progrParam.get(3)),
                        bundles<2?3:bundles,
                        file,
                        progrParam.size()<12?"":progrParam.get(11)));

            }



        }

        return res;
    }


    /**
     * Программы выбранного комплекса из директории
     * @param complexDir
     * @return
     */
    public static Map<Long,ProgramFileData> getProgramms(File complexDir) throws OldComplexTypeException {
        Map<Long,ProgramFileData> res=new LinkedHashMap<>();

        if (!complexDir.exists())  return null;

        //найдем bss без текста и удалим.
        // Не будем удалять, а пусть такие bss будет на совести пользователя. Нехер копировать вручную в прибор что попало
        /*
        for (File file : complexDir.listFiles(dir -> dir.isFile()&&dir.getName().contains(".bss")))
        {
            File txtFile=new File(complexDir,file.getName().substring(0,file.getName().length()-4)+".txt");
            if(!txtFile.exists())file.delete();
        }
        */
        File[] listFiles = complexDir.listFiles(dir -> dir.isFile() && dir.getName().contains(".txt"));
        List<File> files = Arrays.stream(listFiles).sorted((f1, f2) -> {

            String[] f1_split = f1.getName().split("-");
            String[] f2_split = f2.getName().split("-");

            if(f1_split.length==0 || f2_split.length==0) return 0;

            int i1 = Integer.parseInt(f1_split[0]);
            int i2 = Integer.parseInt(f2_split[0]);
            if(i1<i2) return -1;
            else if(i1>i2) return 1;
            else return 0;


        }).collect(Collectors.toList());


        for (File file : files)
        {


            List<String> progrParam = new ArrayList<String>();
            try( Scanner in = new Scanner(file,"UTF-8"))
            {
                while (in.hasNextLine()) progrParam.add(in.nextLine().replace("@",""));
            }catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }

            int bundlesLength=1;
            //в обновлении 1 добавилось 9 поле, тут мы его игнорируем
            if(progrParam.size() < 8) throw new OldComplexTypeException("Обнаружен комплекс старого формата",complexDir.getName().substring(0,complexDir.getName().length()));
            else if(progrParam.size() > 8){
                bundlesLength=Integer.parseInt(progrParam.get(8));
            }

            File bssFile= new File(complexDir,file.getName().substring(0,file.getName().length()-4)+".bss");


            res.put(Long.parseLong(progrParam.get(0)), new ProgramFileData
                    (
                            Long.parseLong(progrParam.get(0)),
                            Long.parseLong(progrParam.get(2)),
                            Long.parseLong(progrParam.get(3)),
                            progrParam.get(1),
                            progrParam.get(6).replace(",","."),
                            progrParam.get(5),
                            file,
                            bssFile.exists() ? bssFile : (File)null,
                            Boolean.parseBoolean(progrParam.get(7)),
                            bundlesLength<2?3:bundlesLength,
                            progrParam.size()<10 ? true : Boolean.valueOf(progrParam.get(9)),
                            progrParam.size()<11 ? "" : progrParam.get(10),
                            progrParam.size()<12?"":progrParam.get(11)
                            ));

        }

        return res;

    }




    /**
     * Создаст текстовый файл с описанием программы
     * @param freqs
     * @param timeForFreq
     * @param idProgram
     * @param txtPath
     *  @param uuid
     *  @param mp3  mp3 программа
     *  @programmMulty мультичастотна ли программа
     * @throws Exception
     */
    public static void copyTxt(String freqs,int timeForFreq,long idProgram, String uuid,long idComplex, int bundlesLength, String nameProgram,
                               boolean mp3,File txtPath,boolean programmMulty,String srcUUID,String srcUUIDComplex) throws Exception {
        txtPath=new File(txtPath.toURI());

        try(PrintWriter writer = new PrintWriter(txtPath,"UTF-8"))
        {

            writer.println(idProgram);
            writer.println(uuid);
            writer.println(idComplex + "");
            writer.println(timeForFreq);
            writer.println("true");//оставлено для совместимости
            writer.println(nameProgram==null?"Unknown name":nameProgram);
            writer.println(freqs+"@");
            writer.println(mp3?"true":"false");
            writer.println(bundlesLength+"");
            writer.println(programmMulty?"true":"false");
            writer.println(srcUUID);
            writer.println(srcUUIDComplex);
        }catch (Exception e)
        {

           if(txtPath.exists()) txtPath.delete();

            throw new Exception(e);
        }

    }

    /**
     * Создаст папку  если ее нет
     * @param dstDir
     */
    public static Path copyDir(File dstDir) throws IOException {
        dstDir=new File(dstDir.toURI());//избавляет от проблемы с пробелами в путях
        if(!dstDir.exists())return Files.createDirectory(dstDir.toPath());
        else return dstDir.toPath();
    }

    /**
     * Просто копирует выбранный файл в указанный. Все названия и пути получаются из вне
     * @param srcFile
     * @param destFile
     * @throws Exception
     */
    public static void copyBSS(File srcFile,File destFile) throws Exception {

        destFile=new File(destFile.toURI());

        try(FileChannel source = new FileInputStream(srcFile).getChannel();FileChannel destination = new FileOutputStream(destFile).getChannel())
        {

            destination.transferFrom(source, 0, source.size());


        } catch (Exception e) {

            if(destFile.exists()) destFile.delete();
            throw new Exception(e);
        }

    }





    public static void recursiveLibsDelete(File diskPath) {

         recursiveDeleteLibsHelper(diskPath);

    }
    private static void recursiveDeleteLibsHelper(File path)
    {

        // до конца рекурсивного цикла
        if (!path.exists())
            return;

        //если это папка, то идем внутрь этой папки и удалим либ файлы и найдем папки, в них мы залезем
        if (path.isDirectory())
        {
            for (File f : path.listFiles((dir, name) -> name.contains(".lib")||name.contains(".LIB"))) f.delete();

            for (File f : path.listFiles((dir) -> dir.isDirectory()))  recursiveDeleteLibsHelper(f);

        }

    }


    public static boolean recursiveDelete(File diskPath) {

       return recursiveDeleteHelper(diskPath);

    }

    private static boolean recursiveDeleteHelper(File path)
    {
        if(!path.canWrite()) return true;//не будем стирать защищенные дирекстории
        // до конца рекурсивного цикла
        if (!path.exists())
            return false;

        //если это папка, то идем внутрь этой папки и вызываем рекурсивное удаление всего, что там есть
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                // рекурсивный вызов
                recursiveDeleteHelper(f);
            }
        }
        // вызываем метод delete() для удаления файлов и пустых(!) папок
        return path.delete();
    }


    private static boolean recursiveDeleteHelper(File path,String excludeNameInRoot,int tier)
    {
        if(!path.canWrite()) return true;//не будем стирать защищенные дирекстории

        // до конца рекурсивного цикла
        if (!path.exists())
            return false;

        //если это папка, то идем внутрь этой папки и вызываем рекурсивное удаление всего, что там есть
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                // рекурсивный вызов
                recursiveDeleteHelper(f,excludeNameInRoot,tier+1);
            }
        }
        // вызываем метод delete() для удаления файлов и пустых(!) папок
        if(!( tier==1 &&  excludeNameInRoot.equalsIgnoreCase(path.getName())))return path.delete();
       else return false;


    }

    public static boolean recursiveDelete(File diskPath,String excludeNameInRoot) {
        return recursiveDeleteHelper(diskPath,excludeNameInRoot,0);

    }
}
