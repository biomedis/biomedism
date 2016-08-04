package ru.biomedis.biomedismair3.UserUtils.Import;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biomedis.biomedismair3.Log;
import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.TherapyComplex;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Created by Anama on 17.09.2015.
 */
public class ImportTherapyComplex
{


    private Listener listener=null;
    private List<Complex> complexes = new ArrayList();
    private List<Program> listProgram=new ArrayList<>();

    public ImportTherapyComplex()
    {
    }

    /**
     * Парсит файл структуры профиля и импортирует их
     * @param xmlFile xml файл
     * @param mda модель данных
     * @return true если все удачно
     */
    public int parse(File xmlFile, ModelDataApp mda, ru.biomedis.biomedismair3.entity.Profile profile) throws Exception {

        if (listener == null) throw new Exception("Нужно реализовать слушатель событий!");
        boolean res = false;
        if (xmlFile == null) return 0;


        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SAXParser parser = null;


        try {
            parser = factory.newSAXParser();
            parser.parse(xmlFile, new ParserHandler());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            if (listener != null) listener.onError(false);
            listener = null;
            return 0;
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
            return 0;

        } catch (Exception e) {
            logger.error("",e);
            if (listener != null) listener.onError(false);
            clear();
            listener = null;
            return 0;
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
            return 0;
        }


        if (listener != null) listener.onEndAnalize();

        if (listener != null) listener.onStartImport();
        //если все хорошо можно импортировать объекты в базу

        int resSize;
        try {
            Iterator<Complex> it1;
            ImportTherapyComplex.Complex complex;
            for(it1 = this.complexes.iterator(); it1.hasNext(); complex.complex = mda.createTherapyComplex(profile, complex.name, complex.descr, complex.timeForFreq, complex.mullty,0)) {
                complex =it1.next();
            }

            Iterator<Program> it = this.listProgram.iterator();
            ImportTherapyComplex.Program prog;
            while(it.hasNext()) {
                prog = it.next();
                mda.createTherapyProgram(this.complexes.get(prog.complexIndex).complex, prog.name, prog.descr, prog.freqs);
            }

            resSize = this.complexes.size();
        } catch (Exception ee) {
            Log.logger.error("Ошибка создание элементов базы", ee);
            if(this.listener != null) {
                this.listener.onError(true);
            }

            this.listener = null;

            try {
                Iterator<Complex> iterator = this.complexes.iterator();

                while(iterator.hasNext()) {
                    ImportTherapyComplex.Complex var30 = iterator.next();
                    if(var30.complex != null) {
                        mda.removeTherapyComplex(var30.complex);
                    }
                }
            } catch (Exception var22) {
                Log.logger.error("Не удалось откатить изменения", ee);
            }

            resSize = 0;
        }

        if(listener!=null)listener.onEndImport();



        if(this.listener != null) {
            this.listener.onEndImport();
        }

        if(this.listener != null) {
            if(resSize != 0) {
                this.listener.onSuccess();
            } else {
                this.listener.onError(false);
            }
        }

        this.listener = null;
        this.clear();
        return resSize;

    }

    private void clear()
    {

        this.complexes.forEach(i -> i.complex = null);
        this.listProgram.clear();

    }

    class ParserHandler extends DefaultHandler {
        boolean fileTypeOK = false;
        boolean inComplex = false;

        ParserHandler() {
        }

        public void startDocument() throws SAXException {
            super.startDocument();
            if(ImportTherapyComplex.this.listener != null) {
                ImportTherapyComplex.this.listener.onStartParse();
            }

        }

        public void endDocument() throws SAXException {
            super.endDocument();
            if(ImportTherapyComplex.this.listener != null) {
                ImportTherapyComplex.this.listener.onEndParse();
            }

        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("UserComplexes")) {
                this.fileTypeOK = true;
                super.startElement(uri, localName, qName, attributes);
            } else {
                SAXException saxException;
                if(!this.fileTypeOK) {
                    saxException = new SAXException(ImportTherapyComplex.this.new FileTypeMissMatch());
                    throw saxException;
                } else if(qName.equals("Complex")) {
                    this.inComplex = true;
                    if(attributes.getLength() != 0) {
                        ImportTherapyComplex.this.complexes.add(ImportTherapyComplex.this.new Complex(attributes.getValue("name"), attributes.getValue("description"), Boolean.parseBoolean(attributes.getValue("mullty")), Integer.parseInt(attributes.getValue("timeForFreq"))));
                    }

                    super.startElement(uri, localName, qName, attributes);
                } else if(qName.equals("Program")) {
                    if(!ImportTherapyComplex.this.complexes.isEmpty() && this.inComplex) {
                        if(attributes.getLength() != 0) {
                            ImportTherapyComplex.this.listProgram.add(ImportTherapyComplex.this.new Program(attributes.getValue("name"), attributes.getValue("description"), attributes.getValue("frequencies"), ImportTherapyComplex.this.complexes.size() - 1));
                        }

                        super.startElement(uri, localName, qName, attributes);
                    } else {
                        saxException = new SAXException(ImportTherapyComplex.this.new FileTypeMissMatch());
                        throw saxException;
                    }
                } else if(this.fileTypeOK) {
                    saxException = new SAXException(ImportTherapyComplex.this.new FileTypeMissMatch());
                    throw saxException;
                } else {
                    super.startElement(uri, localName, qName, attributes);
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(qName.equals("Complex")) {
                this.inComplex = false;
            }

            super.endElement(uri, localName, qName);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
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





    class Program {
        String name;
        String descr;
        String freqs;
        int complexIndex;
        ru.biomedis.biomedismair3.entity.Program program;

        public Program(String name, String descr, String freqs, int complexIndex) {
            this.name = name;
            this.descr = descr;
            this.freqs = freqs;
            this.complexIndex = complexIndex;
        }
    }

    class Complex {
        String name;
        String descr;
        boolean mullty;
        int timeForFreq;
        TherapyComplex complex;

        public Complex(String name, String descr, boolean mullty, int timeForFreq) {
            this.name = name;
            this.descr = descr;
            this.mullty = mullty;
            this.timeForFreq = timeForFreq;
        }
    }

    /**
     * Тип файла не соответствует типу обработчика. Должен содержать тег UserProfile
     */
    class FileTypeMissMatch extends Exception
    {


    }
}
