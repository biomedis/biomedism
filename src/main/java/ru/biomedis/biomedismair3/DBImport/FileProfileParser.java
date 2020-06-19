package ru.biomedis.biomedismair3.DBImport;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Section;
import ru.biomedis.biomedismair3.entity.TherapyComplex;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@Slf4j
public class FileProfileParser
{

    public List<Complex> listComplex=new ArrayList<>();
    public List<Program> listProgram=new ArrayList<>();


    public FileProfileParser()
    {
    }

    /**
     * Парсит файл структуры профиля и импортирует их
     * @param xmlFile xml файл
     * @param mda модель данных
     * @return true если все удачно
     */
    public boolean parse(File xmlFile, ModelDataApp mda) throws Exception {


        boolean res = true;
        if (xmlFile == null) return false;


        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SAXParser parser = null;


        try {
            parser = factory.newSAXParser();
            parser.parse(xmlFile, new ParserHandler());

        } catch (ParserConfigurationException e) {
            log.error("",e);

            return false;
        } catch (SAXException e) {


            log.error("",e);

            clear();

            return false;

        } catch (Exception e) {
            log.error("",e);
            clear();

            return false;
        }



        //сначала спарсим в коллекцию, потом проверим валидность ,
        // те можно ли из этого построить базу,
        // только после этого строим базу. Это позволит избежать лишних ошибок при некорректных файлах

        //проверим нет ли частот которые не пропарсишь.

        try {
            String[] split = null;
            String[] split2 = null;

            for (Program itm : listProgram) {
                if(itm.freqs.isEmpty())continue;
                split = itm.freqs.split(";");
                for (String s : split) {
                    if (s.contains("+")) {
                        split2 = s.split("\\+");
                        for (String s1 : split2) Double.parseDouble(s1);
                    } else Double.parseDouble(s);


                }


            }
        } catch (NumberFormatException e) {

            log.error("Неверный формат частот",e);

            clear();
            return false;
        }



        //если все хорошо можно импортировать объекты в базу



        return res;
    }

    private void clear()
    {

        listComplex.clear();
        listProgram.clear();

    }

    class ParserHandler  extends DefaultHandler
    {
        @Override
        public void startDocument() throws SAXException {
            super.startDocument(); //To change body of generated methods, choose Tools | Templates.

        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument(); //To change body of generated methods, choose Tools | Templates.

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


                return;
            }else
            if(qName.equals("Complex"))
            {


                if(attributes.getLength()!=0)
                {
                    complexesStack.push(new Complex(attributes.getValue("name"),attributes.getValue("description"),Boolean.parseBoolean(attributes.getValue("mullty")),Integer.parseInt(attributes.getValue("timeForFreq"))));//положим на вершину стека
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
                    listProgram.add(new Program(attributes.getValue("name"),attributes.getValue("description"),attributes.getValue("frequencies").replaceAll(",","\\."),indexCompl));

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






    public  class Profile
    {
        String name;
        ru.biomedis.biomedismair3.entity.Profile profile;



        public Profile(String name) {
            this.name = name;

        }
    }

   public  class Complex
    {
        String name;
        String descr;
        boolean mullty;
        int timeForFreq;

        ru.biomedis.biomedismair3.entity.Complex  complex;

        public Complex(String name, String descr, boolean mullty,  int timeForFreq) {
            this.name = name;
            this.descr = descr;
            this.mullty=mullty;
            this.timeForFreq=timeForFreq;

        }
    }

    public   class Program
    {
        String name;
        String descr;
        String freqs;
        int complexIndex=-1;
        ru.biomedis.biomedismair3.entity.Program program;


        public Program(String name, String descr, String freqs,  int complexIndex) {
            this.name = name;
            this.descr = descr;
            this.freqs = freqs;

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
