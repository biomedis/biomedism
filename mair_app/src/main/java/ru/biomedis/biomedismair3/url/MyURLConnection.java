package ru.biomedis.biomedismair3.url;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import lombok.extern.slf4j.Slf4j;


/**
 * URL.setURLStreamHandlerFactory(new MyURLStreamHandlerFactory());
 * <img src="myapp:///pics/image.png"/>
 * Register a protocol handler for URLs like this: <code>myapp:///pics/sland.gif</code><br>
 */
@Slf4j
public class MyURLConnection extends URLConnection
{

    private byte[] data;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    protected MyURLConnection(URL url)
    {
        super(url);
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

    public String getContentType()
    {
        String fileName = getURL().getFile();
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        String type="";
        switch (ext)
        {
            case "png":
            case "jpg":
            case "gif":
                type="image/" + ext;
                break;
            case "js":
                type= "application/javascript";
                break;
            case "css":
                type="text/css";
                break;
            default: type="text/plain";
        }
        return type;
    }

    public int getContentLength()
    {
        return data.length;
    }

    public long getContentLengthLong()
    {
        return data.length;
    }

    public boolean getDoInput()
    {
        return true;
    }

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
        try
        {
            int timeout = this.getConnectTimeout();
            long start = System.currentTimeMillis();
            URL url = getURL();

            String path = url.toExternalForm();
            path = path.startsWith("myapp://") ? path.substring("myapp://".length()) : path.substring("myapp:".length()); // attention: triple '/' is reduced to a single '/'

            /*
            // this is my own asynchronous image implementation
            // instead of this part (including the following loop) you could do your own (synchronous) loading logic
            MyImage img = MyApp.getImage(imgPath);
            do
            {
                if (img.isFailed())
                {
                    throw new IOException("Could not load image: " + getURL());
                }
                else if (!img.hasData())
                {
                    long now = System.currentTimeMillis();
                    if (now - start > timeout)
                    {
                        throw new SocketTimeoutException();
                    }
                    Thread.sleep(100);
                }
            } while (!img.hasData());
            data = img.getData();

            */

            //загрузим файл
            //File file = new File(path);
          //  data = Files.readAllBytes(file.toPath());





            try(ByteArrayOutputStream out =new ByteArrayOutputStream();
                InputStream input =new BufferedInputStream(MyURLConnection.class.getResourceAsStream(path)))
            {
                int data = 0;
                while ((data = input.read()) != -1){
                    out.write(data);
                }
                this.data = out.toByteArray();
            }



            //работает только не в jar!!!
            //File file = new File(getClass().getResource(path).toURI());
           // data = Files.readAllBytes(file.toPath());

        }
        catch (Exception e)
        {
            log.error("",e);
        }
    }

    public OutputStream getOutputStream() throws IOException
    {
        // this might be unnecessary - the whole method can probably be omitted for our purposes
        return new ByteArrayOutputStream();
    }

    public java.security.Permission getPermission() throws IOException
    {
        return null; // we need no permissions to access this URL
    }

}
