package ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Загружает файл с переводами, можно получить сырую информацию о программах и комплексах
 *
 */
public class LoadUUIDs
{


    private List<LoadUUIDs.Program> listPrograms=new ArrayList<>();
    private List<LoadUUIDs.Complex> listComplex=new ArrayList<>();
    private List<LoadUUIDs.Section> listSection=new ArrayList<>();
    private Language lang=null;


    public LoadUUIDs() {
    }


    /**
     * Парсит файлы языковые
     * @param xmlFiles  список xml файлов для обработки
     *
     *
     * @return true если все удачно
     */
    public boolean parse(List<File> xmlFiles)  {


        boolean  res= true;
        if (xmlFiles.isEmpty()) return false;

        for (File xmlFile : xmlFiles) {

            listPrograms.clear();
            listComplex.clear();
            listSection.clear();
            lang=null;

            if(!parseFile(xmlFile)) {System.out.println("Ошибка обработки "+xmlFile.getName());res=false;}
            else  System.out.println("Успех обработки "+xmlFile.getName());
        }

        return res;
    }

    private  boolean parseFile(File xmlFile)
    {
        boolean res = false;

        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SAXParser parser = null;

        try {
            parser = factory.newSAXParser();
            parser.parse(xmlFile, new LoadUUIDs.ParserHandler());

            res=true;

        } catch (ParserConfigurationException e) {
            logger.error("",e);

            return false;
        } catch (SAXException e) {
            logger.error("",e);
            return false;
        } catch (Exception e) {
            logger.error("",e);


            return false;
        }


        return res;
    }


    public List<Program> getListPrograms() {
        return listPrograms;
    }

    public List<Complex> getListComplex() {
        return listComplex;
    }

    public List<Section> getListSection() {
        return listSection;
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


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if(qName.equals("LanguageBaseFile"))
            {

                fileTypeOK=true;
                if(attributes.getLength()!=0){


                }
                super.startElement(uri, localName, qName, attributes);
                return;
            }else
            {
                //если у нас уже не стартовый тег а стартовый не найден то выбросим исключение
                if (fileTypeOK!=true)
                {

                    SAXException saxException = new SAXException(new LoadUUIDs.FileTypeMissMatch());
                    throw saxException;

                }
            }


            if(qName.equals("Program"))
            {
                if(attributes.getLength()!=0)
                {
                    String name = attributes.getValue("name");
                    String description = attributes.getValue("description");
                    String uuid = attributes.getValue("uuid");
                    String nameRussian = attributes.getValue("nameRussian");

                    if(name==null) System.out.println(uuid);
                    if(description==null) System.out.println(uuid);

                    listPrograms.add(new LoadUUIDs.Program(
                            TextUtil.unEscapeXML(name),
                            TextUtil.unEscapeXML(description),
                            uuid,
                            TextUtil.unEscapeXML(nameRussian)));

                }

                super.startElement(uri, localName, qName, attributes);
                return;

            }else
            if(qName.equals("Complex"))
            {
                if(attributes.getLength()!=0)
                {
                    listComplex.add(new LoadUUIDs.Complex(TextUtil.unEscapeXML(attributes.getValue("name")),TextUtil.unEscapeXML(attributes.getValue("description")),attributes.getValue("uuid"),TextUtil.unEscapeXML(attributes.getValue("nameRussian"))));

                }

                super.startElement(uri, localName, qName, attributes);
                return;

            }
            else
            if(qName.equals("Section"))
            {
                if(attributes.getLength()!=0)
                {
                    if(attributes.getValue("name")==null) {
                        System.out.println(attributes.getValue("uuid")+" --name");
                    }
                    if(attributes.getValue("description")==null) {
                        System.out.println(attributes.getValue("uuid")+" --description");
                    }
                    if(attributes.getValue("nameRussian")==null) {
                        System.out.println(attributes.getValue("uuid")+" --nameRussian");
                    }

                    listSection.add(new LoadUUIDs.Section(TextUtil.unEscapeXML(attributes.getValue("name")),TextUtil.unEscapeXML(attributes.getValue("description")),attributes.getValue("uuid"),TextUtil.unEscapeXML(attributes.getValue("nameRussian"))));

                }

                super.startElement(uri, localName, qName, attributes);
                return;

            }

            super.startElement(uri, localName, qName, attributes); //To change body of generated methods, choose Tools | Templates.
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

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







@Data
@AllArgsConstructor
  public static  class Program
    {
        String name;
        String descr;
        String uuid;
        String nameRus;

    }


    @Data
    @AllArgsConstructor
    public static class Section
    {
        String name;
        String descr;
        String uuid;
        String nameRus;



    }


    @Data
    @AllArgsConstructor
    public static class Complex
    {
        String name;
        String descr;
        String uuid;
        String nameRus;
    }



    /**
     * Тип файла не соответствует типу обработчика. Должен содержать тег LanguageBaseFile
     */
    class FileTypeMissMatch extends Exception
    {
        public FileTypeMissMatch() {
        }

        public FileTypeMissMatch(String message) {
            super(message);
        }
    }
}
