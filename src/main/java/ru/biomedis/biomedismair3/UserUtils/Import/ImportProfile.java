package ru.biomedis.biomedismair3.UserUtils.Import;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Created by Anama on 17.09.2015.
 */
public class ImportProfile {

    private Listener listener=null;

    private Profile profile;
    private List<Complex> listComplex=new ArrayList<>();
    private List<Program> listProgram=new ArrayList<>();

    public ImportProfile()
    {
    }

    /**
     * Парсит файл структуры профиля и импортирует их
     * @param xmlFile xml файл
     * @param mda модель данных
     * @return true если все удачно
     */
    public boolean parse(File xmlFile, ModelDataApp mda) throws Exception {

        if (listener == null) throw new Exception("Нужно реализовать слушатель событий!");
        boolean res = false;
        if (xmlFile == null) return false;


        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SAXParser parser = null;


        try {
            parser = factory.newSAXParser();
            parser.parse(xmlFile, new ParserHandler());

        } catch (ParserConfigurationException e) {
            logger.error("",e);
            if (listener != null) listener.onError(false);
            listener = null;
            return false;
        } catch (SAXException e) {


            logger.error("",e);
            if (listener != null) {

                if (e.getCause() instanceof FileTypeMissMatch) {
                    logger.error("SAXException",e);
                    listener.onError(true);
                } else listener.onError(false);
            }
            clear();
            listener = null;
            return false;

        } catch (Exception e) {
            logger.error("",e);
            if (listener != null) listener.onError(false);
            clear();
            listener = null;
            return false;
        }



        //сначала спарсим в коллекцию, потом проверим валидность ,
        // те можно ли из этого построить базу,
        // только после этого строим базу. Это позволит избежать лишних ошибок при некорректных файлах
        if (listener != null) listener.onStartAnalize();
        //проверим нет ли частот которые не пропарсишь.

        try {
            String[] split = null;
            String[] split2 = null;

            for (Program itm : listProgram) {
                if(itm.freqs.isEmpty())continue;
                itm.freqs =  itm.freqs.replace(",",".");
                split = itm.freqs.split(";");
                for (String s : split) {
                    if (s.contains("+")) {
                        split2 = s.split("\\+");
                        for (String s1 : split2) Double.parseDouble(s1);
                    } else Double.parseDouble(s);


                }


            }
        } catch (NumberFormatException e) {

            logger.error("Неверный формат частот",e);
            if (listener != null) listener.onError(true);
            listener = null;
            clear();
            return false;
        }


        if (listener != null) listener.onEndAnalize();

        if (listener != null) listener.onStartImport();
        //если все хорошо можно импортировать объекты в базу
        try {

            //если у нас профиль биофона, то нужно скопировать комплексы в профиль биофона
            if(profile.name.equals(App.BIOFON_PROFILE_NAME)){
                profile.profile=App.getBiofonProfile_();
            }else  profile.profile= mda.createProfile(TextUtil.unEscapeXML(profile.name));



            for (Complex complex : listComplex) {

                complex.complex =  mda.createTherapyComplex(profile.profile,TextUtil.unEscapeXML(complex.name),TextUtil.unEscapeXML(complex.descr),complex.timeForFreq,complex.bundlesLength);
            }

            for (Program program : listProgram) {

                mda.createTherapyProgram(listComplex.get(program.complexIndex).complex,TextUtil.unEscapeXML(program.name),TextUtil.unEscapeXML(program.descr),program.freqs,program.multy);
            }


            res=true;

        } catch (Exception e)
        {

            logger.error("Ошибка создание элементов базы",e);
            if (listener != null) listener.onError(true);
            listener = null;

            //удалим то что созданно.
            try
            {


           if(profile.profile!=null)     mda.removeProfile(profile.profile);



            }catch (Exception ex)
            {

                logger.error("Не удалось откатить изменения",e);
            }



            res=false;
        }


        if(listener!=null)listener.onEndImport();



        if(listener!=null)
        {
            if(res)listener.onSuccess();
            else  listener.onError(false);
        }



        listener=null;
        clear();
        return res;
    }

    private void clear()
    {
        profile.profile=null;
        listComplex.clear();
        listProgram.clear();

    }

    class ParserHandler  extends DefaultHandler
    {
        @Override
        public void startDocument() throws SAXException {
            super.startDocument(); //To change body of generated methods, choose Tools | Templates.
            if(listener!=null)listener.onStartParse();
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument(); //To change body of generated methods, choose Tools | Templates.
            if(listener!=null)listener.onEndParse();
        }


