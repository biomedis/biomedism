package ru.biomedis.starter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Anama on 01.09.2015.
 */
public class TextFileLineReader {
    private File file;
    private Path path;
    private Runnable onClose = () -> actionPerLine=null;
    private Consumer<String> actionPerLine = (String line) -> System.out.print(line);
    private Charset encoding=StandardCharsets.UTF_8;


    public TextFileLineReader(File file) {
        this.file = file;
        path = file.toPath();
    }

    public TextFileLineReader(String filePath) {
        this.file = new File(filePath);
        path = file.toPath();
    }

    /**
     *
     * @param file
     * @param enc использовать StandardCharsets.
     */
    public TextFileLineReader(File file, Charset enc) {
        this.file = file;
        path = file.toPath();
        encoding=enc;
    }

    /**
     *
     * @param filePath
     * @param enc использовать StandardCharsets.
     */
    public TextFileLineReader(String filePath, Charset enc) {
        this.file = new File(filePath);
        path = file.toPath();
        encoding=enc;
    }

    /**
     * нужно установить setOnclose и setActionPerLine
     * И запустить метод
     * Если хочется более интересных операций используйте  getStreamLines() при этом устанавливать  setOnclose и setActionPerLine не нужно
     */
    public boolean readAll() {
        boolean res=true;
        try (Stream<String> lines = Files.lines(path, encoding)) {
            lines.onClose(onClose).forEach(actionPerLine);

        } catch (IOException e) {
            e.printStackTrace();
            res=false;
        }
        return res;
    }

    public String readToString() throws IOException {

        StringBuilder strb=new StringBuilder();
        try (Stream<String> lines = Files.lines(path, encoding)) {
            lines.onClose(onClose).forEach(strb::append);

        }
        return strb.toString();
    }
    public String readToString(int limit) throws IOException {

        StringBuilder strb=new StringBuilder();
        try (Stream<String> lines = Files.lines(path, encoding)) {
            lines.onClose(onClose).limit(limit).forEach(strb::append);

        }
        return strb.toString();
    }
    /**
     * Вернет Stream в котором элементами будут строки
     *
     * @return
     * @throws IOException
     */
    public Stream<String> getStreamLines() throws IOException {
        return Files.lines(path, encoding);
    }


    public void setActionPerLine(Consumer<String> action) {
        actionPerLine = action;
    }


    public void setOnclose(Runnable action) {
        onClose = action;
    }


    public static Stream<String> stream(String filename) throws IOException {
        final Path path = new File( filename ).toPath();
       return    Files.lines( path, StandardCharsets.UTF_8 ) ;

    }
    public static Stream<String> stream(File file) throws IOException {
        final Path path = file.toPath();
        return    Files.lines( path, StandardCharsets.UTF_8 ) ;

    }
    public static Stream<String> stream(String filename,Charset enc) throws IOException {
        final Path path = new File( filename ).toPath();
        return    Files.lines( path, enc ) ;

    }
    public static Stream<String> stream(File file,Charset enc) throws IOException {
        final Path path = file.toPath();
        return    Files.lines( path, enc ) ;

    }

}
