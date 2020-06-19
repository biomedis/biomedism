package ru.biomedis.biomedismair3.DBImport;

import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Section;
import ru.biomedis.biomedismair3.utils.Text.TextFileLineReader;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Anama on 09.11.2015.
 */
public class NewDBImport
{
    private ModelDataApp model;
    private File dbPath=new File("./newdb");
    private String nameDep;

    private List<Dep> deps_ru=new ArrayList<>();
    private List<Dep> deps_en=new ArrayList<>();
    private List<Dep> deps_el=new ArrayList<>();

    private List<Complex> compl_ru=new ArrayList<>();
    private List<Complex> compl_en=new ArrayList<>();
    private List<Complex> compl_el=new ArrayList<>();

    private List<Progr> prog_ru=new ArrayList<>();
    private List<Progr> prog_en=new ArrayList<>();
    private List<Progr> prog_el=new ArrayList<>();
    private String basePath;//текущая выбранная база

    private  final List<Dep> getDeps()
    {
       switch (basePath)
       {
           case "rubase":
               return deps_ru;

           case "enbase":
               return deps_en;

           case "grbase":
               return deps_el;

           default: return null;
       }

    }
    private final List<Progr> getProgr()
    {
        switch (basePath)
        {
            case "rubase":
                return prog_ru;

            case "enbase":
                return prog_en;

            case "grbase":
                return prog_el;

            default: return null;
        }

    }
    private final List<Complex> getCompl()
    {
        switch (basePath)
        {
            case "rubase":
                return compl_ru;

            case "enbase":
                return compl_en;

            case "grbase":
                return compl_el;

            default: return null;
        }

    }
    public NewDBImport(ModelDataApp model) {
        this.model = model;
    }

    class Dep
    {
        public Dep(String fileName, String name, int parent) {
            this.fileName = fileName;
            this.name = name;
            this.parent = parent;
        }

        public int parent;
        public String name;
        public String fileName;


    }
    class Progr
    {
        public Progr(int complex, int dep, String freqs, String name) {
            this.complex = complex;
            this.dep = dep;

            this.freqs = freqs;
            this.name = name;
        }


        String freqs;
        String name;
        int complex;
        int dep;

    }
    class Complex
    {
        public Complex(int dep, String name) {
            this.dep = dep;
            this.name = name;

        }

        public int dep;
        public String name;



    }



    class Counter
    {
        private int count=0;
        private int lastIndex;//последний элемент списка
        private int initIndex;//предыдущий элемент в списке, до начала нового списка

        public Counter(int initIndex)
        {
            this.initIndex=initIndex;
            lastIndex=initIndex;
        }

        public int getCount(){return count;}
        public int inc()
        {
            lastIndex++;
            return ++count;
        }
        public int dec(){lastIndex--; return --count;}
        public void reset(){count=0;lastIndex=initIndex;}

        public int getLastIndex() {
            return lastIndex;
        }

        public int getInitIndex() {
            return initIndex;
        }
    }

