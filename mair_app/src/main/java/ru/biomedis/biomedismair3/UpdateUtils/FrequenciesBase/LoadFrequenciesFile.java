package ru.biomedis.biomedismair3.UpdateUtils.FrequenciesBase;

import lombok.extern.slf4j.Slf4j;
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



/**
 * Загружает файлы переводов. Просто перезаписывает все частоты по найденным UUID для программ, корректируя символы , и проверяя корректность ввода
 *
 */
@Slf4j
public class LoadFrequenciesFile
{


    private List<LoadFrequenciesFile.Program> listPrograms=new ArrayList<>();

    /**
     * Парсит файл структуры пользовательской бюазы и импортирует ее в указанный раздел
     * @param xmlFile xml файл
     * @param mda модель данных
     *
     * @return true если все удачно
     */
    public boolean parse(File xmlFile, ModelDataApp mda) throws Exception {


        boolean res = false;
        if (xmlFile == null) return false;



        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SAXParser parser = null;


        try {
            parser = factory.newSAXParser();
            parser.parse(xmlFile, new LoadFrequenciesFile.ParserHandler());

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




        //проверим нет ли частот которые не пропарсишь.

        try {
            String[] split = null;
            String[] split2 = null;

            for (LoadFrequenciesFile.Program itm : listPrograms) {
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

            log.error("Неверный формат частот",e);

            return false;
        }



        //если все хорошо можно импортировать объекты в базу
        try {
            for(LoadFrequenciesFile.Program prog: listPrograms)
            {
                ru.biomedis.biomedismair3.entity.Program program = mda.getProgram(prog.uuid);
                if(program==null) continue;
                if(prog.freqs.equals(program.getFrequencies())) continue;
                System.out.println("Обновлена программа "+prog.name +" uuid= "+prog.uuid);
                program.setFrequencies(prog.freqs);
                mda.updateProgram(program);
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

            if(qName.equals("FrequenciesBaseFile"))
            {

                fileTypeOK=true;
                super.startElement(uri, localName, qName, attributes);
                return;
            }else
            {
                //если у нас уже не стартовый тег а стартовый не найден то выбросим исключение
                if (fileTypeOK!=true)
                {

                    SAXException saxException = new SAXException(new LoadFrequenciesFile.FileTypeMissMatch());
                    throw saxException;

                }
            }


            if(qName.equals("Program"))
            {
                if(attributes.getLength()!=0)
                {
                    listPrograms.add(new LoadFrequenciesFile.Program(TextUtil.unEscapeXML(attributes.getValue("name")),"",attributes.getValue("frequencies").replace(" ",""),attributes.getValue("uuid")));

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








    class Program
    {
        String name;
        String descr;
        String freqs;
        String uuid;

        public Program(String name, String descr, String freqs,String uuid) {
            this.name = name;
            this.descr = descr;
            this.freqs = freqs;
            this.uuid=uuid;
        }
    }

    /**
     * Тип файла не соответствует типу обработчика. Должен содержать тег FrequenciesBaseFile
     */
    class FileTypeMissMatch extends Exception
    {


    }
}
