package ru.biomedis.starter;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;

/**
 * https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
 */
public class DefaultBrowserCaller {

    public static void openInBrowser(String link,App app){
        HostServicesDelegate hostServices = HostServicesFactory.getInstance(app);
        hostServices.showDocument(link);
    }
}
