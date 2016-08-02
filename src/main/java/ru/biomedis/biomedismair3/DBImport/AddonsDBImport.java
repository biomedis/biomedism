package ru.biomedis.biomedismair3.DBImport;

import ru.biomedis.biomedismair3.ModelDataApp;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;


import java.io.File;


/**
 * Created by Anama on 09.11.2015.
 */
public class AddonsDBImport
{
    private ModelDataApp model;
    private File dbPath=new File("./addons");

    public AddonsDBImport(ModelDataApp model) {
        this.model = model;
    }

    public boolean execute()
    {

        boolean res=true;


        FileProfileParser ru=new FileProfileParser();
        FileProfileParser eng=new FileProfileParser();
        try
        {
            res= ru.parse(new File(dbPath,"ru.xmlp"),model);
            if(!res) {return false;}
            res= eng.parse(new File(dbPath,"eng.xmlp"),model);
            if(!res) {return false;}
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        //заполним базу.

        try
        {
            System.out.print("Создается раздел  - Профилактические комплексы для начинающих...");
            Section section =  model.createSection(model.findSection(2),"Профилактические комплексы","",true,model.getLanguage("ru"));
             model.addString(section.getName(),"Preventive complexes",model.getLanguage("en"));
             System.out.println("OK");

            for (FileProfileParser.Complex complex : ru.listComplex) {

                System.out.print("Создается комплекс  - "+complex.name+"...");

                complex.complex =  model.createComplex(complex.name,complex.descr,section,true,model.getLanguage("ru"));

                int i = ru.listComplex.indexOf(complex);

                model.addString( complex.complex.getName(),eng.listComplex.get(i).name,model.getLanguage("en"));
                model.addString( complex.complex.getDescription(),eng.listComplex.get(i).descr,model.getLanguage("en"));

                System.out.println("OK");
            }

            for (FileProfileParser.Program program : ru.listProgram) {
                System.out.print("Создается программа  - "+program.name+"...");
                program.program = model.createProgram(program.name,program.descr,program.freqs,ru.listComplex.get(program.complexIndex).complex,true,model.getLanguage("ru"));
                int i =ru.listProgram.indexOf(program);
                model.addString( program.program.getName(),eng.listProgram.get(i).name,model.getLanguage("en"));
                model.addString( program.program.getDescription(),eng.listProgram.get(i).descr,model.getLanguage("en"));
                System.out.println("OK");
            }
            System.out.print("Успешно!!!");
            res=true;

        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.print("Ошибка!!!");
            return false;
        }





        return res;
    }
}