    /**
     * Производит построение структуры для каждого языка
     * @param path путь к папке языка
     */
    private void buildStructure(String path) throws Exception {
        File baseDir=new File(dbPath,path);
        basePath=path;
        if(!baseDir.exists())throw new Exception();



        Counter rootDepsCount=new Counter(-1);
        //в файлах данные по строкам размещены согласованно по отношению к их родительским  структурам, номер строки это ID
        TextFileLineReader tr=new TextFileLineReader(new File(baseDir,"root.txt"));
        tr.setActionPerLine(s ->
        {
            String[] split = s.split(";");
            if(split.length!=0)
            {
                rootDepsCount.inc();
                getDeps().add(new Dep(split[0], split[1], -1));

            }

        });
        tr.readAll();






/** инфекционные ***/



        Counter ISubDepsCount=new Counter(rootDepsCount.getLastIndex());
        //список разделов инфекционных программ
          tr=new TextFileLineReader(new File(baseDir,getDeps().get(0).fileName+".txt"), StandardCharsets.UTF_16LE);
        tr.setActionPerLine(s ->
        {
            getDeps().add(new Dep(getDeps().get(0).fileName+ISubDepsCount.inc(), TextUtil.removeUTF8BOM(s),0));//подразделы, имена файлов это базовая часть укажет на файлы программ.F+имя это файлы частот
        });
        tr.readAll();


        //в подразделах у нас програмы
        int i=ISubDepsCount.getInitIndex()+1;
        while(i<=ISubDepsCount.getLastIndex())
        {

            List<String> names=new ArrayList<>();
            tr=new TextFileLineReader(new File(baseDir,getDeps().get(i).fileName+".txt"), StandardCharsets.UTF_16LE);//файл программ
            tr.setActionPerLine(s -> names.add(TextUtil.removeUTF8BOM(s)));
            tr.readAll();

            List<String> freqs=new ArrayList<>();
            TextFileLineReader  tr1=new TextFileLineReader(new File(baseDir,"f"+getDeps().get(i).fileName+".txt"), StandardCharsets.UTF_16LE);//файл частот
            tr1.setActionPerLine(s -> freqs.add(TextUtil.removeUTF8BOM(s)));
            tr1.readAll();

            System.out.println("Файл частот " + "F" + getDeps().get(i).fileName + ".txt");
            // запишим программы
            for(int k=0;k<names.size();k++)  getProgr().add(new Progr(-1,i,checkFreqs(freqs.get(k).replace(",", ".").replace(" ", "")),names.get(k)));


            i++;
        }

/**********/






        /** Неинфекционные ***/
        Counter NISubDepsCount=new Counter(ISubDepsCount.getLastIndex());
        //список разделов неинфекционных программ
        tr=new TextFileLineReader(new File(baseDir,getDeps().get(1).fileName+".txt"), StandardCharsets.UTF_16LE);
        tr.setActionPerLine(s ->
        {
            getDeps().add(new Dep(getDeps().get(1).fileName+NISubDepsCount.inc(),TextUtil.removeUTF8BOM(s),0));//подразделы, имена файлов это базовая часть укажет на файлы программ.F+имя это файлы частот
        });
        tr.readAll();

        //в подразделах у нас програмы
         i=NISubDepsCount.getInitIndex()+1;
        while(i<=NISubDepsCount.getLastIndex())
        {

            List<String> names=new ArrayList<>();
            tr=new TextFileLineReader(new File(baseDir,getDeps().get(i).fileName+".txt"), StandardCharsets.UTF_16LE);//файл программ
            tr.setActionPerLine(s -> names.add(TextUtil.removeUTF8BOM(s)));
            tr.readAll();

            List<String> freqs=new ArrayList<>();
            TextFileLineReader  tr1=new TextFileLineReader(new File(baseDir,"f"+getDeps().get(i).fileName+".txt"), StandardCharsets.UTF_16LE);//файл частот
            tr1.setActionPerLine(s -> freqs.add(TextUtil.removeUTF8BOM(s)));
            tr1.readAll();

            System.out.println("Файл частот " + "F" + getDeps().get(i).fileName + ".txt");
            // запишим программы
            for(int k=0;k<names.size();k++)  getProgr().add(new Progr(-1, i, checkFreqs(freqs.get(k).replace(",", ".").replace(" ", "")),names.get(k)));


            i++;
        }



/**********/

        /** авторских комплексов ***/



        List<String>  ghComplexes=new ArrayList<>();

        //список  авторских комплексов
        tr=new TextFileLineReader(new File(baseDir,getDeps().get(2).fileName+".txt"), StandardCharsets.UTF_16LE);
        tr.setActionPerLine(s -> ghComplexes.add(TextUtil.removeUTF8BOM(s)));
        tr.readAll();


         i=1;
        for (String ghComplex : ghComplexes)
        {

            getCompl().add(new Complex(2, ghComplex));
            //список программ комплекса
            List<String> names=new ArrayList<>();
            tr=new TextFileLineReader(new File(baseDir,getDeps().get(2).fileName+i+".txt"), StandardCharsets.UTF_16LE);//файл программ
            tr.setActionPerLine(s -> names.add(TextUtil.removeUTF8BOM(s)));
            tr.readAll();

            //список частот программ комплекса
            List<String> freqs=new ArrayList<>();
            TextFileLineReader  tr1=new TextFileLineReader(new File(baseDir,"f"+getDeps().get(2).fileName+i+".txt"), StandardCharsets.UTF_16LE);//файл частот
            tr1.setActionPerLine(s -> freqs.add(TextUtil.removeUTF8BOM(s)));
            tr1.readAll();
            System.out.println("Файл частот " + "F" + getDeps().get(2).fileName +i + ".txt");
            // запишим программы
            for(int k=0;k<names.size();k++)  getProgr().add(new Progr(getCompl().size()-1, -1, checkFreqs(freqs.get(k).replace(",", ".").replace(" ", "")),names.get(k)));

            i++;
        }


        //добавим в комплексы программы и частоты




/**********/

        /** элементов ***/
        Counter ELSubDepsCount=new Counter(NISubDepsCount.getLastIndex());
        //список программ элементов
        tr=new TextFileLineReader(new File(baseDir,getDeps().get(3).fileName+".txt"), StandardCharsets.UTF_16LE);
        tr.setActionPerLine(s ->
        {
            getDeps().add(new Dep(getDeps().get(3).fileName+ELSubDepsCount.inc(),TextUtil.removeUTF8BOM(s),3));//подразделы, имена файлов это базовая часть укажет на файлы программ.F+имя это файлы частот
        });
        tr.readAll();

        //в подразделах у нас програмы
         i=ELSubDepsCount.getInitIndex()+1;
        while(i<=ELSubDepsCount.getLastIndex())
        {

            List<String> names=new ArrayList<>();
            tr=new TextFileLineReader(new File(baseDir,getDeps().get(i).fileName+".txt"), StandardCharsets.UTF_16LE);//файл программ
            tr.setActionPerLine(s -> names.add(TextUtil.removeUTF8BOM(s)));
            tr.readAll();

            List<String> freqs=new ArrayList<>();
            TextFileLineReader  tr1=new TextFileLineReader(new File(baseDir,"f"+getDeps().get(i).fileName+".txt"), StandardCharsets.UTF_16LE);//файл частот
            tr1.setActionPerLine(s -> freqs.add(TextUtil.removeUTF8BOM(s)));
            tr1.readAll();


            System.out.println("Файл частот "+"F"+getDeps().get(i).fileName+".txt");
            // запишим программы
            for(int k=0;k<names.size();k++)  getProgr().add(new Progr(-1,i,checkFreqs(freqs.get(k).replace(",",".").replace(" ", "")),names.get(k)));


            i++;
        }





    }


