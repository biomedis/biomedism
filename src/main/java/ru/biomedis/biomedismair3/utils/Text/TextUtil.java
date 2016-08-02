package ru.biomedis.biomedismair3.utils.Text;

import java.util.regex.Pattern;

/**
 * Created by Anama on 11.11.2015.
 */
public class TextUtil
{
    /**
     * Проверит строку частот типа 5.4;6+5+6.7 на возможнность парсинга
     * @param freqs
     * @return
     * @throws Exception
     */
    public static String checkFreqs(String freqs) throws Exception
    {

        String[] split = freqs.split(";");
        for (String s : split)
        {
            String[] split1 = s.split("\\+");
            if(split1.length==1) Double.parseDouble(s);
            else for (String s1 : split1) Double.parseDouble(s1);


        }


        return freqs;
    }

    /**
     * Удаляем BOM из UNICODE Строки
     * @param s
     * @return
     */
    public static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        return s;
    }
    public static String digitText(String src)
    {

        return replaceAll(src,"[^\\w\\d\\- ]", "");
    }

    public static String replaceWinPathBadSymbols(String src)
    {

        return src.replaceAll("[\\\\/\\*\\|:\\?<>\"\\.]", "");
    }


    /**
     * Версия реплейса с поддержкой UNICODE !!!!!
     * @param src
     * @param regex
     * @param replacement
     * @return
     */
    public static String replaceAll(String src,String regex, String replacement) {
        return Pattern.compile(regex,Pattern.UNICODE_CHARACTER_CLASS).matcher(src).replaceAll(replacement);
    }

    /**
     * Проверяет выражение. Поддерживает UNICODE строки
     * @param src
     * @param regex
     * @return
     */
    public static boolean match(String src,String regex) {
        return Pattern.compile(regex,Pattern.UNICODE_CHARACTER_CLASS).matcher(src).matches();
    }
}
