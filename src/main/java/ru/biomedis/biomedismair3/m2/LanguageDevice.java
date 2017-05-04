package ru.biomedis.biomedismair3.m2;


import lombok.AllArgsConstructor;
import lombok.Data;
import ru.biomedis.biomedismair3.utils.USB.ByteHelper;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
public class LanguageDevice {

    private int deviceLangID;
    private String encodedType;
    private String name;
    private String abbr;


    private static Map<String,LanguageDevice> langs=new HashMap<>();
    static{

        langs.put("en",new LanguageDevice(1,"Cp1250","Английский","en"));
        langs.put("ru",new LanguageDevice(2,"Cp1251","Русский","ru"));
        langs.put("de",new LanguageDevice(3,"Cp1250","Немецкий","de"));
        langs.put("el",new LanguageDevice(4,"Cp1253","Греческий","el"));
        langs.put("fr",new LanguageDevice(5,"Cp1250","Французский","fr"));
        langs.put("pl",new LanguageDevice(6,"Cp1250","Польский","pl"));
        langs.put("it",new LanguageDevice(7,"Cp1250","Итальянский","it"));
        //langs.put("en",new LanguageDevice(8,"Cp1254","Турецкий","en"));
    }

    // //https://unicode-table.com/ru/
    // сделать структуру определяющую диапазоны, которую можно расширять. Метод ее юспользует
    /**
     * Определяет язык прибора по кодовой точке UTF-8.
     * @param text
     * @return
     */
    public LanguageDevice langByCodePoint(String text){

    }

    /**
     * Преобразует строку из UTF8 в кодировку заданную языком.
     * @param src
     * @param abbr
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoLangDeviceSupported
     */
    public static List<Byte> getBytesInDeviceLang(String src, String abbr) throws  NoLangDeviceSupported {
        if(!langs.containsKey(abbr)) throw new NoLangDeviceSupported(abbr);
        LanguageDevice  ld=langs.get(abbr);
        try {
            return ByteHelper.byteArrayToByteList(src.getBytes(ld.getEncodedType()));
        } catch (UnsupportedEncodingException e) {
            throw new NoLangDeviceSupported("Не поддерживается кодировка " +ld.getEncodedType(),e);
        }
    }


    public static LanguageDevice getLanguage(int id){
        Optional<LanguageDevice> first = langs.entrySet()
                                                                 .stream()
                                                                 .map(e->e.getValue())
                                                                 .filter(l -> l.getDeviceLangID() == id)
                                                                 .findFirst();
        return first.orElseGet(null);
    }
    /**
     *
     * @param abbr
     * @return
     */
    public static LanguageDevice getDeviceLang(String abbr){
         return langs.get(abbr);

    }

    /**
     * Не поддерживается язык устройством
     */
    public static class NoLangDeviceSupported extends Exception{
        public NoLangDeviceSupported(String langAbbr) {
            super("Язык "+langAbbr+" не подерживается устройством");
        }

        public NoLangDeviceSupported(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
