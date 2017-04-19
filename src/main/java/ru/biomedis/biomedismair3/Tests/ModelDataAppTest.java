/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.Tests;

import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.Tests.TestsFramework.BaseTest;
import ru.biomedis.biomedismair3.Tests.TestsFramework.TestClass;
import ru.biomedis.biomedismair3.Tests.TestsFramework.TestItem;
import ru.biomedis.biomedismair3.entity.*;

import java.util.List;

import static org.junit.Assert.*;

/**
 *Тесты проводятся последовательно, вручную отключаем и включаем их.
 * Есть тесты создания и удаления. нужно их попарно включать и выключать, также проводить по одному чтобы проверить только создание например.
 * Также могут быть варианты тестов, см. названия тестов
 * @author Anama
 */
@TestClass(name="ModelDataAppTest" ,ignore = false)
public class ModelDataAppTest extends BaseTest
{


    public ModelDataAppTest() 
    {
        super();
        
        addTestItem(new TestItem("Тест1. Тест проверки создания разделов, комплексов и программ", true) {

            @Override
            public void payload(App ctx) throws Exception {
                message("для удобства лучше использовать пустую базу");


                assertNotNull("getProgramLanguage is null", ctx.getModel().getProgramLanguage());


                Section section1 = ctx.getModel().createSection(null, "Раздел1", "", true, ctx.getModel().getProgramLanguage());
                Section section2 = ctx.getModel().createSection(null, "Раздел2", "", true, ctx.getModel().getProgramLanguage());
                Section section3 = ctx.getModel().createSection(null, "Раздел3", "", true, ctx.getModel().getProgramLanguage());

                Section section4 = ctx.getModel().createSection(section1, "Раздел1_1", "", true, ctx.getModel().getProgramLanguage());

                assertNotNull("section4 is null", section4);

                assertNotNull("section1 is null", section1);
                assertNotNull("section2 is null", section2);
                assertNotNull("section3 is null", section3);

                Complex complex1 = ctx.getModel().createComplex("Комплекс 1", "", section1, true, ctx.getModel().getProgramLanguage());
                Complex complex2 = ctx.getModel().createComplex("Комплекс 1", "", section1, true, ctx.getModel().getProgramLanguage());
                Complex complex3 = ctx.getModel().createComplex("Комплекс 1", "", section1, true, ctx.getModel().getProgramLanguage());

                assertNotNull("complex1 is null", complex1);
                assertNotNull("complex2 is null", complex2);
                assertNotNull("complex3 is null", complex3);


                Program program1;
                Program program2;
                Program program3;
                for (int i = 0; i < 5; i++) {
                    program1 = ctx.getModel().createProgram("програма " + i, "", "10;9;3;", complex1, true, ctx.getModel().getProgramLanguage());
                    program2 = ctx.getModel().createProgram("програма " + i, "", "10;9;3;", complex2, true, ctx.getModel().getProgramLanguage());
                    program3 = ctx.getModel().createProgram("програма " + i, "", "10;9;3;", complex3, true, ctx.getModel().getProgramLanguage());
                    assertNotNull("program1 is null", program1);
                    assertNotNull("program2 is null", program2);
                    assertNotNull("program3 is null", program3);

                }

                Program program5 = ctx.getModel().createProgram("програма ", "", "10;9;3;", section1, true, ctx.getModel().getProgramLanguage());
                assertNotNull("program3 is null", program5);


            }
        });
        addTestItem(new TestItem("Тест2. Тест удаления результатов Теста 1. ", true) {

            @Override
            public void payload(App ctx) throws Exception
            {

                int cSection = ctx.getModel().countSection();
                int cProg = ctx.getModel().countProgram();
                int cComplex = ctx.getModel().countComplex();
                int cStrings = ctx.getModel().countStrings();

                message("Разделов " + cSection);
                message("Программ " + cProg);
                message("Комплексов " + cComplex);
                message("Строк " + cStrings);


                List<Section> allRootSection = ctx.getModel().findAllRootSection();
                List<Section> allSectionByParent = ctx.getModel().findAllSectionByParent(null);

                assertTrue("findAllRootSection() != findAllSectionByParent(null)", allRootSection.size() == allSectionByParent.size());
                allSectionByParent.clear();

                for (Section section : allRootSection)
                {
                    ctx.getModel().clearSection(section);

                    for (Section section1 : ctx.getModel().findAllSectionByParent(section)) {
                        ctx.getModel().clearSection(section1);
                        ctx.getModel().removeSection(section1);
                    }
                    ctx.getModel().removeSection(section);

                }




                int cSection2 = ctx.getModel().countSection();
                int cProg2 = ctx.getModel().countProgram();
                int cComplex2= ctx.getModel().countComplex();
                int cStrings2 = ctx.getModel().countStrings();


                assertTrue("Разделов "+ cSection+". Не удалились разделы "+cSection2,cSection2==0);
                assertTrue("Программ "+ cProg+". Не  удалились программы "+cProg2,cProg2==0);
                assertTrue("Комплексов "+ cComplex+". Не  удалились комплексы "+cComplex2,cComplex2==0);
                assertTrue("Строк "+ cStrings+". Не  удалились строки "+cStrings2,cStrings2==0);

                message("Все данные успешно удалены");





            }
        });



          addTestItem(new TestItem("Тест3. Тестирование создания профилей и терапии", true) {
            
            @Override
            public void payload(App ctx) throws Exception
            {

                assertNotNull("getProgramLanguage is null", ctx.getModel().getProgramLanguage());


                //раздел
                Section section1 = ctx.getModel().createSection(null, "Раздел1", "", true, ctx.getModel().getProgramLanguage());




                assertNotNull("section1 is null",section1);

                //комплекс
                Complex complex1 = ctx.getModel().createComplex("Комплекс 1", "", section1, true, ctx.getModel().getProgramLanguage());


                assertNotNull("complex1 is null",complex1);



                Program program1=null;

                //программы комплекса
                for(int i=0;i<5;i++)
                {
                    program1= ctx.getModel().createProgram("програма " + i, "", "10;9;3;", complex1, true, ctx.getModel().getProgramLanguage());
                    assertNotNull("program1 is null",program1);


                }
//программа в разделе
                Program  program5= ctx.getModel().createProgram("програма раздела ", "", "10;9;3;", section1, true, ctx.getModel().getProgramLanguage());
                assertNotNull("program3 is null",program5);




                //профиль
                Profile profile =ctx.getModel().createProfile("профиль");
                assertNotNull("profile is null",profile);



                //тер.комплекс на основе комплекса. Должны создаться тер програмы
                TherapyComplex therapyComplex1 = ctx.getModel().createTherapyComplex(profile, complex1, 100,0);
                assertNotNull("therapyComplex1 is null", therapyComplex1);


                message("тер.комплекс на основе комплекса. ");
                List<TherapyProgram> therapyPrograms1 = ctx.getModel().findTherapyPrograms(therapyComplex1);
                assertTrue("Не создались терапевтические программы на основе комплекса", therapyPrograms1.size() > 0);
                message("тер.комплекс - " + therapyComplex1.getName());
                for (TherapyProgram itm : therapyPrograms1) {

                    assertTrue("Отсутствуют имена терапевтических програм", !itm.getName().isEmpty());
                    message("     тер.программы - " + itm.getName());
                }


                message("тер.комплекс на основе ИМЕНИ. " );
                //терр комплекс на основе имени. Те пустой
                TherapyComplex therapyComplex2 = ctx.getModel().createTherapyComplex(profile, "Терап компл","описа", 100,1);
                assertNotNull("therapyComplex2 is null", therapyComplex2);
                message("тер.комплекс - " + therapyComplex2.getName());

                TherapyProgram therapyProgram = ctx.getModel().createTherapyProgram(therapyComplex2, program5.getNameString(),program5.getDescriptionString(),program5.getFrequencies());//программа из секции
                assertNotNull("therapyProgram is null", therapyProgram);
                message("       тер.программа - " + therapyProgram.getName());


                // программа из комплекса добавим ее вручную. также она добавлена в дрой терр комплекс
                TherapyProgram therapyProgra2 = ctx.getModel().createTherapyProgram(therapyComplex2, program5.getNameString(),program1.getDescriptionString(),program1.getFrequencies());
                assertNotNull("therapyProgram is null", therapyProgra2);
                message("       тер.программа - " + therapyProgra2.getName());



            }
        });


        addTestItem(new TestItem("Тест4. Удаление результатов теста 3. Версия 1",true) {
            @Override
            public void payload(App context) throws Exception {
                message("Удаляется профиль и как следствие все тер. комплексы и програмы");

                for (Profile profile : context.getModel().findAllProfiles()) {
                    context.getModel().removeProfile(profile);
                }
                List<Profile> allProfiles = context.getModel().findAllProfiles();

                assertTrue("не удалились профили",allProfiles.size()==0);

                assertTrue("не удалилились терап.комплексы",context.getModel().countTherapyComplex()==0);
                assertTrue("не удалилились терап.программы",context.getModel().countTherapyProgram()==0);

            }
        });

        addTestItem(new TestItem("Тест4. Удаление результатов теста 3. Версия 2",true) {
            @Override
            public void payload(App ctx) throws Exception {
                message("Удаляются все комплексы и програмы. должны удалиться и терапевтические тоже.");

                int cSection = ctx.getModel().countSection();
                int cProg = ctx.getModel().countProgram();
                int cComplex = ctx.getModel().countComplex();
                int cStrings = ctx.getModel().countStrings();
                int cTc1= ctx.getModel().countTherapyComplex();
                int cTp1 = ctx.getModel().countTherapyProgram();

                message("Разделов " + cSection);
                message("Программ " + cProg);
                message("Комплексов " + cComplex);
                message("Строк " + cStrings);
                message("терр.Комплексов " + cTc1);
                message("терр.программ " + cTp1);

                List<Section> allRootSection = ctx.getModel().findAllRootSection();


                for (Section section : allRootSection)
                {
                    ctx.getModel().clearSection(section);

                    for (Section section1 : ctx.getModel().findAllSectionByParent(section)) {
                        ctx.getModel().clearSection(section1);
                        ctx.getModel().removeSection(section1);
                    }
                    ctx.getModel().removeSection(section);

                }




                int cSection2 = ctx.getModel().countSection();
                int cProg2 = ctx.getModel().countProgram();
                int cComplex2= ctx.getModel().countComplex();
                int cStrings2 = ctx.getModel().countStrings();


                assertTrue("Разделов "+ cSection+". Не удалились разделы "+cSection2,cSection2==0);
                assertTrue("Программ "+ cProg+". Не  удалились программы "+cProg2,cProg2==0);
                assertTrue("Комплексов "+ cComplex+". Не  удалились комплексы "+cComplex2,cComplex2==0);
                assertTrue("Строк "+ cStrings+". Не  удалились строки "+cStrings2,cStrings2==0);

                int cTc2= ctx.getModel().countTherapyComplex();
                int cTp2 = ctx.getModel().countTherapyProgram();

                assertTrue("Тер.комплексов "+ cTc2+".  Не удалилились терап.комплексы "+cTc2,cTp2==0);
                assertTrue("Тер.комплексов "+ cTp2+". Не удалилились терап.программы "+cTp2,cTp2==0);


                message("Все данные успешно удалены");

            }
        });
        addTestItem(new TestItem("тест добавления строки",false) {
            @Override
            public void payload(App context) throws Exception {
              Section  root=context.getModel().createSection(null, "Старая база частот", "", true, context.getModel().getLanguage("ru"));

                context.getModel().addString(root.getName(), "Old frequencies base", context.getModel().getDefaultLanguage());

                context.getModel().addString(root.getDescription(), "", context.getModel().getDefaultLanguage());

                assertNotEquals("Одинаковые языки!",context.getModel().getDefaultLanguage(),context.getModel().getLanguage("ru"));
                context.getModel().addString(root.getDescription(), "", context.getModel().getLanguage("ru"));
            }
        });
    }





    
    
    
    
    
    
    
}
