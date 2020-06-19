package ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.entity.LocalizedString;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * Загружает файл частот
 *
 */
@Slf4j
public class LoadLanguageFiles
{


    private List<LoadLanguageFiles.Program> listPrograms=new ArrayList<>();
    private List<LoadLanguageFiles.Complex> listComplex=new ArrayList<>();
    private List<LoadLanguageFiles.Section> listSection=new ArrayList<>();
    private Language lang=null;

    private Map<String,String> transMap;

    public LoadLanguageFiles(Map<String, String> transMap) {
        this.transMap = transMap;
    }

    public LoadLanguageFiles() {
    }

    private ModelDataApp mda;
    /**
     * Парсит файлы языковые и делает обновления в базе
     * @param xmlFiles  список xml файлов для обработки
     * @param mda модель данных
     *
     * @return true если все удачно
     */
    public boolean parse(List<File> xmlFiles, ModelDataApp mda)  {

        this.mda=mda;
        boolean  res= true;
        if (xmlFiles.isEmpty()) return false;

        for (File xmlFile : xmlFiles) {

            listPrograms.clear();
            listComplex.clear();
            listSection.clear();
            lang=null;

            if(!parseFile(xmlFile,mda)) {System.out.println("Ошибка обработки "+xmlFile.getName());res=false;}
            else  System.out.println("Успех обработки "+xmlFile.getName());
        }

        return res;
    }

