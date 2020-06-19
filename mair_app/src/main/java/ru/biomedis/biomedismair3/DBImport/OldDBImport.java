package ru.biomedis.biomedismair3.DBImport;


import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;
import ru.biomedis.biomedismair3.utils.Text.TextUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Anama on 25.06.2015.
 */
public class OldDBImport
{

    Connection c = null;
    ModelDataApp model;

    Map<Integer,String> cats_ru=new LinkedHashMap<>();
    Map<Integer,SubCats> subcats_ru=new LinkedHashMap<>();
    Map<Integer,Progr> progs_ru=new LinkedHashMap<>();

    Map<Integer,String> cats_de=new LinkedHashMap<>();
    Map<Integer,SubCats> subcats_de=new LinkedHashMap<>();
    Map<Integer,Progr> progs_de=new LinkedHashMap<>();

    Map<Integer,String> cats_el=new LinkedHashMap<>();
    Map<Integer,SubCats> subcats_el=new LinkedHashMap<>();
    Map<Integer,Progr> progs_el=new LinkedHashMap<>();

    Map<Integer,String> cats_en=new LinkedHashMap<>();
    Map<Integer,SubCats> subcats_en=new LinkedHashMap<>();
    Map<Integer,Progr> progs_en=new LinkedHashMap<>();

    Map<Integer,String> cats_ro=new LinkedHashMap<>();
    Map<Integer,SubCats> subcats_ro=new LinkedHashMap<>();
    Map<Integer,Progr> progs_ro=new LinkedHashMap<>();

    public OldDBImport(ModelDataApp model)
    {
        this.model=model;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:healerbase_ru.sqlite");
            execute(cats_ru, subcats_ru, progs_ru);
            c.close();

            c = DriverManager.getConnection("jdbc:sqlite:healerbase_de.sqlite");
            execute(cats_de, subcats_de, progs_de);
            c.close();

            c = DriverManager.getConnection("jdbc:sqlite:healerbase_el.sqlite");
            execute(cats_el, subcats_el, progs_el);
            c.close();

            c = DriverManager.getConnection("jdbc:sqlite:healerbase_ro.sqlite");
            execute(cats_ro, subcats_ro, progs_ro);
            c.close();

            c = DriverManager.getConnection("jdbc:sqlite:healerbase_eng.sqlite");
            execute(cats_en, subcats_en, progs_en);
            c.close();



        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");



    }


    class SubCats
    {
       public int id;
        public String name;
        public int catsId;

    }
    class Progr
    {

        int id;
        String freqs;
        String name;
        int subCatsId;
    }



