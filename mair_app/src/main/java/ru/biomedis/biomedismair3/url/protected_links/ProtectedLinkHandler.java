package ru.biomedis.biomedismair3.url.protected_links;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import ru.biomedis.biomedismair3.social.remote_client.SocialClient;

/**
 * Хэндлит загрузку файлов с схемой в пути b_protected.   b_protected://
 * Путь должен иметь /file_protected/{id}
 */
public class ProtectedLinkHandler extends URLStreamHandler
{

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        return new ProtectedLinkConnection(url, SocialClient.INSTANCE.getFilesClient());
    }

}
