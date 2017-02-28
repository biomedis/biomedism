package ru.biomedis.biomedismair3.utils.Text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;

/**
 * Created by Anama on 11.11.2015.
 */
public class TextUtil
{

    public static String escapeXML(String s) {
        StringBuilder result = new StringBuilder();
        StringCharacterIterator i = new StringCharacterIterator(s);
        char c =  i.current();
        while (c != CharacterIterator.DONE ){
            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;

                case '>':
                    result.append("&gt;");
                    break;

                case '"':
                    result.append("&quot;");
                    break;

                case '\'':
                    result.append("&apos;");
                    break;

                case '&':
                    result.append("&amp;");
                    break;

                default:
                    result.append(c);
            }
            c = i.next();
        }
        return result.toString();
    }




    public static String unEscapeXML(String text)
    {
        StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char charAt = text.charAt(i);
            if (charAt != '&') {
                result.append(charAt);
                i++;
            } else {
                if (text.startsWith("&amp;", i)) {
                    result.append('&');
                    i += 5;
                } else if (text.startsWith("&apos;", i)) {
                    result.append('\'');
                    i += 6;
                } else if (text.startsWith("&quot;", i)) {
                    result.append('"');
                    i += 6;
                } else if (text.startsWith("&lt;", i)) {
                    result.append('<');
                    i += 4;
                } else if (text.startsWith("&gt;", i)) {
                    result.append('>');
                    i += 4;
                } else if (text.startsWith("&prime;", i)) {
                    result.append('>');
                    i += 4;
                } else i++;
            }
        }
        return result.toString();
    }


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
