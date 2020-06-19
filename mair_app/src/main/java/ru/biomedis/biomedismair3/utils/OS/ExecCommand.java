package ru.biomedis.biomedismair3.utils.OS;

import java.io.IOException;

/**
 * Created by anama on 18.10.16.
 */
public class ExecCommand {



    public static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        if(result.charAt(result.length()-1)=='\n') return result.substring(0,result.length()-1).intern();
        else return result;

    }
}
