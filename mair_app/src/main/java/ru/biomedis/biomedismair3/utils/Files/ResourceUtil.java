package ru.biomedis.biomedismair3.utils.Files;

import java.io.*;

/**
 * Created by anama on 27.12.16.
 */
public class ResourceUtil {
    public File saveResource(String name ,String dstName) throws IOException {
        return saveResource(name, dstName,true);
    }

    public File saveResource(String name, String dstName,boolean replace) throws IOException {
        return saveResource(new File("."),dstName, name, replace);
    }

    public File saveResource(File outputDirectory, String dstName,String name) throws IOException {
        return saveResource(outputDirectory, dstName, name, true);
    }

    public File saveResource(File outputDirectory, String dstName, String nameRes, boolean replace)
            throws IOException {
        File out = new File(outputDirectory, dstName);
        if (!replace && out.exists())
            return out;
        // Step 1:
        InputStream resource = this.getClass().getResourceAsStream(nameRes);
        if (resource == null)
            throw new FileNotFoundException(nameRes + " (resource not found)");
        // Step 2 and automatic step 4
        try(InputStream in = resource;
            OutputStream writer = new BufferedOutputStream(
                    new FileOutputStream(out))) {
            // Step 3
            byte[] buffer = new byte[1024 * 4];
            int length;
            while((length = in.read(buffer)) >= 0) {
                writer.write(buffer, 0, length);
            }
        }
        return out;
    }
}