        boolean fileTypeOK=false;
        private  Stack<Complex> complexesStack=new Stack<>();


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if(qName.equals("UserProfile"))
            {
                fileTypeOK=true;
                super.startElement(uri, localName, qName, attributes);
                return;
            }else
            {
                //если у нас уже не стартовый тег а стартовый не найден то выбросим исключение
                if (fileTypeOK!=true)
                {

                    SAXException saxException = new SAXException(new FileTypeMissMatch());
                    throw saxException;

                }
            }

            if(qName.equals("Profile"))
            {
                if(attributes.getLength()!=0)
                {

                    profile = new Profile(attributes.getValue("name"));
                }

                return;
            }else
            if(qName.equals("Complex"))
            {


                if(attributes.getLength()!=0)
                {
                    complexesStack.push(new Complex(attributes.getValue("name"),attributes.getValue("description"),Integer.parseInt(attributes.getValue("timeForFreq")),Integer.parseInt(attributes.getValue("bundlesLength")==null?"1":attributes.getValue("bundlesLength"))));//положим на вершину стека
                    listComplex.add(complexesStack.peek());
                }

                super.startElement(uri, localName, qName, attributes);
                return;

            }else  if(qName.equals("Program"))
            {
                //будет или индекс раздела или комплекса но не вместе. можете быть оба -1 те корень

                int indexCompl=-1;

                if(complexesStack.isEmpty())
                {
                    //программы только в комплексах должны быть
                    SAXException saxException = new SAXException(new FileTypeMissMatch());
                    throw saxException;

                }
                else indexCompl=listComplex.indexOf(complexesStack.peek());

                if(attributes.getLength()!=0)
                {
                    boolean multy;
                    String multy_s = attributes.getValue("multy");
                    if(multy_s==null)multy=true;
                    else multy=Boolean.valueOf(multy_s);
                    listProgram.add(new Program(attributes.getValue("name"),attributes.getValue("description"),attributes.getValue("frequencies"),indexCompl,multy));

                }

                super.startElement(uri, localName, qName, attributes);
                return;

            }else if(fileTypeOK)
            {
                //если не было перехода ранее из метода, том сюда попадем.

                //если нашли левый тег
                SAXException saxException = new SAXException(new FileTypeMissMatch());
                throw saxException;
            }

            super.startElement(uri, localName, qName, attributes); //To change body of generated methods, choose Tools | Templates.
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

           if(qName.equals("Complex"))complexesStack.pop();

            super.endElement(uri, localName, qName); //To change body of generated methods, choose Tools | Templates.
        }


        /**
         *  Всплывает когда обрабатывается строка внутри тега. Учитывает переносы строк. У нас тут строк внутри тегов нет поэтому  метод дефолтный
         * @param ch
         * @param start
         * @param length
         * @throws SAXException
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length); //To change body of generated methods, choose Tools | Templates.
        }

    }


    public void setListener(Listener listener){this.listener=listener;}

    public interface Listener
    {
        public void onStartParse();
        public void onEndParse();
        public void onStartAnalize();
        public void onEndAnalize();
        public void onStartImport();
        public void onEndImport();
        public void onSuccess();

        /**
         *
         * @param fileTypeMissMatch false просто ошибка парсинга, true тип файла неверный
         */
        public void onError(boolean fileTypeMissMatch);

    }



    class Profile
    {
        String name;
        ru.biomedis.biomedismair3.entity.Profile profile;



        public Profile(String name) {
            this.name = name;

        }
    }

    class Complex
    {
        String name;
        String descr;

        int timeForFreq;
        int bundlesLength=1;

TherapyComplex  complex;

        public Complex(String name, String descr,  int timeForFreq,Integer bundlesLength) {
            this.name = name;
            this.descr = descr;

            this.timeForFreq=timeForFreq;
            if(bundlesLength!=null) this.bundlesLength=bundlesLength;
        }
    }

    class Program
    {
        String name;
        String descr;
        String freqs;
        boolean multy;
        int complexIndex=-1;
        ru.biomedis.biomedismair3.entity.TherapyProgram program;


        public Program(String name, String descr, String freqs,  int complexIndex,boolean multy) {
            this.name = name;
            this.descr = descr;
            this.freqs = freqs;
            this.multy = multy;
            this.complexIndex = complexIndex;
        }
    }

    /**
     * Тип файла не соответствует типу обработчика. Должен содержать тег UserProfile
     */
    class FileTypeMissMatch extends Exception
    {


    }
}
