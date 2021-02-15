package ru.biomedis.biomedismair3.url.protected_links;

import feign.Response;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import ru.biomedis.biomedismair3.social.remote_client.FilesClient;


/**
 *
 * <img src="b_protected:///pics/image.png"/>
 * Register a protocol handler for URLs like this: <code>b_protected:///pics/sland.gif</code><br>
 */
@Slf4j
public class ProtectedLinkConnection extends URLConnection
{

    private final FilesClient filesClient;
    private byte[] data;
    private static final String PATH_MARKER="file_protected/";
    private static final Tika tika = new Tika();

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected ProtectedLinkConnection(URL url, FilesClient filesClient)
    {
        super(url);
        this.filesClient = filesClient;
    }

    @Override
    public void connect() throws IOException
    {
        if (connected)
        {
            return;
        }
        loadImage();
        connected = true;
    }

    @Override
    public String getHeaderField(String name)
    {
        if ("Content-Type".equalsIgnoreCase(name))
        {
            return getContentType();
        }
        else if ("Content-Length".equalsIgnoreCase(name))
        {
            return "" + getContentLength();
        }
        return null;
    }

    @Override
    public String getContentType()
    {
        return tika.detect(getURL().getFile());
    }

    @Override
    public int getContentLength()
    {
        return data.length;
    }

    @Override
    public long getContentLengthLong()
    {
        return data.length;
    }

    @Override
    public boolean getDoInput()
    {
        return true;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        connect();
        return new ByteArrayInputStream(data);
    }

    private void loadImage() throws IOException
    {
        if (data != null)
        {
            return;
        }
        String path="";
        try
        {
            URL url = getURL();

            path = url.toExternalForm();
            int index = path.indexOf(PATH_MARKER);
            if(index<=0) throw new Exception("Не верная ссылка на защищенный файл");
            long id = Long.parseLong(path.substring(index+PATH_MARKER.length()));
            Response response = filesClient.downloadProtectedFile(id);

            this.data = IOUtils.toByteArray(response.body().asInputStream());

        }
        catch (Exception e)
        {
            log.error(path,e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        // this might be unnecessary - the whole method can probably be omitted for our purposes
        return new ByteArrayOutputStream();
    }

    @Override
    public java.security.Permission getPermission() throws IOException
    {
        return null; // we need no permissions to access this URL
    }

}
