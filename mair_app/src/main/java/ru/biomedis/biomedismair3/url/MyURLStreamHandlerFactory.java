package ru.biomedis.biomedismair3.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import ru.biomedis.biomedismair3.url.protected_links.ProtectedLinkHandler;


public class MyURLStreamHandlerFactory implements URLStreamHandlerFactory
{

    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        if (protocol.equals("myapp"))
        {
            return new MyURLHandler();
        }else  if (protocol.equals("b_protected"))
        {
            return new ProtectedLinkHandler();
        }
        return null;
    }

}
