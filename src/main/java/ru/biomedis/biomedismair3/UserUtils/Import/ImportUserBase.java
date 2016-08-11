package ru.biomedis.biomedismair3.UserUtils.Import;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biomedis.biomedismair3.ModelDataApp;
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
 * XML файл. теги не содержат текста, все данные внутри атрибутов. Все ковычки должны быть экранированны или их недолжно быть в текста атрибутов!!!!!!. Здесь они восстанавливаются
 * Created by Anama on 17.09.2015.
 */
public class ImportUserBase
{
private Listener listener=null;


    private List<Section> listSections=new ArrayList<>();
    private List<Program> listPrograms=new ArrayList<>();
    private List<Complex> listComplexes=new ArrayList<>();

    public ImportUserBase()
    {
    }

    /**
     * Парсит файл структуры пользовательской бюазы и импортирует ее в указанный раздел
     * @param xmlFile xml файл
     * @param mda модель данных
     * @param container контейнерный раздел в базе который примет распарсеную структуру
     * @return true если все удачно
     */
    public boolean parse(File xmlFile, ModelDataApp mda,ru.biomedis.biomedismair3.entity.Section container) throws Exception {

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

                    logger.error("SAXException");
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

            for (Program itm : listPrograms) {
                if(itm.freqs.isEmpty())continue;
                itm.freqs=itm.freqs.replace(",",".");
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


            for (Section section : listSections)  {
                if(section.sectionIndex < 0) section.section = mda.createSection(container, section.name, section.descr, false, mda.getUserLanguage());
                else section.section = mda.createSection(listSections.get(section.sectionIndex).section, section.name, section.descr, false, mda.getUserLanguage());

            }
            for (Complex complex : listComplexes)
            {
               if(complex.sectionIndex>=0) complex.complex=mda.createComplex(complex.name,complex.descr,listSections.get(complex.sectionIndex).section,false,mda.getUserLanguage());
                else complex.complex=mda.createComplex(complex.name,complex.descr,container,false,mda.getUserLanguage());
            }

            for(Program prog: listPrograms)
            {
                if(prog.sectionIndex>=0) prog.program = mda.createProgram(prog.name,prog.descr,prog.freqs,listSections.get(prog.sectionIndex).section,false,mda.getUserLanguage());
                else if(prog.complexIndex>=0) prog.program = mda.createProgram(prog.name,prog.descr,prog.freqs,listComplexes.get(prog.complexIndex).complex,false,mda.getUserLanguage());
                else prog.program = mda.createProgram(prog.name,prog.descr,prog.freqs,container,false,mda.getUserLanguage());

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
                for(Program prog: listPrograms) {mda.removeProgram(prog.program);prog.program=null;}
                for (Complex complex : listComplexes){mda.removeComplex(complex.complex);complex.complex=null;}
                for (Section section : listSections){mda.removeSection(section.section);section.section=null;}

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
        for(Program prog: listPrograms) prog.program=null;
        for (Complex complex : listComplexes)complex.complex=null;
        for (Section section : listSections)section.section=null;


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


        private Stack<Section> sectionsStack=new Stack<>();
        private  Stack<Complex> complexesStack=new Stack<>();


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if(qName.equals("UserBase"))
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


            if(qName.equals("Section"))
            {
                int index=-1;
                if(sectionsStack.isEmpty())index=-1;//если у нас пустой стек, значит эта секция в корне
                else index=listSections.indexOf(sectionsStack.peek());//если стек не пустой значит секция в секции и мы ищем индекс родителя в основном массиве по значению вершины стека

                if(attributes.getLength()!=0)
                {
                    sectionsStack.push(new Section(TextUtil.unEscapeXML(attributes.getValue("name")),TextUtil.unEscapeXML(attributes.getValue("description")),index));//положим на вершину стека
                    listSections.add(sectionsStack.peek());
                }
            }else  if(qName.equals("Complex"))
            {
                int index=-1;
                if(sectionsStack.isEmpty())index=-1;
                else index=listSections.indexOf(sectionsStack.peek());

                if(attributes.getLength()!=0)
                {
                    complexesStack.push(new Complex(TextUtil.unEscapeXML(attributes.getValue("name")),TextUtil.unEscapeXML(attributes.getValue("description")),index));//положим на вершину стека
                    listComplexes.add(complexesStack.peek());
                }

                super.startElement(uri, localName, qName, attributes);
                return;
            }else  if(qName.equals("Program"))
            {
                //будет или индекс раздела или комплекса но не вместе. можете быть оба -1 те корень
                int indexSect=-1;
                int indexCompl=-1;

                if(complexesStack.isEmpty())
                {
                    if(!sectionsStack.isEmpty()) indexSect=listSections.indexOf(sectionsStack.peek());

                }
                else indexCompl=listComplexes.indexOf(complexesStack.peek());

                if(attributes.getLength()!=0)
                {
                   listPrograms.add(new Program(TextUtil.unEscapeXML(attributes.getValue("name")),TextUtil.unEscapeXML(attributes.getValue("description")),attributes.getValue("frequencies"),indexSect,indexCompl));

                }

                super.startElement(uri, localName, qName, attributes);
                return;

            }else if(fileTypeOK)
            {

                //если попался левый тег, мы тут возмутимся
                SAXException saxException = new SAXException(new FileTypeMissMatch());
                throw saxException;
            }

            super.startElement(uri, localName, qName, attributes); //To change body of generated methods, choose Tools | Templates.
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if(qName.equals("Section"))sectionsStack.pop();
            else if(qName.equals("Complex"))complexesStack.pop();

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



    class Section
    {
        String name;
        String descr;
        int sectionIndex=-1;
        ru.biomedis.biomedismair3.entity.Section section;

        public Section(String name, String descr, int sectionIndex) {
            this.name = name;
            this.descr = descr;
            this.sectionIndex = sectionIndex;
        }
    }

    class Complex
    {
        String name;
        String descr;
        int sectionIndex=-1;//если -1 то значит вне секции. индекс в массивах соответствующих
        ru.biomedis.biomedismair3.entity.Complex complex;

        public Complex(String name, String descr, int sectionIndex) {
            this.name = name;
            this.descr = descr;
            this.sectionIndex = sectionIndex;
        }
    }

    class Program
    {
        String name;
        String descr;
        String freqs;
        int sectionIndex=-1;
        int complexIndex=-1;
        ru.biomedis.biomedismair3.entity.Program program;

        public Program(String name, String descr, String freqs, int sectionIndex, int complexIndex) {
            this.name = name;
            this.descr = descr;
            this.freqs = freqs;
            this.sectionIndex = sectionIndex;
            this.complexIndex = complexIndex;
        }
    }

    /**
     * Тип файла не соответствует типу обработчика. Должен содержать тег UserBase
     */
    class FileTypeMissMatch extends Exception
    {


    }
}
