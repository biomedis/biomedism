package ru.biomedis.biomedismair3.utils.Files;

import ru.biomedis.biomedismair3.utils.Text.TextFileLineReader;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Списки комплексов и программ, в старом формате.
 * Created by Anama on 13.10.2015.
 */
public class FilesOldProfileHelper
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
        }
        return programms;
    }




    /**
     * Список директорий комплексов
     * @param diskPath путь к диску
     * @return
     */
    public static List<ComplexFileData> getComplexes(File diskPath) throws Exception {

        List<ComplexFileData> res=new ArrayList<>();
        // до конца рекурсивного цикла
        if (!diskPath.exists())  return null;
        File textFile=null;


        File[] listFiles = diskPath.listFiles(dir -> dir.isDirectory() && !dir.getName().equalsIgnoreCase("System Volume Information"));
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
            int ind= file.getName().indexOf("(");
            int ind2= file.getName().indexOf("-");

            if(ind==-1) ind= file.getName().length();
            if(ind2==-1) ind2= 0;

            //пустая папка
            if(textFile==null)
            {
                res.add(new ComplexFileData(-1, file.getName().substring(ind2+1, ind), 0, 1, file));
                continue;
            }
            //прочитаем файл
            List<String> progrParam = new ArrayList<String>();
            TextFileLineReader tr=new TextFileLineReader(textFile, StandardCharsets.UTF_16LE);//файл частот
            tr.setActionPerLine(s -> progrParam.add(TextUtil.removeUTF8BOM(s)));

                tr.readAll();



            if(progrParam.size()==0) throw new Exception("Обнаружен файл неизвестного формата  "+textFile.getName());

            int time=0;
            boolean mullty=false;

            //в файле 1 строка
            String[] split = progrParam.get(0).split(";");
            if(split.length==1)
            {
                mullty=true;
                String[] split2 =  split[0].substring(0, split[0].length() - 1).split("-");
                if(split2.length==2)
                {
                    time=Integer.parseInt(split2[1]);

                }else  throw new Exception("Обнаружен файл неизвестного формата  "+textFile.getName());
            }else
            {
                for (String s : split)
                {
                    if(s.contains("+")) continue;
                    String[] split1 = s.split("-");
                    if(split1.length==2)
                    {
                        time=Integer.parseInt(split1[1]);
                        break;

                    }else  throw new Exception("Обнаружен файл неизвестного формата  "+textFile.getName());


                }
            }

            res.add(new ComplexFileData(-1, file.getName().substring(ind2+1, ind), time,1, file));

        }

        return res;
    }


    /**
     * Программы выбранного комплекса из директории
     * @param complexDir
     * @return
     */
    public static Map<Long,ProgramFileData> getProgramms(File complexDir, int time) throws Exception {
        Map<Long,ProgramFileData> res=new LinkedHashMap<>();

        if (!complexDir.exists())  return null;



long k=0;
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
            int ind= file.getName().length()-4;
            int ind2= file.getName().indexOf("-");
            if(ind==-1) ind= file.getName().length()-1;
            if(ind2==-1) ind2= 0;

            //прочитаем файл
            List<String> progrParam = new ArrayList<String>();
            TextFileLineReader tr=new TextFileLineReader(file, StandardCharsets.UTF_16LE);//файл частот
            tr.setActionPerLine(s -> progrParam.add(TextUtil.removeUTF8BOM(s)));
            tr.readAll();

            if(progrParam.size()==0) throw new Exception("Обнаружена программа неизвестного формата "+complexDir.getName().substring(0,file.getName().length()));

            String r=progrParam.get(0).replace("@", "");
            StringBuilder strB=new StringBuilder();

            int lastInd=0;
            int  index=r.indexOf("-",0);
            while(index!=-1 )
            {
                strB.append(r.substring(lastInd,index));
                lastInd=r.indexOf(";",index);
                if(lastInd==-1) break;
                index=r.indexOf("-",lastInd);

            }

            res.put(k--, new ProgramFileData(-1,-1,time,"", strB.toString().replace(",","."), file.getName().substring(ind2+1, ind), file,  null,false,1,true));
        }

        return res;

    }








}