    private boolean parseFile(File xmlFile, ModelDataApp mda)
    {
        boolean res = false;

        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SAXParser parser = null;


        try {
            parser = factory.newSAXParser();
            parser.parse(xmlFile, new LoadLanguageFiles.ParserHandler());

        } catch (ParserConfigurationException e) {
            log.error("",e);

            return false;
        } catch (SAXException e) {
            log.error("",e);



            return false;

        } catch (Exception e) {
            log.error("",e);


            return false;
        }


        if(lang==null) {  System.out.println("Не обнаружен язык файла "+xmlFile.getName()); return false;}





        //если все хорошо можно импортировать объекты в базу
        try {
            for(LoadLanguageFiles.Program prog: listPrograms)
            {
                ru.biomedis.biomedismair3.entity.Program program = mda.getProgram(transMap==null?prog.uuid:transMap.get(prog.uuid));

                if(program==null) continue;
                LocalizedString localStringName = mda.getLocalString(program.getName(), lang);
                if (localStringName == null) localStringName =mda.addString(program.getName(), "", lang);

                LocalizedString localStringDescr = mda.getLocalString(program.getDescription(), lang);
                if(localStringDescr==null) localStringDescr = mda.addString(program.getDescription(),"",lang);

                mda.initStringsProgram(program,lang);

                if(!prog.name.equals(program.getNameString()) )
                {

                        localStringName.setContent(prog.name);
                        mda.updateLocalString(localStringName);

                    System.out.print("Обновлена программа "+prog.nameRus);
                    System.out.print(" uuid= "+(transMap==null?prog.uuid:transMap.get(prog.uuid)));
                    System.out.print(" Имя было: "+program.getNameString());
                    System.out.println(" Стало:"+prog.name);

                }

                if(!prog.descr.equals(program.getDescriptionString()))
                {
                        localStringDescr.setContent(prog.descr);
                        mda.updateLocalString(localStringDescr);


                    System.out.println("Обновлена программа "+prog.nameRus +" uuid= "+(transMap==null?prog.uuid:transMap.get(prog.uuid)) +" Описание Было: "+program.getDescriptionString()+" Стало:"+prog.descr);
                }





            }

            for(LoadLanguageFiles.Complex complex: listComplex)
            {
                ru.biomedis.biomedismair3.entity.Complex itemComplex = mda.getComplex(transMap==null?complex.uuid:transMap.get(complex.uuid));

                if(itemComplex==null) continue;

                LocalizedString localStringName = mda.getLocalString(itemComplex.getName(), lang);
                if (localStringName == null) localStringName= mda.addString(itemComplex.getName(), "", lang);

                LocalizedString localStringDescr = mda.getLocalString(itemComplex.getDescription(), lang);
                if (localStringDescr == null) localStringDescr = mda.addString(itemComplex.getDescription(), "", lang);
                mda.initStringsComplex(itemComplex,lang);

                if(!complex.name.equals(itemComplex.getNameString()) ) {

                        localStringName.setContent(complex.name);
                        mda.updateLocalString(localStringName);

                    System.out.println("Обновлена секция "+complex.nameRus +" uuid= "+(transMap==null?complex.uuid:transMap.get(complex.uuid)) +" Имя было: "+itemComplex.getNameString()+" Стало:"+complex.name);
                }

                if(!complex.descr.equals(itemComplex.getDescriptionString())) {

                        localStringDescr.setContent(complex.descr);
                        mda.updateLocalString(localStringDescr);

                    System.out.println("Обновлена секция "+complex.nameRus +" uuid= "+(transMap==null?complex.uuid:transMap.get(complex.uuid)) +" Описание Было: "+itemComplex.getDescriptionString()+" Стало:"+complex.descr);
                }





            }


            for(LoadLanguageFiles.Section section: listSection)
            {
                ru.biomedis.biomedismair3.entity.Section itemSection = mda.getSection(transMap==null?section.uuid:transMap.get(section.uuid));

                if(itemSection==null) continue;

                LocalizedString localStringName = mda.getLocalString(itemSection.getName(), lang);
                if (localStringName == null) localStringName = mda.addString(itemSection.getName(), "", lang);

                LocalizedString localStringDescr = mda.getLocalString(itemSection.getDescription(), lang);
                if (localStringDescr == null) localStringDescr = mda.addString(itemSection.getDescription(), "", lang);
                mda.initStringsSection(itemSection,lang);

                if(!section.name.equals(itemSection.getNameString()) ) {

                        localStringName.setContent(section.name);
                        mda.updateLocalString(localStringName);

                    System.out.println("Обновлена секция "+section.nameRus +" uuid= "+(transMap==null?section.uuid:transMap.get(section.uuid)) +" Имя было: "+itemSection.getNameString()+" Стало:"+section.name);
                }

                if(!section.descr.equals(itemSection.getDescriptionString())) {

                        localStringDescr.setContent(section.descr);
                        mda.updateLocalString(localStringDescr);


                    System.out.println("Обновлена секция "+section.nameRus +" uuid= "+(transMap==null?section.uuid:transMap.get(section.uuid)) +" Описание Было: "+itemSection.getDescriptionString()+" Стало:"+section.descr);
                }





            }
            res=true;

        } catch (Exception e)
        {

            log.error("Ошибка обновления элементов базы",e);

            res=false;
        }







        return res;
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



                    try {
                        lang= mda.getLanguage(attributes.getValue("lang"));
                    }catch (Exception e){

                        SAXException saxException = new SAXException(new LoadLanguageFiles.FileTypeMissMatch("В программе нет языка "+attributes.getValue("lang")));
                        throw saxException;
                    }


                }
                super.startElement(uri, localName, qName, attributes);
                return;
            }else
            {
                //если у нас уже не стартовый тег а стартовый не найден то выбросим исключение
                if (fileTypeOK!=true)
                {

                    SAXException saxException = new SAXException(new LoadLanguageFiles.FileTypeMissMatch());
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

                    listPrograms.add(new LoadLanguageFiles.Program(
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
                    listComplex.add(new LoadLanguageFiles.Complex(
                            TextUtil.unEscapeXML(attributes.getValue("name"))
                            ,TextUtil.unEscapeXML(attributes.getValue("description"))
                            ,attributes.getValue("uuid")
                            ,TextUtil.unEscapeXML(attributes.getValue("nameRussian"))));

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
                    if(attributes.getValue("uuid")==null) {
                        System.out.println(attributes.getValue("nameRussian")+" --nameRussian");
                    }


                    listSection.add(new LoadLanguageFiles.Section(
                            TextUtil.unEscapeXML(attributes.getValue("name"))
                            ,TextUtil.unEscapeXML(attributes.getValue("description"))
                            ,attributes.getValue("uuid")
                            ,TextUtil.unEscapeXML(attributes.getValue("nameRussian"))));

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
    class Program
    {
        String name;
        String descr;
        String uuid;
        String nameRus;



    }
    @Data
    @AllArgsConstructor
    class Section
    {
        String name;
        String descr;
        String uuid;
        String nameRus;



    }
    @Data
    @AllArgsConstructor
    class Complex
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