    private String checkFreqs(String freqs) throws Exception
    {

        freqs =   TextUtil.removeUTF8BOM(freqs);
        try {
            String[] split = freqs.split(";");
            for (String s : split)
            {
                String[] split1 = s.split("\\+");
                if(split1.length==1) Double.parseDouble(s);
                else for (String s1 : split1) Double.parseDouble(s1);


            }
        }catch (Exception e)
        {
            System.out.println(freqs);
            throw new Exception(e);
        }



        return freqs;
    }

    /**
     * Произведет импорт базы
     */
    public boolean execute()
    {
        try {
            buildStructure("rubase");
            buildStructure("enbase");
            buildStructure("grbase");

            //заполним базу.

            Map<Integer,Section> catslist=new HashMap<>();
            Map<Integer,ru.biomedis.biomedismair3.entity.Complex> complexlist=new HashMap<>();

            Section root=model.createSection(null,"Новая база частот","",true,model.getLanguage("ru"));
            model.addString(root.getName(), "New frequencies base", model.getDefaultLanguage());





            Section root2=null;
            Section temp=null;
            //создадим категории
            System.out.println("Создаем категории");
            for (Dep entry : deps_ru) {
                if(deps_ru.indexOf(entry)==1) continue;//пропустим неинфкекционные

                System.out.print(entry.name);

                    if(entry.parent==-1)temp=root;
                    else temp=  catslist.get(entry.parent);//индексы в массиве разделов исходном и результирующем совпадают!!

                root2=model.createSection(temp,entry.name,"",true,model.getLanguage("ru"));
                int ind=deps_ru.indexOf(entry);
                catslist.put(ind, root2);


                model.addString(root2.getName(), deps_el.get(ind).name, model.getLanguage("el"));
                model.addString(root2.getName(), deps_en.get(ind).name, model.getLanguage("en"));

                model.addString(root2.getDescription(), "", model.getLanguage("el"));
                model.addString(root2.getDescription(), "", model.getLanguage("en"));

                System.out.println("ОК");


            }









            System.out.println("создадим комплексы");
ru.biomedis.biomedismair3.entity.Complex compl=null;
            for (Complex entry : compl_ru) {


                System.out.print(entry.name);

                temp=  catslist.get(entry.dep);//индексы в массиве разделов исходном и результирующем совпадают!!

                compl = model.createComplex(entry.name, "", temp, true, model.getLanguage("ru"));

                int ind=compl_ru.indexOf(entry);
                complexlist.put(ind, compl);


                model.addString(compl.getName(), compl_el.get(ind).name, model.getLanguage("el"));
                model.addString(compl.getName(), compl_en.get(ind).name, model.getLanguage("en"));

                model.addString(compl.getDescription(), "", model.getLanguage("el"));
                model.addString(compl.getDescription(), "", model.getLanguage("en"));

                System.out.println("ОК");
            }







            System.out.println("создадим программы");
            ru.biomedis.biomedismair3.entity.Program program=null;
            for (Progr entry : prog_ru) {


                System.out.print(entry.name);



                if(entry.dep!=-1) {
                    program=model.createProgram(entry.name,"",entry.freqs,catslist.get(entry.dep),true,model.getLanguage("ru"));
                }
                    else
                    if (entry.complex != -1){
                        program = model.createProgram(entry.name, "", entry.freqs, complexlist.get(entry.complex), true, model.getLanguage("ru"));
                    }
                    else throw new Exception("Не верные индексы dep и complex");





                int ind=prog_ru.indexOf(entry);



                model.addString(program.getName(), prog_el.get(ind).name, model.getLanguage("el"));
                model.addString(program.getName(), prog_en.get(ind).name, model.getLanguage("en"));

                model.addString(program.getDescription(), "", model.getLanguage("el"));
                model.addString(program.getDescription(), "", model.getLanguage("en"));

                System.out.println("ОК");
            }
            System.out.println("Ок");



        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }



        return true;
    }
}
