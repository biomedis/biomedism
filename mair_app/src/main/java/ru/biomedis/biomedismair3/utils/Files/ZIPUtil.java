package ru.biomedis.biomedismair3.utils.Files;



import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZIPUtil
{


    /**
     * Запаковать папку в архив
     * @param folder путь к папке
     * @param dstZipFile путь к файлу нового архива
     * @return успех или нет
     */
    public static  boolean  zipFolder(File folder,File dstZipFile){
    boolean res=true;

    try( ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dstZipFile)))
    {
        doZip(folder, out,folder.getPath());

    }catch (Exception e)
    {
        log.error("ошибка создания архива");
        res=false;
    }

return res;
}



    public static  boolean unZip(File zipArch,File dstFolder)
    {
        boolean res=true;

        if (!zipArch.exists() || !zipArch.canRead()) {
            log.error("File cannot be read");
            return false;
        }

        if(!dstFolder.exists()) if(!dstFolder.mkdir()) {  log.error("Не удалось создать папку для распаковки");return false;}


        try {
            ZipFile zip = new ZipFile(zipArch);
            Enumeration entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
               // System.out.println(entry.getName());

                if (entry.isDirectory()) {
                    new File(dstFolder, entry.getName()).mkdirs();
                } else {

                    int i=   entry.getName().lastIndexOf(File.separator);
                    if(i!=-1){
                        new File(dstFolder, entry.getName().substring(0,i)).mkdirs();
                    }
                    writeUnZip(zip.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(
                                    new File(dstFolder, entry.getName()))));
                }
            }

            zip.close();
        } catch (IOException e) {
            log.error("Ошибка распаковки архива");
            res=false;
        }
        return res;
    }

    private static void doZip(File dir, ZipOutputStream out, String folderPathString) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                doZip(f, out,folderPathString);
            else {
                String p=f.getPath().substring(folderPathString.length()+1);
                out.putNextEntry(new ZipEntry(p));
                write(new FileInputStream(f), out);
            }
        }
    }


    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        in.close();
    }

    private static void writeUnZip(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
        in.close();
    }
}
