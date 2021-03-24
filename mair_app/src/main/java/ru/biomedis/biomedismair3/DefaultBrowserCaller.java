package ru.biomedis.biomedismair3;


import lombok.extern.log4j.Log4j2;
import ru.biomedis.biomedismair3.utils.OS.OSValidator;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 *
 *
 * https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
 */
@Log4j2
public class DefaultBrowserCaller {

    public static void openInBrowser(String link,App app){
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                    .isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(link));
            } else {
                if(OSValidator.isWindows()) openOnWindows(link);
                else if(OSValidator.isMac()) openOnMac(link);
                else openOnLinux(link);
            }
        }catch (Exception e){
            log.error("Не удалось открыть ссылку в браузере", e);
        }
    }

    private static void openOnWindows(String url) throws IOException {
        Runtime rt = Runtime.getRuntime();
        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
    }

    private static void openOnMac(String url) throws IOException {
        Runtime rt = Runtime.getRuntime();

        rt.exec("open " + url);
    }

    private static void openOnLinux(String url) throws IOException {
        Runtime rt = Runtime.getRuntime();

        String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
                "netscape", "opera", "links", "lynx" };

        StringBuffer cmd = new StringBuffer();
        for (int i = 0; i < browsers.length; i++)
            if(i == 0)
                cmd.append(String.format( "%s \"%s\"", browsers[i], url));
            else
                cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
        // If the first didn't work, try the next browser and so on

        rt.exec(new String[] { "sh", "-c", cmd.toString() });
    }
}