    public boolean importDB()
    {

        Map<Integer,Section> catslist=new HashMap<>();
        Map<Integer,Section>subcatslist=new HashMap<>();

        //создадим категории , в них подкатегории.
        //далее создадим мэп с ними, чтобы использовать для создания программ. Далее пройдем по списку программ и запихем
        //их в нужные категории секции
        String catName="";
        Section root=null;
        Section root2=null;
        int nameId;
        int descrId;
        try {


            root=model.createSection(null,"Старая база частот","",true,model.getLanguage("ru"));
            model.addString(root.getName(),"Old frequencies base",model.getDefaultLanguage());
            //model.addString(root.getDescription(),"",model.getDefaultLanguage());




            //создадим категории
            System.out.println("Создаем категории");
            for (Map.Entry<Integer, String> entry : cats_ru.entrySet()) {

                if(entry.getKey()==4) continue;//игнор пользовательского раздела
                System.out.println("   "+entry.getValue());

                root2=model.createSection(root,entry.getValue(),"",true,model.getLanguage("ru"));

                catslist.put(entry.getKey(), root2);


                model.addString(root2.getName(), cats_de.get(entry.getKey()), model.getLanguage("de"));
                model.addString(root2.getName(), cats_ro.get(entry.getKey()), model.getLanguage("ro"));
                model.addString(root2.getName(), cats_el.get(entry.getKey()), model.getLanguage("el"));
                model.addString(root2.getName(), cats_en.get(entry.getKey()), model.getLanguage("en"));

                model.addString(root2.getDescription(), "", model.getLanguage("de"));
                model.addString(root2.getDescription(), "", model.getLanguage("ro"));
                model.addString(root2.getDescription(), "", model.getLanguage("el"));
                model.addString(root2.getDescription(), "", model.getLanguage("en"));




            }
            System.out.println("Ок");
            System.out.println("создадим подкатегории");
            //создадим подкатегории
            int i=0;
            for (Map.Entry<Integer, SubCats> entry : subcats_ru.entrySet()) {
                if(entry.getValue().catsId==4) continue;
                System.out.println(++i + "   " + entry.getValue().name);


                root2=model.createSection(catslist.get(entry.getValue().catsId),entry.getValue().name,"",true,model.getLanguage("ru"));
                subcatslist.put(entry.getValue().id, root2);


                model.addString(root2.getName(), subcats_de.get(entry.getKey()).name, model.getLanguage("de"));
                model.addString(root2.getName(), subcats_ro.get(entry.getKey()).name, model.getLanguage("ro"));
                model.addString(root2.getName(), subcats_el.get(entry.getKey()).name, model.getLanguage("el"));
                model.addString(root2.getName(), subcats_en.get(entry.getKey()).name, model.getLanguage("en"));

                model.addString(root2.getDescription(), "", model.getLanguage("de"));
                model.addString(root2.getDescription(), "", model.getLanguage("ro"));
                model.addString(root2.getDescription(), "", model.getLanguage("el"));
                model.addString(root2.getDescription(), "", model.getLanguage("en"));




            }
            System.out.println("Ок");


            System.out.println("создадим программы");
            Program program = null;
            i = 0;

            for (Map.Entry<Integer, Progr> entry : progs_ru.entrySet()) {
                System.out.print(++i + "   " + entry.getValue().name + " -- " + entry.getValue().freqs + " --- ...");

                if(entry.getValue().freqs.equals("0.0"))System.out.println("DDDDDDDDDDDDD "+entry.getValue().freqs);



                if(entry.getValue().freqs.isEmpty()){ System.err.print(" пУСТОЙ НАБОР ЧАСТОТ"); continue;}

                program = model.createProgram(entry.getValue().name, "", TextUtil.checkFreqs(entry.getValue().freqs), subcatslist.get(entry.getValue().subCatsId), true, model.getLanguage("ru"));

                if (i == 3124)
                {
                    System.err.print("");
                      }

                model.addString(program.getName(), progs_de.get(entry.getKey()).name, model.getLanguage("de"));
                model.addString(program.getName(), progs_ro.get(entry.getKey()).name, model.getLanguage("ro"));
                model.addString(program.getName(), progs_el.get(entry.getKey()).name, model.getLanguage("el"));
                model.addString(program.getName(), progs_en.get(entry.getKey()).name, model.getLanguage("en"));


                model.addString(program.getDescription(),"", model.getLanguage("de"));
                model.addString(program.getDescription(), "", model.getLanguage("ro"));
                model.addString(program.getDescription(), "", model.getLanguage("el"));
                model.addString(program.getDescription(), "", model.getLanguage("en"));





                System.out.println("..Ок");


            }
            System.out.println("Ок");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
return true;
    }


    private  void execute(Map<Integer,String> cats,Map<Integer,SubCats> subcats,Map<Integer,Progr> progs) {



        Statement stmt = null;
        try {
            stmt = c.createStatement();


        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ResultSet rs=null;
        System.out.print("Получение Категорий");
        try
        {
            rs = stmt.executeQuery("SELECT * FROM Cats;");
            while (rs.next())
            {
                cats.put(rs.getInt("_id"), rs.getString("name"));
                // System.out.print(".");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }finally
        {
            try {
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }
        System.out.println("  ОК");



        SubCats sbcts=null;
        System.out.print("Получение ПодКатегорий");
        try
        {
            rs = stmt.executeQuery("SELECT * FROM Sub_cats;");
            while (rs.next())
            {
                sbcts=new SubCats();
                sbcts.id=rs.getInt("_id");
                sbcts.name=rs.getString("name");
                sbcts.catsId=rs.getInt("Cats_id");
                //System.out.print(".");
                subcats.put( sbcts.id,sbcts);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }finally
        {
            try {
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }

        System.out.println("  ОК");

        System.out.print("Получение Программ");
        Progr prgr=null;
        try
        {
            rs = stmt.executeQuery("SELECT * FROM Therapy where subcats_id<>115;");//исключим програмы юзера
            while (rs.next())
            {
                prgr=new Progr();
                prgr.id= rs.getInt("_id");
                prgr.subCatsId=  rs.getInt("subcats_id");
                prgr.name=rs.getString("name");
                prgr.freqs="";
                progs.put(prgr.id, prgr);

                // System.out.print(".");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }finally
        {
            try {
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }


        System.out.println("  ОК");


        System.out.print("Получение частот");
//получим частоты для программ
        try
        {
            rs = stmt.executeQuery("SELECT * FROM freq_therapy;");


            while (rs.next())
            {
                //исключим парсинг частот принадлежащим исключенными программам(с выше, исключили програмы раздела мои програмы) therapy_id это id програмы
               if( !progs.containsKey(rs.getInt("therapy_id")) ) continue;

                        if(progs.get(rs.getInt("therapy_id")).freqs.isEmpty()) progs.get(rs.getInt("therapy_id")).freqs+=rs.getString("freq");
                       else  progs.get(rs.getInt("therapy_id")).freqs+=";"+rs.getString("freq");

                // System.out.print(".");
            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }finally
        {
            try {
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }

        System.out.println("  ОК");

        /*
        System.out.print("Очистка");
        for (Map.Entry<Integer, Progr> entry : progs.entrySet())
        {
            if(!entry.getValue().freqs.isEmpty()) entry.getValue().freqs=entry.getValue().freqs.substring(1);
            //System.out.print(".");
        }

        System.out.println("  ОК");
        */


//закроем сединение
        try {
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

}

