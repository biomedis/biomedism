package ru.biomedis.biomedismair3.utils.Text;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by Anama on 01.09.2015.
 */
public class Base64Codec {

    public static String encode(String src)
    {
        return  Base64.getEncoder().encodeToString(src.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String base64)
    {
        return new String(Base64.getDecoder().decode( base64 ),StandardCharsets.UTF_8 );
    }

}
