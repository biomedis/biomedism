/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3;

import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import ru.biomedis.biomedismair3.JPAControllers.*;
import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
import ru.biomedis.biomedismair3.entity.*;

import javax.persistence.*;
import java.io.File;
import java.text.Collator;
import java.util.*;

import static ru.biomedis.biomedismair3.Log.logger;


/**
 *
 * @author Anama
 */
public class ModelDataApp {

    private IInProfileChanged onInProfileChanged;
    public interface IInProfileChanged{
         void change( long profileID);
    }

    public void setInProfileChanged(IInProfileChanged listener){
        onInProfileChanged = listener;
    }


    private final EntityManagerFactory emf;
    private final ComplexJpaController complexDAO;
    private final ProgramJpaController programDAO;
    private final ProfileJpaController profileDAO;
    private final TherapyComplexJpaController therapyComplexDAO;
    private final TherapyProgramJpaController therapyProgramDAO;
    private final LanguageJpaController languageDAO;
    private final LocalizedStringJpaController localizedStringDAO;
    private final StringsJpaController stringsDAO;
    private final SectionJpaController sectionDAO;
    private final ProgramOptionsJpaController optionsDAO;
    
    
    private Locale programLocale;
    private Locale systemLocale;
    private Language userLanguage;
    private  Language defaultLanguage;
    private final Map<String,Language> languageMap=new HashMap<>();
    private final Map<Long,Language> languageMapID=new HashMap<>();
    private final  Map<String,ProgramOptions> optionsMap=new HashMap<>();

    private final Map<String,INamedComparator> comparators=new HashMap<>();//компараторы для языков



    
    /**
     *
     * @param emf
     */
    public ModelDataApp(EntityManagerFactory emf) {
        this.emf = emf;
        complexDAO = new ComplexJpaController(emf);
        programDAO = new ProgramJpaController(emf);
        profileDAO = new ProfileJpaController(emf);
        therapyComplexDAO = new TherapyComplexJpaController(emf);
        therapyProgramDAO = new TherapyProgramJpaController(emf);
        languageDAO = new LanguageJpaController(emf);
        localizedStringDAO = new LocalizedStringJpaController(emf);
        stringsDAO = new StringsJpaController(emf);
        sectionDAO = new SectionJpaController(emf);
        optionsDAO=new ProgramOptionsJpaController(emf);
        systemLocale= Locale.getDefault();


        init();
          
    }
    
    /**
     * Всякая инициализация 
     */
    private void init()
    {
        //проверим и создадим данные языков
        List<Language> findAllLanguage = new ArrayList<>();//findAllLanguage();


            //создадим тут списочек           
            findAllLanguage.add(createLanguage("user", "Язык ввода пользователя"));
            findAllLanguage.add(createLanguage("hr", "хорватский"));
            findAllLanguage.add(createLanguage("zh", "китайский"));
            findAllLanguage.add(createLanguage("ro", "румынский"));
            findAllLanguage.add(createLanguage("ca", "каталанский"));
            findAllLanguage.add(createLanguage("rm", "ретороманский"));
            findAllLanguage.add(createLanguage("vi", "вьетнамский"));
            findAllLanguage.add(createLanguage("tr", "турецкий"));
            findAllLanguage.add(createLanguage("hu", "венгерский"));
            findAllLanguage.add(createLanguage("fil", "филиппинский"));
            findAllLanguage.add(createLanguage("lv", "латышский"));
            findAllLanguage.add(createLanguage("lt", "Lietuvių", true));
            findAllLanguage.add(createLanguage("hi", "хинди"));
            findAllLanguage.add(createLanguage("th", "тайский"));
            findAllLanguage.add(createLanguage("de", "Deutsch",true));
            findAllLanguage.add(createLanguage("fi", "финский"));
            findAllLanguage.add(createLanguage("sv", "шведский"));
            findAllLanguage.add(createLanguage("fr", "Français",true));
            findAllLanguage.add(createLanguage("bg", "болгарский"));
            findAllLanguage.add(createLanguage("sl", "словенский"));
            findAllLanguage.add(createLanguage("sk", "словацкий"));
            findAllLanguage.add(createLanguage("uk", "украинский"));
            findAllLanguage.add(createLanguage("da", "датский"));
            findAllLanguage.add(createLanguage("it", "Italiano",true));
            findAllLanguage.add(createLanguage("sr", "сербский"));
            findAllLanguage.add(createLanguage("iw", "иврит"));
            findAllLanguage.add(createLanguage("ko", "корейский"));
            findAllLanguage.add(createLanguage("fa", "персидский"));
            findAllLanguage.add(createLanguage("ar", "арабский"));
            findAllLanguage.add(createLanguage("in", "индонезийский"));
            findAllLanguage.add(createLanguage("cs", "чешский"));
            findAllLanguage.add(createLanguage("nb", "норвежский букмол"));
            findAllLanguage.add(createLanguage("el", "Ελληνική",true));
            findAllLanguage.add(createLanguage("pt", "португальский"));
            findAllLanguage.add(createLanguage("pl", "польский"));
            findAllLanguage.add(createLanguage("en", "English",true));
            findAllLanguage.add(createLanguage("ru", "Русский",true));
            findAllLanguage.add(createLanguage("es", "испанский"));
            findAllLanguage.add(createLanguage("nl", "голландский"));
            findAllLanguage.add(createLanguage("ja", "японский"));

        for(Language lang:findAllLanguage){
            languageMap.put(lang.getAbbr(), lang);
            languageMapID.put(lang.getId(), lang);
        }
        
        defaultLanguage=languageMap.get("en");
        userLanguage=languageMap.get("user");
        //setProgramLanguageDefault();


        //работа с опциями. Стоит сделать проферку наличия конкретных опций, чтобы добавлять их без очистки базы
        List<ProgramOptions> allOptions = findAllOptions();
        if(allOptions.stream().filter(option -> option.getName().equals("enable_autoupdate")).count()==0)
        {
            ProgramOptions option = createOption("enable_autoupdate", "true");
            if(option!=null) optionsMap.put(option.getName(),option);
        }
        if(allOptions.stream().filter(option -> option.getName().equals("mp3_path")).count()==0)
        {
            ProgramOptions option = createOption("mp3_path", "");
            if(option!=null) optionsMap.put(option.getName(),option);
        }
        if(allOptions.stream().filter(option -> option.getName().equals("last_export_path")).count()==0)
        {
            ProgramOptions option = createOption("last_export_path", "");
            if(option!=null) optionsMap.put(option.getName(),option);
        }

        if(allOptions.stream().filter(option -> option.getName().equals("last_saved_files_path")).count()==0)
        {
            ProgramOptions option = createOption("last_saved_files_path", "");
            if(option!=null) optionsMap.put(option.getName(),option);
        }

        if(allOptions.stream().filter(option -> option.getName().equals("data_path")).count()==0)
        {
            ProgramOptions option = createOption("data_path", "");
            if(option!=null) optionsMap.put(option.getName(),option);
        }

        if(allOptions.stream().filter(option -> option.getName().equals("device.disk.mark")).count()==0)
        {
            ProgramOptions option = createOption("device.disk.mark", "BIOMEDIS-M");
            if(option!=null) optionsMap.put(option.getName(),option);
        }

        if(allOptions.stream().filter(option -> option.getName().equals("app.lang")).count()==0)
        {
            ProgramOptions option = createOption("app.lang", "");//опция есть но пустая. пока язык не выбран, будет определяться автоматически. см App
            if(option!=null) optionsMap.put(option.getName(),option);
        }



        if(allOptions.stream().filter(option -> option.getName().equals("app.lang_insert_complex")).count()==0)
        {
            ProgramOptions option = createOption("app.lang_insert_complex", "");//опция есть но пустая. пока язык не выбран. Язык вставки комплекса. Устанвливается автоматически при перекл, языка в программе, но может быть выбран вручную
            if(option!=null) optionsMap.put(option.getName(),option);
        }

        if(allOptions.stream().filter(option -> option.getName().equals("codec.path")).count()==0)
        {
            ProgramOptions option = createOption("codec.path", "");
            if(option!=null) optionsMap.put(option.getName(),option);
        }
        allOptions = findAllOptions();//получим опции с новыми
        allOptions.stream().forEach(option->optionsMap.put(option.getName(),option));


        //пользовательская база

        Section userBase=null;
        try
        {
            userBase = findAllSectionByTag("USER");

        }catch (NoResultException ex)
        {
            try {
                userBase= createSection(null, "Пользовательская база", "", "USER", false, getLanguage("ru"));
                addString(userBase.getName(), "User base", getDefaultLanguage());

            } catch (Exception e) {
                Log.logger.error("",e);
            }

        }catch (NonUniqueResultException ex)
        {
            Log.logger.error("",ex);
        }


        //создание компараторов с учетом языка
        findAllLanguage.stream().filter(i->i.isAvaliable()).forEach(i->comparators.put(i.getAbbr(),
                new INamedComparator(Collator.getInstance(new Locale(i.getAbbr())))));

        comparators.put("user",
                new INamedComparator(Collator.getInstance(getSystemLocale())));

    }

    public INamedComparator getComparator(Language lang){return comparators.get(lang.getAbbr());}
    public INamedComparator getComparator(String lang){return comparators.get(lang);}

    /**
     * Вернет значение опции по имени
     * @param name
     * @return
     * @throws Exception
     */
    public String getOption(String name)throws Exception
    {
       if(optionsMap.containsKey(name)) return   optionsMap.get(name).getValue();
       else { throw new Exception("нет такой опции!");}

    }

    /**
     * Установить значение опции. Сохраняет опцию в базе
     * @param name
     * @param value
     * @throws Exception
     */
    public void setOption(String name,String value)throws Exception
    {
        if(optionsMap.containsKey(name))    updateOption(name,value);
        else throw new Exception("нет такой опции!");

    }
    /**
     * вернет установленную локаль программы
     * @return 
     */
    public Locale getProgramLocale(){return programLocale;}
    
    /**
     * Вернет сущность пользовательского языка(это просто для пометки пользовательского ввода - он не локализует и выводится как есть всегда)
     * @return 
     */
    public Language getUserLanguage(){return userLanguage;}
    
    
    /**
     * Получает объект сущности языка программы или дефолтный объект(для англ языка)
     * @return 
     */
    public Language getProgramLanguage()
    {
        //проверка может быть не надежна для экзотических языков если аббривиятуру в стандарте ISO поменят языка
        if(languageMap.containsKey(getProgramLocale().getLanguage())) return languageMap.get(getProgramLocale().getLanguage());
        return defaultLanguage;
                }
    /**
     * Возвратит язык по умолчанию
     * @return 
     */
    public Language getDefaultLanguage()
    {
        return defaultLanguage;
    }

    public Language getLanguage(String abbr) throws Exception {
        if(languageMap.containsKey(abbr)) return languageMap.get(abbr);
        else throw new Exception("Нет такого языка");
    }
    public Language getLanguage(long id) throws Exception {
        if(languageMapID.containsKey(id)) return languageMapID.get(id);
        else throw new Exception("Нет такого языка");
    }

    public Locale getSystemLocale(){return systemLocale;}

    /**
     * Установится язык программы. Можно получить getProgramLanguage() , сохраняется в настройках programLocale которая ставится как Locale.setDefault(programLocale); учитывается доступность локализации программы
     * @param locale локаль желаемого языка, если такого нет то будет выбран дефолтный
     */
    public void setProgramLanguage(Locale locale)
    {

        if(languageMap.containsKey(locale.getLanguage()))
        {
            if(languageMap.get(locale.getLanguage()).isAvaliable()) programLocale = locale;
           else  programLocale=new Locale(getDefaultLanguage().getAbbr());
        }
        else programLocale=new Locale(getDefaultLanguage().getAbbr());
        Locale.setDefault(programLocale);
    }


    /**
     * Установить язык  программы по умолчнию, англ
     */
    public void setProgramLanguageDefault()
    {
        programLocale=new Locale(getDefaultLanguage().getAbbr());
        Locale.setDefault(programLocale);
    }
    ///////////////////////////////////////////////////////////////////////


    public boolean isAutoUpdateEnable() {
        String enableAutoupdate = "true";
        try {
            enableAutoupdate = getOption("enable_autoupdate");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.valueOf(enableAutoupdate);

    }
    public void enableAutoUpdate()  {
        try {
            setOption("enable_autoupdate","true");
        } catch (Exception e) {
           Log.logger.error("",e);
        }
    }
    public void disableAutoUpdate() {
        try {
            setOption("enable_autoupdate","false");
        } catch (Exception e) {
            Log.logger.error("",e);
        }
    }


    public String getMp3Path(String defaultPath){
        try {
            String last_export_path = getOption("mp3_path");
            if(last_export_path.isEmpty())return defaultPath;
            if(new File(last_export_path).exists()==false)return defaultPath;
            else return last_export_path;
        } catch (Exception e) {
            return defaultPath;
        }
    }

    public void setMp3Path(File path)  {
        if(path==null)return;
        try {
            updateOption("mp3_path",path.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getLastExportPath(String defaultPath){
        try {
            String last_export_path = getOption("last_export_path");
            if(last_export_path.isEmpty())return defaultPath;
            if(new File(last_export_path).exists()==false)return defaultPath;
            else return last_export_path;
        } catch (Exception e) {
            return defaultPath;
        }
    }

    public void setLastExportPath(File path)  {
            if(path==null)return;
        try {
            updateOption("last_export_path",path.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getLastSavedFilesPath(String defaultPath){
        try {
            String last_saved_files_path = getOption("last_saved_files_path");
            if(last_saved_files_path.isEmpty())return defaultPath;
            if(new File(last_saved_files_path).exists()==false)return defaultPath;
            else return last_saved_files_path;
        } catch (Exception e) {
            return defaultPath;
        }
    }

    public void setLastSavedFilesPath(File path)  {
        if(path==null)return;
        try {
            updateOption("last_saved_files_path",path.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * Абривиатура языка вставки комплекса
     * @return Пустое значение, если неудачно
     */
    public  String getInsertComplexLang(){
        try {
            return  getOption("app.lang_insert_complex");
        } catch (Exception e) {
            logger.error("Ошибка получения языка вставки комплекса",e);
        }
        return "";
    }
    /** ОПЦИИ **/


    private ProgramOptions createOption(String name,String value)  {
        ProgramOptions opt=new ProgramOptions();
        opt.setName(name);
        opt.setValue(value);
        try {
            optionsDAO.create(opt);
        }
        catch (Exception ex) {
            Log.logger.error("",ex);
            opt=null;


        }

        return opt;
    }


    private List<ProgramOptions> findAllOptions()
    {
        return optionsDAO.findOptionsEntities();
    }


    /**
     * Получает опцию из базы
     * @param name
     * @return
     */
    public ProgramOptions findOptionInBase(String name){
        Query query=emf.createEntityManager().createQuery("Select o From ProgramOptions o Where o.name = :name").setMaxResults(1);
        query.setParameter("name",name);
        return (ProgramOptions )query.getSingleResult();


    }

    private void updateOption(String name,String value) throws Exception {

        if(optionsMap.containsKey(name))
        {
            ProgramOptions programOptions = optionsMap.get(name);
            programOptions.setValue(value);
            optionsDAO.edit(programOptions);
        }

    }
    /*************/
    
    
    
    
    
    
    
    /************** ПРОФИЛИ **************/
    //Обнуление поля therapy профиля и сохранения этой сущности приведет к удалению записи в таблице therapy
    public Profile createProfile(String name)throws Exception
    {      
        Profile profile=new Profile();

      
        try {

            profile.setName(name);
            profile.setUuid(UUID.randomUUID().toString());
            this.profileDAO.create(profile);
            profile.setLastChange(Calendar.getInstance().getTimeInMillis());

        } catch (Exception ex) {
            Log.logger.error("",ex);
            profile=null;

            throw new Exception("Ошибка создания профиля",ex);
        }

        if(profile!=null){
            profile.setPosition(profile.getId());
            updateProfile(profile);
        }

        
        return profile;
    }

    public Profile createProfile(String name, long position)throws Exception
    {
        Profile profile=new Profile();


        try {
            profile.setPosition(position);
            profile.setName(name);
            profile.setUuid(UUID.randomUUID().toString());
            profile.setLastChange(Calendar.getInstance().getTimeInMillis());
            this.profileDAO.create(profile);

        } catch (Exception ex) {
            Log.logger.error("",ex);
            profile=null;

            throw new Exception("Ошибка создания профиля",ex);
        }
        return profile;
    }
    
    public void updateProfile(Profile profile) throws Exception
    {
     profileDAO.edit(profile);
    }
    
    public void removeProfile(Profile profile) throws Exception
    {

        try {


            for (TherapyComplex therapyComplex : findTherapyComplexes(profile)) removeTherapyComplex(therapyComplex);
            profileDAO.destroy(profile.getId());
        }catch (Exception e)
        {
            Log.logger.error("",e);
            throw  e;
        }



    }
    
    public Profile getProfile(long id)
    {
        return profileDAO.findProfile(id);
    }
    
    public List<Profile> findAllProfiles()
    {
        Query query = emf.createEntityManager().createQuery("Select p From Profile p   order by p.name asc");

        return query.getResultList();
    }

    public Profile getLastProfile()
    {

        Query query = emf.createEntityManager().createQuery("Select p From Profile p   order by p.lastChange desc");
        query.setMaxResults(1);
        return (Profile)query.getSingleResult();
    }

    /**
     * Поиск профиля по части имени
     * @param text
     * @return
     */
    public List<Profile> searchProfile(String text)
    {

        Query query=emf.createEntityManager().createQuery("Select prof From Profile prof Where lower(prof.name) like lower(:txt)");
        query.setParameter("txt", "%"+text+"%");

        return query.getResultList();

    }


    public Profile duplicateProfile(Profile profile) throws Exception {
        Profile p = createProfile(profile.getName() + "_");
        for (TherapyComplex therapyComplex : findTherapyComplexes(profile)) {
            copyTherapyComplexToProfile(p, therapyComplex);
        }
        return p;
    }

    
    /******************************************/
    
       /************** Разделы **************/
    
    public Section createSection(Section parent,String name,String description, boolean isOwnerSystem,Language lang) throws Exception
    {
        
        Section section=new Section();      
        section.setOwnerSystem(isOwnerSystem);
        section.setParent(parent);
        section.setTag(null);//если вставлять пустой то будет ошибка из-за не уникальности.
        Strings sName=null;
        Strings sDescr=null;
        section.setUuid(UUID.randomUUID().toString());
        
        try
        {
         sName= createString(name, lang);
         sDescr= createString(description, lang);
        section.setDescription(sDescr);
        section.setName(sName);
        sectionDAO.create(section);
        }
        catch(Exception ex)
        {
            Log.logger.error("",ex);
            section=null;
            throw new Exception("Ошибка создания раздела",ex);
        }
        
        
       return section;
    }

    public Section createSection(Section parent,String name,String description,String tag, boolean isOwnerSystem,Language lang) throws Exception
    {

        Section section=new Section();
        section.setOwnerSystem(isOwnerSystem);
        section.setParent(parent);
        section.setTag(tag);
        Strings sName=null;
        Strings sDescr=null;
        section.setUuid(UUID.randomUUID().toString());

        try
        {
            sName= createString(name, lang);
            sDescr= createString(description, lang);
            section.setDescription(sDescr);
            section.setName(sName);
            sectionDAO.create(section);
        }
        catch(Exception ex)
        {
            Log.logger.error("",ex);
            section=null;
            throw new Exception("Ошибка создания раздела",ex);
        }


        return section;
    }
    /**
     * Очистит выбранный раздел от програм и комплексов
     * @param section
     * @throws Exception 
     */
    public void clearSection(Section section) throws Exception
    {
        //здесь нужно удалять программы и комплексы рекурсивно
        Strings description = section.getDescription();
        Strings name = section.getName();
        
        List<Complex> complexs = findAllComplexBySection(section);
        List<Program> programs = findAllProgramBySection(section);
        try {  

            
            
             for(Complex itm:complexs)removeComplex(itm);//удалит вложенные комплексы и программы в них
             for(Program itm:programs)removeProgram(itm);//удалит вложенные  программы
            
           
            
             
        } catch (Exception e) 
        {
            Log.logger.error("",e);
            throw new Exception("Ошибка удаления раздела",e);
        } 
    }

    /**
     * Удаляет раздел указанный с комплексами и программми. Если Раздел содержит дочерние разделы выбросит исключение
     * @param section
     * @throws Exception 
     */
    public void removeSection(Section section) throws Exception
    {
        
        //здесь нужно удалять программы и комплексы рекурсивно
        Strings description = section.getDescription();
        Strings name = section.getName();
        
        List<Complex> complexs = findAllComplexBySection(section);
        List<Program> programs = findAllProgramBySection(section);
        try {  
            if(!findAllSectionByParent(section).isEmpty()) throw new Exception("Раздел содержит дочерние разделы!");
            
            
             for(Complex itm:complexs)removeComplex(itm);//удалит вложенные комплексы и программы в них
             for(Program itm:programs)removeProgram(itm);//удалит вложенные  программы
            
             sectionDAO.destroy(section.getId());
             removeString(description);
             removeString(name);
            
             
        } catch (Exception e) 
        {
            Log.logger.error("",e);
            throw new Exception("Ошибка удаления раздела",e);
        }
       
    }
    
    public void updateSection(Section section) throws Exception
    {
        sectionDAO.edit(section);
    }
    
    
    
    public List<Section> findAllSectionByParent(Section parent)
    {
        Query query=null;
        
        if(parent!=null) 
        {
            query=emf.createEntityManager().createQuery("Select s From Section s WHERE s.parent=:parent");
            query.setParameter("parent",parent);      
        }
        else query=emf.createEntityManager().createQuery("Select s From Section s WHERE s.parent is NULL");
           
      return query.getResultList();
    }




    public Section findAllSectionByTag(String tag) throws NoResultException,NonUniqueResultException
    {
        Query query=null;

        if(!tag.isEmpty())
        {
            query=emf.createEntityManager().createQuery("Select s From Section s WHERE s.tag=:tag").setMaxResults(1);
            query.setParameter("tag",tag);
        }

        List<Section> res = (List<Section>)query.getResultList();
        if(res.size()!=0) return  res.get(0);
        else return null;
    }



     public List<Section> findAllRootSection()
    {
        Query query=null;      
       
            query=emf.createEntityManager().createQuery("Select s From Section s WHERE s.parent is NULL order by s.tag");
           
      return query.getResultList();
    }

    /**
     * Заполнить поля строк локализованных.
     * @param section
     */
    public void initStringsSection(Section section)
    {
        section.setNameString(getSmartString(section.getName()));
        section.setDescriptionString(getSmartString(section.getDescription()));

    }

    public void initStringsSection(List<Section> sections)
    {
        sections.stream().forEach(section -> {

                section.setNameString(getSmartString(section.getName()));
                section.setDescriptionString(getSmartString(section.getDescription()));

        });

    }
    /**
     * Заполнить поля строк локализованных.
     * @param p
     */
    public void initStringsSection(Section p,Language lang)
    {
        String name = getString(p.getName(), lang);
        String d = getString(p.getDescription(), lang);

        p.setNameString(name==null?"":name);
        p.setDescriptionString(d==null?"":d);

    }
    public void initStringsSection(Section p,Language lang,boolean defLang)
    {
        String name = getString(p.getName(), lang);
        String d = getString(p.getDescription(), lang);

        if(!defLang){
            p.setNameString(name==null?"":name);
            p.setDescriptionString(d==null?"":d);
        }else {
            p.setNameString(name == null ? getString(p.getName(),getDefaultLanguage()) : name);
            p.setDescriptionString(d == null ? getString(p.getDescription(),getDefaultLanguage()) : d);
        }

    }
    /**
     * Инициализация выбранным языком. Если  нет перевода, будет пустая строка
     * @param sections
     * @param lang
     *
     */
    public void initStringsSection(List<Section> sections,Language lang)
    {
        String name =null;
        String d=null;
        for(Section p : sections)
        {
            name =null;
            d=null;

            name = getString(p.getName(), lang);
            d = getString(p.getDescription(), lang);

            p.setNameString(name==null?"":name);
            p.setDescriptionString(d==null?"":d);


        }

    }

    /**
     * Инициализация выбранным языком. Если defLang==true, то если нет перевода, будет умная замена иначе пустая строка
     * @param sections
     * @param lang
     * @param defLang если нет перевода то выбрать перевод по умолчанию
     */
    public void initStringsSection(List<Section> sections,Language lang,boolean defLang)
    {
        String name =null;
        String d=null;
        for(Section p : sections)
        {
            name =null;
            d=null;

            name = getString(p.getName(), lang);
            d = getString(p.getDescription(), lang);

            if(!defLang){
                p.setNameString(name==null?"":name);
                p.setDescriptionString(d==null?"":d);
            }else {
                p.setNameString(name == null ? getString(p.getName(),getDefaultLanguage()) : name);
                p.setDescriptionString(d == null ? getString(p.getDescription(),getDefaultLanguage()) : d);
            }


        }

    }



      /******************************************/



  /************** Комплексы **************/
    
    public Complex createComplex(String name,String description,Section parent,boolean isOwnerSystem,Language lang) throws Exception
    {
        
        return createComplex( name, description, parent, isOwnerSystem, lang, 0);
    }

    public Complex createComplex(String name,String description,Section parent,boolean isOwnerSystem,Language lang,int timeForFreq) throws Exception
    {

        Complex complex=new Complex();
        complex.setOwnerSystem(isOwnerSystem);
        complex.setSection(parent);
        Strings sName=null;
        Strings sDescr=null;
        complex.setUuid(UUID.randomUUID().toString());
        complex.setTimeForFreq(timeForFreq);

        try
        {
            sName= createString(name, lang);
            sDescr= createString(description, lang);
            complex.setDescription(sDescr);
            complex.setName(sName);
            complexDAO.create(complex);
        }
        catch(Exception ex)
        {
            Log.logger.error("",ex);
            complex=null;
            throw new Exception("Ошибка создания комплекса",ex);
        }
        return complex;
    }

    public void removeComplex(Complex complex) throws Exception
    {
        //УДАЛИТЬ ТЕРАПЕВТ
       
        Strings description = complex.getDescription();
        Strings name = complex.getName();
        
        List<Program> programs = findAllProgramByComplex(complex);

        try {        
            //удаляем програмы
           for(Program itm:programs) removeProgram(itm);//автоматически удаляться все программы терапевтические, связанные с этими программи(если програмы из комплекса перетаскивались в терапевтический комплекс напрямую) 
            
             complexDAO.destroy(complex.getId());
             removeString(description);
             removeString(name);
            
             
        } catch (Exception e) 
        {
            Log.logger.error("",e);
            throw new Exception("Ошибка удаления комплекса",e);
        }
    }
    
    
    public List<Complex> findAllComplexBySection(Section section)
    {
        return complexDAO.findAllComplexBySection(section);
    }

    /**
     * Заполнить поля строк локализованных.
     * @param section
     */
    public void initStringsComplex(Complex section)
    {
        section.setNameString(getSmartString(section.getName()));
        section.setDescriptionString(getSmartString(section.getDescription()));

    }

    public void initStringsComplex(List<Complex> sections)
    {
        sections.stream().forEach(section ->
        {


                section.setNameString(getSmartString(section.getName()));
                section.setDescriptionString(getSmartString(section.getDescription()));

        });

    }



    /**
     * Заполнить поля строк локализованных.
     * @param complex
     */
    public void initStringsComplex(Complex complex,Language l)
    {

        String name=getString(complex.getName(),l);
        String d=getString(complex.getDescription(),l);

        complex.setNameString(name==null?"":name);
        complex.setDescriptionString(d==null?"":d);

    }
    public void initStringsComplex(Complex complex,Language l, boolean defLang)
    {

        String name=getString(complex.getName(),l);
        String d=getString(complex.getDescription(),l);

        if(!defLang){
            complex.setNameString(name==null?"":name);
            complex.setDescriptionString(d==null?"":d);
        }else {
            complex.setNameString(name == null ? getString(complex.getName(),getDefaultLanguage()) : name);
            complex.setDescriptionString(d == null ? getString(complex.getDescription(),getDefaultLanguage()) : d);
        }

    }

    public void initStringsComplex(List<Complex> cs,Language l)
    {
       for (Complex c : cs)
        {

            String name=getString(c.getName(),l);
            String d=getString(c.getDescription(),l);

            c.setNameString(name==null?"":name);
            c.setDescriptionString(d==null?"":d);

        }

    }
    public void initStringsComplex(List<Complex> cs,Language l, boolean defLang)
    {
        for (Complex c : cs)
        {

            String name=getString(c.getName(),l);
            String d=getString(c.getDescription(),l);
            if(!defLang){
                c.setNameString(name==null?"":name);
                c.setDescriptionString(d==null?"":d);
            }else {
                c.setNameString(name == null ? getString(c.getName(),getDefaultLanguage()) : name);
                c.setDescriptionString(d == null ? getString(c.getDescription(),getDefaultLanguage()) : d);
            }


        }

    }



    public void updateComplex(Complex complex) throws Exception{complexDAO.edit(complex);}
    
     /******************************************/
     /************** программы **************/
    public Program createProgram(String name,String description,String frequencies,Complex parent,boolean isOwnerSystem,Language lang) throws Exception
    {
        Program program=new Program();
        program.setComplex(parent);
        program.setFrequencies(frequencies);
        program.setOwnerSystem(isOwnerSystem);
        program.setSection(null);
        program.setUuid(UUID.randomUUID().toString());


         Strings sName=null;
        Strings sDescr=null;
        
        try
        {
         sName= createString(name, lang);
         sDescr= createString(description, lang);
         program.setDescription(sDescr);
         program.setName(sName);
         programDAO.create(program);

            program.setPosition(program.getId());
            programDAO.edit(program);
        }
        catch(Exception ex)
        {
            Log.logger.error("",ex);
            program=null;
            throw new Exception("Ошибка создания программы",ex);
        }
        return program;
        
    }
    public Program createProgram(String name,String description,String frequencies,Section parent,boolean isOwnerSystem,Language lang) throws Exception
    {
        Program program=new Program();
        program.setComplex(null);
        program.setFrequencies(frequencies);
        program.setOwnerSystem(isOwnerSystem);
        program.setSection(parent);
        program.setUuid(UUID.randomUUID().toString());
         Strings sName=null;
        Strings sDescr=null;


        
        try
        {
         sName= createString(name, lang);
         sDescr= createString(description, lang);
         program.setDescription(sDescr);
         program.setName(sName);
         programDAO.create(program);
            program.setPosition(program.getId());
            programDAO.edit(program);
        }
        catch(Exception ex)
        {
            Log.logger.error("",ex);
            program=null;
            throw new Exception("Ошибка создания программы",ex);
        }
        return program;
        
    }

    /**
     * Заполнить поля строк локализованных.
     * @param section
     */
    public void initStringsProgram(Program section)
    {
        section.setNameString(getSmartString(section.getName()));
        section.setDescriptionString(getSmartString(section.getDescription()));

    }
    /**
     * Заполнить поля строк локализованных.
     * @param p
     */
    public void initStringsProgram(Program p,Language lang)
    {
        initStringsProgram(p,lang,false);

    }
    public void initStringsProgram(Program p,Language lang,boolean defLang)
    {
        String name = getString(p.getName(), lang);
        String d = getString(p.getDescription(), lang);

        if(!defLang){
            p.setNameString(name==null?"":name);
            p.setDescriptionString(d==null?"":d);
        }else {
            p.setNameString(name == null ? getString(p.getName(),getDefaultLanguage()) : name);
            p.setDescriptionString(d == null ? getString(p.getDescription(),getDefaultLanguage()) : d);
        }

    }




    public void initStringsProgram(List<Program> programms,Language lang)
    {
        initStringsProgram(programms,lang,false);

    }
    public void initStringsProgram(List<Program> programms,Language lang,boolean defLang)
    {
        String name =null;
        String d=null;
        for(Program p : programms)
        {
            name =null;
            d=null;

            name = getString(p.getName(), lang);
            d = getString(p.getDescription(), lang);

            if(!defLang){
                p.setNameString(name==null?"":name);
                p.setDescriptionString(d==null?"":d);
            }else {
                p.setNameString(name == null ? getString(p.getName(),getDefaultLanguage()) : name);
                p.setDescriptionString(d == null ? getString(p.getDescription(),getDefaultLanguage()) : d);
            }


        }

    }


    public void initStringsProgram(List<Program> sections)
    {
        sections.stream().forEach(section ->
        {


                section.setNameString(getSmartString(section.getName()));
                section.setDescriptionString(getSmartString(section.getDescription()));

        });

    }
    
    public void removeProgram(Program program) throws Exception
    {
       
        
       
        Strings description = program.getDescription();
        Strings name = program.getName();

        try {  
            

            
             programDAO.destroy(program.getId());
             removeString(description);
             removeString(name);
            
             
        } catch (Exception e) 
        {
            Log.logger.error("",e);
            throw new Exception("Ошибка удаления програмы",e);
        }
       
        
    }
    
    public List<Program> findAllProgramByComplex(Complex complex)
    {
     return   programDAO.findAllProgramByComplex(complex);
    }

    /**
     *
     * @param section
     * @return
     */
     public List<Program> findAllProgramBySection(Section section)
    {
        return   programDAO.findAllProgramBySection(section);
    }
     public void updateProgram(Program program) throws Exception{programDAO.edit(program);}


    /**
     * Установит необходимость генерации для всех программ
     */
    public void setNeedGenerateAllTherapyProgramm(){
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("Update TherapyProgram set changed=true");
        try{
            em.getTransaction().begin();
            query.executeUpdate();
            em.getTransaction().commit();
        }finally {
            if(em!=null) em.close();
        }




    }
     
    /******************************************/
     
     /************ Строки ***/
     
    /**
     * Создает строку и локализация с языком lang
     * @param content
     * @param lang
     * @return
     * @throws Exception 
     */
     private Strings createString(String content,Language lang) throws Exception 
     {
         
         Strings str=new Strings();
         LocalizedString lstr=new LocalizedString();  
         
        try {
            this.stringsDAO.create(str);
            lstr.setLanguage(lang);
            lstr.setContent(content);
            lstr.setStrings(str);
            localizedStringDAO.create(lstr);
            
        } catch (Exception ex) {
            Log.logger.error("",ex);
            str=null;
            lstr=null;
            throw new Exception("Ошибка создания локализованной строки", ex);
        }
         return str;
     }
     
     /**
      * Добавить локализованную строку к ресурсу . Выкинет исключение если уже есть строка с указанным языком
      * @param str ресурс 
      * @param content
      * @param lang
      * @return 
      * @throws Exception  
      */
        public  LocalizedString addString(Strings str,String content,Language lang) throws Exception 
        {
            int count = localizedStringDAO.countByStrings(str,lang);
            if(count!=0) {throw new Exception("Строка с таким языком уже есть");}

            LocalizedString lstr=new LocalizedString();  

           try {

               lstr.setLanguage(lang);
               lstr.setContent(content);
               lstr.setStrings(str);
               localizedStringDAO.create(lstr);

           } catch (Exception ex) {
               Log.logger.error("",ex);

               lstr=null;
               throw new Exception("Ошибка создания локализованной строки", ex);
           }
            return lstr;
        }
     
     /**
      * Возвратит строку пользователя или текущей локали или дефолтную в прорядке их отсутствия.
      * @param lStr
      * @return 
      */
     public String getSmartString(Strings lStr)
     {
        String str=null;
        str = getString( lStr,getUserLanguage());
        if(str!=null) return str;
        
        str = getString( lStr,getProgramLanguage());
        if(str!=null) return str;
        
        str = getString( lStr,getDefaultLanguage());
        if(str!=null) return str;
        
         return str;
     }



    /**
     * Вернет абривиатуру языка, которыю предпочтет getSmartString
     * @param lStr
     * @return
     */
    public String getSmartLang(Strings lStr)
    {
        String str=null;
        str = getString( lStr,getUserLanguage());
        if(str!=null) return getUserLanguage().getAbbr();

        str = getString( lStr,getProgramLanguage());
        if(str!=null) return getProgramLanguage().getAbbr();

        str = getString( lStr,getDefaultLanguage());
        if(str!=null) return getDefaultLanguage().getAbbr();

        return str;


    }
     /**
      * Возвратит строку пользователя или текущей локали или дефолтную в прорядке их отсутствия.
      * @param lStr
      * @return 
      */
     public LocalizedString getSmartLocalString(Strings lStr)
     {
        LocalizedString str=null;
        str = getLocalString( lStr,getUserLanguage());
        if(str!=null) return str;
        
        str = getLocalString( lStr,getProgramLanguage());
        if(str!=null) return str;
        
        str = getLocalString( lStr,getDefaultLanguage());
        if(str!=null) return str;
        
         return str;
     }
     /**
      * получает строку локализованную по классу агрегатору
      * @param lStr
      * @param lang
      * @return  nuul если нет такой строки
      */
     public String getString(Strings lStr,Language lang)
     {
         
        List<LocalizedString> findByStrings = localizedStringDAO.findByStrings(lStr, lang);
        if(findByStrings.isEmpty()) return null;
        
         return findByStrings.get(0).getContent();
          
     }

    /**
     * получает строку локализованную по классу агрегатору или строку для языка по умолчанию
     * @param lStr
     * @param lang
     * @return строка на языке по умолчанию если нет искомой или если нет его, то пользовательская
     */
    public String getString2(Strings lStr,Language lang)
    {
        List<LocalizedString> findByStrings = localizedStringDAO.findByStrings(lStr, lang);
        if(findByStrings.isEmpty()) {
            findByStrings = localizedStringDAO.findByStrings(lStr, getDefaultLanguage());
            if(findByStrings.isEmpty()){
                findByStrings = localizedStringDAO.findByStrings(lStr, getUserLanguage());
                if(findByStrings.isEmpty()) return "";
                else return getString(lStr,getUserLanguage());
            }else return getString(lStr,getDefaultLanguage());

        }

        return findByStrings.get(0).getContent();
    }
     /**
      *Сущность  локализованной строки
      * @param lStr
      * @param lang
      * @return 
      */
      public LocalizedString getLocalString(Strings lStr,Language lang)
     {
         
        List<LocalizedString> findByStrings = localizedStringDAO.findByStrings(lStr, lang);
        if(findByStrings.isEmpty()) return null;
        
         return findByStrings.get(0);
          
     }
     /**
      * Удаляет строку и все локализации
      * @param str 
      */
     public void removeString(Strings str) throws Exception
     {
        //удалим LocalizedStrings и потом саму строку
        List<LocalizedString> strs = localizedStringDAO.findByStrings(str);
         try {
              for(LocalizedString itm: strs) localizedStringDAO.destroy(itm.getId());              
              stringsDAO.destroy(str.getId());
              
         } catch (Exception ex) 
         {
             Log.logger.error("",ex);
            throw new Exception("Ошибка удаления строки  и локализаций",ex);
         }
       
     }
     
      
     /**
      * Добавит локализованную строку, если LocalizedString отсутствует, она будет создана
      *
      */
     public void updateLocalString(LocalizedString lStr) throws Exception
     {
         
         localizedStringDAO.edit(lStr);
     
     }
     
     //нужно решить вопрос с инициализацией загруженных строк. 
     //Можно написать StringConverter к Strings тогда можно его прямо в CellFactory запихивать



     
      /******************************************/

    /**
     * Переведет на заданный язык. Результат будет в name и description(если было на языке юзера, то так и останется)
     * В oname будет на языке программы или если нет на языке по умолчанию или если нет на языке юзера или просто пустая строка
     * @param tp
     * @param toLang
     * @return вернет измененный объект
     */
      public TherapyProgram translate(TherapyProgram tp, Language toLang) throws Exception {
          if(tp.getSrcUUID().isEmpty()) return tp;

          String name = tp.getName();
          String description = tp.getDescription();
          String oname = tp.getOname();

          Program srcProgram = getProgram(tp.getSrcUUID());
          if(srcProgram==null) return tp;

          String newName = getString2(srcProgram.getName(),toLang);
          String newDesc = getString2(srcProgram.getDescription(),toLang);
          String newOName = getString2(srcProgram.getName(),getProgramLanguage());

          if(newName.equals(newOName))tp.setOname("");
          else tp.setOname(newOName);
          if(!newName.isEmpty())tp.setName(newName);
          tp.setDescription(newDesc);

          try {
              updateTherapyProgram(tp);
          } catch (Exception e) {
              tp.setName(name);
              tp.setDescription(description);
              tp.setOname(oname);
              throw e;
          }

          return tp;
      }

    /**
     * Переведет на заданный язык - комплекс и все его программы!! Результат будет в name и description(если было на языке юзера, то так и останется)
     * В oname будет на языке программы или если нет на языке по умолчанию или если нет на языке юзера или просто пустая строка
     * @param tc
     * @param toLang
     * @return вернет измененный объект
     */
    public TherapyComplex translate(TherapyComplex tc, Language toLang) throws Exception {
        if(tc.getSrcUUID().isEmpty()) {
            //комплекс может содержать программы, которые можно перевести
            for (TherapyProgram tp : findTherapyPrograms(tc)) {
                translate(tp,toLang);
            }
            return tc;
        }

        String name = tc.getName();
        String description = tc.getDescription();
        String oname = tc.getOname();

        Complex srComplex = getComplex(tc.getSrcUUID());
        if(srComplex==null) {
            //комплекс может содержать программы, которые можно перевести
            for (TherapyProgram tp : findTherapyPrograms(tc)) {
                translate(tp,toLang);
            }
            return tc;
        }

        String newName = getString2(srComplex.getName(),toLang);
        String newDesc = getString2(srComplex.getDescription(),toLang);
        String newOName = getString2(srComplex.getName(),getProgramLanguage());

        if(newName.equals(newOName))tc.setOname("");
        else tc.setOname(newOName);
        if(!newName.isEmpty())tc.setName(newName);
        tc.setDescription(newDesc);

        try {
            updateTherapyComplex(tc);
            for (TherapyProgram tp : findTherapyPrograms(tc)) {
                translate(tp,toLang);
            }

        } catch (Exception e) {
            tc.setName(name);
            tc.setDescription(description);
            tc.setOname(oname);
            throw e;
        }

        for (TherapyProgram tp : findTherapyPrograms(tc)) {
            translate(tp,toLang);
        }


        return tc;
    }

    public void translate(Profile profile, Language toLang) throws Exception{
        for (TherapyComplex complex : findTherapyComplexes(profile)) {
            translate(complex,toLang);
        }
    }
    /************ Языки ***/
     public Language createLanguage(String abbr,String name,boolean avaliable)
     {

         Language langU=null;
         try {
             langU= updateLanguage(abbr,name,avaliable);
         } catch (Exception e) {
             Log.logger.error("",e);
             return langU;
         }
         if(langU!=null)return langU;


         Language lang = new Language();
         
         lang.setAbbr(abbr);
         lang.setName(name);
         lang.setAvaliable(avaliable);
        try {
            this.languageDAO.create(lang);
        } catch (Exception ex) {
            Log.logger.error("",ex);
        }
         return lang;
     }

    public Language createLanguage(String abbr,String name)
    {
        Language langU=null;
        try {
            langU= updateLanguage(abbr,name,false);
        } catch (Exception e) {
            Log.logger.error("",e);
            return langU;
        }
        if(langU!=null)return langU;

        Language lang = new Language();

        lang.setAbbr(abbr);
        lang.setName(name);
        lang.setAvaliable(false);
        try {
            this.languageDAO.create(lang);
        } catch (Exception ex) {
            Log.logger.error("",ex);
        }
        return lang;
    }
     public List<Language> findAllLanguage(){return this.languageDAO.findLanguageEntities();}

    /**
     * Список языков для которых есть переводы. кроме юзерского
     * @return
     */
    public List<Language> findAvaliableLangs(){
        Query query = emf.createEntityManager().createQuery("Select t from Language t where t.avaliable=:tc and t.abbr<>'user'");
        query.setParameter("tc", true);
        return query.getResultList();

    }


    public boolean hasLang(String abbr){
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("Select count(l) from   Language l WHERE l.abbr=:abbr");
        query.setParameter("abbr", abbr);
        long cnt = (Long) query.getSingleResult();
        if(em!=null) em.close();

        return cnt>0;
    }
    private Language findLanguage(String abbr){
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("Select l from   Language l WHERE l.abbr=:abbr");
            query.setParameter("abbr", abbr);
            return  (Language) query.getSingleResult();
        }finally {
            if(em!=null) em.close();
        }


    }

    /**
     * Обновит язык если изменено имя или доступность. Если нет языка то вернет null, если есть то вернет обновленный или найденный
     * @param abbr
     * @param name
     * @param avaliable
     * @return
     * @throws Exception
     */
    private  Language updateLanguage(String abbr,String name,boolean avaliable) throws Exception {
        Language language = findLanguage(abbr);
        if(language==null) return null;
        if(!name.equals(language.getName()) || avaliable!=language.isAvaliable())
        {
            language.setName(name);
            language.setAvaliable(avaliable);

                languageDAO.edit(language);

        }


        return language;
    }



     /******************************************/
     
     /************ Терапия ***/
     
    //основные действия через профиль.
     
     public List<TherapyComplex> findTherapyComplexes(Profile profile)
     {
         
        Query query = emf.createEntityManager().createQuery("Select t from TherapyComplex t where t.profile=:tc order by t.position asc");
        query.setParameter("tc", profile);
         return query.getResultList();
     }

    /**
     * Список тер. программ сортированных по позиции
     * @param therapyComplex
     * @return
     */
      public List<TherapyProgram> findTherapyPrograms(TherapyComplex therapyComplex)
     {
         Query query = emf.createEntityManager().createQuery("Select t from TherapyProgram t where t.therapyComplex = :tc order by t.position");
         query.setParameter("tc", therapyComplex);
         return query.getResultList();
     }

    /**
     * Список тер. программ сортированных по позиции
     * @param idTherapyComplex
     * @return
     */
    public List<TherapyProgram> findTherapyPrograms(Long idTherapyComplex)
    {
        Query query = emf.createEntityManager().createQuery("Select t from TherapyProgram t where t.therapyComplex.id = :tc order by t.position");
        query.setParameter("tc", idTherapyComplex);
        return query.getResultList();
    }

    public List<TherapyProgram> findAllTherapyPrograms()
    {
        Query query = emf.createEntityManager().createQuery("Select t from TherapyProgram t ");

        return query.getResultList();
    }
      /******************************************/
      /************ Терапия Комплексы ***/




    /**
     * Создаст терапевтический комплекс, с именем name
     * @param profile профиль
     * @param name имя тер.комплекса
     * @param description описание
     * @param timeForFreq время на частоту
     *
     * @param bundlesLength колличество частот в пачке для мультичастотного режима(<2 пачки не образуются)
     * @return
     * @throws Exception
     */
    public TherapyComplex createTherapyComplex(String srcuuid,Profile profile,String name,String description,int timeForFreq, int bundlesLength) throws Exception
    {
        TherapyComplex tc=new TherapyComplex();
        tc.setDescription(description);
        tc.setProfile(profile);
        tc.setTimeForFrequency(timeForFreq);
        tc.setName(name);

        tc.setBundlesLength(bundlesLength);
        tc.setOname("");

        tc.setSrcUUID(srcuuid);

        try
        {
            therapyComplexDAO.create(tc);
        }catch(Exception e){
            Log.logger.error("",e);
            tc=null;
            throw new Exception("Ошибка создания терапевтического комплекса",e);
        }

        if(tc!=null){
            tc.setPosition(tc.getId());
            updateTherapyComplex(tc);
        }

        Platform.runLater(()->onInProfileChanged.change(profile.getId()));
        return tc;
    }

    /**
     *  Создаст терапевтический комплекс из комплекса complex
     * @param profile профиль
     * @param complex комплекс из которого создается тер.комплекс
     * @param timeForFreq время на частоту
     *
     * @param bundlesLength колличество частот в пачке для мультичастотного режима(<2 пачки не образуются)
     * @return
     * @throws Exception
     */
      public TherapyComplex createTherapyComplex(Profile profile,Complex complex,int timeForFreq, int bundlesLength) throws Exception
      {

          TherapyComplex tc=new TherapyComplex();
          tc.setProfile(profile);
          tc.setTimeForFrequency(timeForFreq);
          tc.setName(getSmartString(complex.getName()));

          tc.setBundlesLength(bundlesLength);
          //tc.setChanged(true);//только изменения в существующих комплексах должно приводить к регенерации
          tc.setSrcUUID(complex.getUuid());

          List<Program> findAllProgramByComplex = findAllProgramByComplex(complex);
          initStringsProgram(findAllProgramByComplex);

          initStringsComplex(complex);
          tc.setDescription(complex.getDescriptionString());
          tc.setName(complex.getNameString());

        try
        {
         therapyComplexDAO.create(tc);//создадим компелекс
         for(Program itm: findAllProgramByComplex) createTherapyProgram(itm.getUuid(),tc, itm.getNameString(),itm.getDescriptionString(),itm.getFrequencies());//создадим програмы из комплекса

        }catch(Exception e){Log.logger.error("",e);tc=null;throw new Exception("Ошибка создания терапевтического комплекса",e); }
          if(tc!=null){
              tc.setPosition(tc.getId());
              updateTherapyComplex(tc);
          }

          Platform.runLater(()->onInProfileChanged.change(profile.getId()));
        return tc;
      }

    /**
     *  Создаст терапевтический комплекс из комплекса complex, с учетом языка вставки. Также создаст соответствующие программы
     * @param profile профиль
     * @param complex комплекс из которого создается тер.комплекс
     * @param timeForFreq время на частоту
     *
     * @param bundlesLength колличество частот в пачке для мультичастотного режима(<2 пачки не образуются)
     *                      @param  insertComplexLang язык вставки комплекса
     * @return
     * @throws Exception
     */
    public TherapyComplex createTherapyComplex(Profile profile,Complex complex,int timeForFreq, int bundlesLength, String insertComplexLang) throws Exception
    {

        TherapyComplex tc=new TherapyComplex();
        tc.setProfile(profile);
        tc.setTimeForFrequency(timeForFreq);
        tc.setName(getSmartString(complex.getName()));

        tc.setBundlesLength(bundlesLength);
        //tc.setChanged(true);//только изменения в существующих комплексах должно приводить к регенерации
        tc.setSrcUUID(complex.getUuid());


        initStringsComplex(complex);
        List<Program> findAllProgramByComplex = findAllProgramByComplex(complex);
        initStringsProgram(findAllProgramByComplex);

        String il=insertComplexLang;
        String lp=getProgramLanguage().getAbbr();

        String name="";
        String oname="";
        String descr="";

        Map<Long,String> onameP=new HashMap<>();

        if(il.equals(lp))
        {
            name=complex.getNameString();
            descr=complex.getDescriptionString();

        }else {
            //вставим имя на языке вставки.
            if(complex.isOwnerSystem())oname=complex.getNameString();
            name = getString2(complex.getName(),getLanguage(il));
            descr=getString2(complex.getDescription(),getLanguage(il));


            String nameP="";

            String descrP="";
            //заменим тоже самое для программ
            for(Program itm: findAllProgramByComplex)
            {
                onameP.put(itm.getId(),itm.getNameString());
                nameP = getString2(itm.getName(),getLanguage(il));
                descrP=getString2(itm.getDescription(),getLanguage(il));
                itm.setNameString(nameP);
                itm.setDescriptionString(descrP);

            }


        }

        tc.setDescription(descr);
        tc.setName(name);
        tc.setOname(oname);






        try
        {
            therapyComplexDAO.create(tc);//создадим компелекс
            if(il.equals(lp))
            {
                for(Program itm: findAllProgramByComplex){
                    createTherapyProgram(itm.getUuid(),tc,
                            itm.getNameString(),
                            itm.getDescriptionString(),
                            itm.getFrequencies());//создадим програмы из комплекса
                }
            }else {
                for(Program itm: findAllProgramByComplex){
                    createTherapyProgram(itm.getUuid(),tc,
                            itm.getNameString(),
                            itm.getDescriptionString(),
                            itm.getFrequencies(),
                            onameP.get(itm.getId()));
                }
            }


        }catch(Exception e){
            Log.logger.error("",e);
            tc=null;
            throw new Exception("Ошибка создания терапевтического комплекса",e); }
        if(tc!=null){
            tc.setPosition(tc.getId());
            updateTherapyComplex(tc);
        }

        Platform.runLater(()->onInProfileChanged.change(profile.getId()));
        return tc;
    }



    public List<Program> findAllProgram()
    {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT p FROM Program p");
        return query.getResultList();
    }
    public List<Complex> findAllComplex()
    {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT p FROM Complex p");
        return query.getResultList();
    }
    public List<Section> findAllSection()
    {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT p FROM Section p");
        return query.getResultList();
    }


    public int getTimeTherapyComplex(TherapyComplex th)
    {

        int res=0;
        Query query=null;
        List<String> results=null;
        //если все частоты мульти, подсчитаем все без мп3 и умножим на время частоты, mp3 считает ся ниже отдельно
        /*
        if(th.isMulltyFreq())
        {

            //посчитаем пачки частот для всех программ комплекса
            int freqBundlesCount=0;//сколько пачек получем из частот программ



            if(   th.getBundlesLength()>=2){


                for (TherapyProgram tp : findTherapyPrograms(th))
                {
                    int numFreqsForce = tp.getNumFreqsForce();
                    freqBundlesCount+=(int)Math.ceil((float)numFreqsForce/(float)th.getBundlesLength());

                }


                res = (int)(th.getTimeForFrequency() * freqBundlesCount);
            }else {
                res = (int)(th.getTimeForFrequency() * countTherapyPrograms(th,false));
            }


        }
        else {
             query = emf.createEntityManager().createQuery("Select t.frequencies from TherapyProgram t where t.therapyComplex = :tc and t.mp3 <> true");
            query.setParameter("tc", th);
            results = query.getResultList();
            res = results.stream().mapToInt(value -> value.split(";").length).sum() * th.getTimeForFrequency();
        }
*/

       //теперь все комплексы мультичастотные

            //посчитаем пачки частот для всех программ комплекса
            int freqBundlesCount=0;//сколько пачек получем из частот программ



            if(   th.getBundlesLength()>=2){


                for (TherapyProgram tp : findTherapyPrograms(th))
                {
                    if(!tp.isMultyFreq()){
                        res+=tp.getNumFreqs()*th.getTimeForFrequency();
                    }else {
                    int numFreqsForce = tp.getNumFreqsForce();
                    freqBundlesCount+=(int)Math.ceil((float)numFreqsForce/(float)th.getBundlesLength());
                    }
                }


                res += (int)(th.getTimeForFrequency() * freqBundlesCount);
            }else {

                for (TherapyProgram tp : findTherapyPrograms(th))
                {
                    if(!tp.isMultyFreq()){
                        res+=tp.getNumFreqs()*th.getTimeForFrequency();
                    }else {
                       res+=th.getTimeForFrequency();
                    }
                }


            }
             /*{
            query = emf.createEntityManager().createQuery("Select t.frequencies from TherapyProgram t where t.therapyComplex = :tc and t.mp3 <> true");
            query.setParameter("tc", th);
            results = query.getResultList();
            res = results.stream().mapToInt(value -> value.split(";").length).sum() * th.getTimeForFrequency();
        }
*/








        //получим программы комплекса с mp3 файлами
         query = emf.createEntityManager().createQuery("Select t.frequencies from TherapyProgram t where t.therapyComplex = :tc and t.mp3 = true");
         query.setParameter("tc", th);
         results = query.getResultList();

        Mp3File mp3file = null;
        for (String s : results)
        {

            try {
                mp3file = new Mp3File(s);
            } catch (Exception e)
            {

                mp3file=null;
            }
            if(mp3file!=null) res+=mp3file.getLengthInSeconds();
        }

       return  res;


    }


    /**
     * Время профиля
     * @param p
     * @return
     */
    public int getTimeProfile(Profile p)
    {

       return findAllTherapyComplexByProfile(p).stream().mapToInt(value -> getTimeTherapyComplex(value)).sum();
    }


    public long countTherapyPrograms(Profile p)
    {

        Query query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex.profile.id=:p");
        query.setParameter("p", p.getId());
        return (Long)query.getSingleResult();
    }


    /**
     * Колличество программ,withMp3 считать только mp3 программы или только простые
     * @param tc
     * @param withMp3 withMp3 считать только mp3 программы или только простые
     * @return
     */
    public long countTherapyPrograms(TherapyComplex tc,boolean withMp3)
    {
        Query query=null;
        if(!withMp3) query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex=:tc  and s.mp3 <> true");
        else  query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex=:tc and s.mp3 =true");
        query.setParameter("tc", tc);
        return (Long)query.getSingleResult();
    }

    /**
     * Колличество программа в т.комплексе, withMp3 считать только mp3 программы или только простые
     * @param idTc id т.комплеса
     *  @param withMp3 считать только mp3 программы или только простые
     * @return
     */
    public long countTherapyPrograms(long idTc,boolean withMp3)
    {
        Query query=null;
        if(!withMp3) query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex.id=:tc  and s.mp3 <> true");
        else query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex.id=:tc  and s.mp3 =true");
        query.setParameter("tc", idTc);
        return (Long)query.getSingleResult();
    }
    /**
     * Колличество программ
     * @param tc
     *
     * @return
     */
    public long countTherapyPrograms(TherapyComplex tc)
    {
        Query query=null;
       query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex=:tc");
        query.setParameter("tc", tc);
        return (Long)query.getSingleResult();
    }

    /**
     * Колличество программа в т.комплексе
     * @param idTc id т.комплеса
     *
     * @return
     */
    public long countTherapyPrograms(long idTc)
    {
        Query query=null;
        query = emf.createEntityManager().createQuery("Select count(s) from TherapyProgram s WHERE s.therapyComplex.id=:tc");
        query.setParameter("tc", idTc);
        return (Long)query.getSingleResult();
    }



    public TherapyComplex findTherapyComplex(Long id)
    {
      return   this.therapyComplexDAO.findTherapyComplex(id);
    }
    public Complex findComplex(String uuid)
    {
        Query query = emf.createEntityManager().createQuery("Select t from Complex t where t.uuid = :p").setMaxResults(1);
        query.setParameter("p", uuid);
        List<Complex> resultList = query.getResultList();
        if(resultList.size()==0) return null;
        else    return resultList.get(0);
    }
    public Program findProgram(String uuid)
    {
        Query query = emf.createEntityManager().createQuery("Select t from Program t where t.uuid = :p").setMaxResults(1);
        query.setParameter("p", uuid);
        List<Program> resultList = query.getResultList();
        if(resultList.size()==0) return null;
        else    return resultList.get(0);
    }
    /**
       * Удалит терапевтический комплекс и все терапевтические программы в нем
       * @param th
       * @throws Exception 
       */
      public void removeTherapyComplex(TherapyComplex th) throws Exception
      {
        
        List<TherapyProgram> findTherapyProgram = findTherapyPrograms(th);
          
        try {
            
          for(TherapyProgram itm:findTherapyProgram) removeTherapyProgram(itm);
          
            therapyComplexDAO.destroy(th.getId());
            Platform.runLater(()->onInProfileChanged.change(th.getProfile().getId()));
        } catch (Exception ex) {
            Log.logger.error("",ex);
            throw new Exception("Ошибка удаления терапевтического комплекса",ex);
        }


          
          
      }
      public void updateTherapyComplex(TherapyComplex cm) throws Exception
      {
          therapyComplexDAO.edit(cm);
          Platform.runLater(()->onInProfileChanged.change(cm.getProfile().getId()));
      }

       public List<TherapyComplex> findAllTherapyComplexByProfile(Profile profile)
       {
           Query query = emf.createEntityManager().createQuery("Select t from TherapyComplex t where t.profile = :p order by t.position asc");
           query.setParameter("p", profile);
          return query.getResultList();

       }

    public List<TherapyComplex> findAllTherapyComplexes()
    {
        Query query = emf.createEntityManager().createQuery("Select t from TherapyComplex t");

        return query.getResultList();

    }

    /**
     *
     * Создаст копию комплекса в указанном профиле
     * @param profile
     * @param tc
     * @multyFreq изменяет этот параметр на установленный
     */
    public TherapyComplex copyTherapyComplexToProfile(Profile profile, TherapyComplex tc) throws Exception {
        TherapyComplex therapyComplex=null;
        try {
             therapyComplex = createTherapyComplex(tc.getSrcUUID(),profile,
                    tc.getName(),
                    tc.getDescription(),
                    tc.getTimeForFrequency(),
                    tc.getBundlesLength());
            therapyComplex.setSrcUUID(tc.getSrcUUID());
            therapyComplex.setOname(tc.getOname());
            updateTherapyComplex(therapyComplex);

            for (TherapyProgram therapyProgram : findTherapyPrograms(tc)) {

                copyTherapyProgramToComplex(therapyComplex,therapyProgram);
            }


        } catch (Exception e) {
            Log.logger.error("",e);
            tc=null;
            throw new Exception("Ошибка копирования терапевтического комплекса",e);
        }

        Platform.runLater(()->onInProfileChanged.change(profile.getId()));
        return therapyComplex;

    }



       /******************************************/
       /************ Терапия программы ***/


       public TherapyProgram copyTherapyProgramToComplex(TherapyComplex complex, TherapyProgram tp) throws Exception{
           TherapyProgram therapyProgram=null;
           try {
               therapyProgram =createTherapyProgram(tp.getSrcUUID(), complex,tp.getName(),tp.getDescription(),tp.getFrequencies());
               therapyProgram.setPosition(therapyProgram.getId());
               therapyProgram.setOname(tp.getOname());
               therapyProgram.setMp3(tp.isMp3());
               therapyProgram.setMultyFreq(tp.isMultyFreq());

               updateTherapyProgram(therapyProgram);


           } catch (Exception e) {
               Log.logger.error("",e);

               throw new Exception("Ошибка копирования терапевтическогй программы",e);
           }

           return therapyProgram;
       }

       public TherapyProgram copyTherapyProgramToComplex(TherapyComplex complex, TherapyProgram tp,long position) throws Exception {
           TherapyProgram therapyProgram=null;
           try {
               therapyProgram =createTherapyProgram(tp.getSrcUUID(), complex,tp.getName(),tp.getDescription(),tp.getFrequencies());
               therapyProgram.setPosition(position);
               therapyProgram.setOname(tp.getOname());
               therapyProgram.setMp3(tp.isMp3());
               therapyProgram.setMultyFreq(tp.isMultyFreq());

               updateTherapyProgram(therapyProgram);



           } catch (Exception e) {
               Log.logger.error("",e);

               throw new Exception("Ошибка копирования терапевтическогй программы",e);
           }


           return therapyProgram;

       }
      
      public TherapyProgram createTherapyProgram(String srcUUID, TherapyComplex therapyComplex, String name,String description, String freqs) throws Exception
      {
         return createTherapyProgram(srcUUID, therapyComplex,  name, description,  freqs, true);
      }

    public TherapyProgram createTherapyProgram(String srcUUID,TherapyComplex therapyComplex, String name,String description, String freqs,boolean multy) throws Exception
    {
        TherapyProgram tc=new TherapyProgram();
        tc.setTherapyComplex(therapyComplex);
        tc.setName(name);
        tc.setDescription(description);
        tc.setFrequencies(freqs);
        tc.setTimeMarker(0);
        tc.setChanged(true);//по умолчанию файлы не генерятся, значит установим этот флажек
        tc.setMp3(false);
        tc.setOname("");
        tc.setMultyFreq(multy);
        tc.setSrcUUID(srcUUID);

        try
        {

            therapyProgramDAO.create(tc);
            tc.setPosition(tc.getId());
            therapyProgramDAO.edit(tc);


        }catch(Exception e)
        {
            Log.logger.error("",e);
            tc=null;
            throw new Exception("Ошибка создания терапевтической программы",e);
        }

        Platform.runLater(()->onInProfileChanged.change(therapyComplex.getProfile().getId()));
        return tc;
    }


    /**
     *
     * @param therapyComplex
     * @param name - имя на языке вставки
     * @param description
     * @param freqs
     * @param oName имя на языке программы
     * @return
     * @throws Exception
     */
    public TherapyProgram createTherapyProgram(String srcUUID,TherapyComplex therapyComplex, String name,String description, String freqs,String oName) throws Exception
    {
       return createTherapyProgram(srcUUID, therapyComplex,  name, description,  freqs, oName,true);
    }

    public TherapyProgram createTherapyProgram(String srcUUID,TherapyComplex therapyComplex, String name,String description, String freqs,String oName,boolean multy) throws Exception
    {
        TherapyProgram tc=new TherapyProgram();
        tc.setTherapyComplex(therapyComplex);
        tc.setName(name);
        tc.setDescription(description);
        tc.setFrequencies(freqs);
        tc.setTimeMarker(0);
        tc.setChanged(true);//по умолчанию файлы не генерятся, значит установим этот флажек
        tc.setMp3(false);
        tc.setOname(oName);
        tc.setMultyFreq(multy);
        tc.setSrcUUID(srcUUID);
        try
        {

            therapyProgramDAO.create(tc);
            tc.setPosition(tc.getId());
            therapyProgramDAO.edit(tc);


        }catch(Exception e)
        {
            Log.logger.error("",e);
            tc=null;
            throw new Exception("Ошибка создания терапевтической программы",e);
        }

        Platform.runLater(()->onInProfileChanged.change(therapyComplex.getProfile().getId()));
        return tc;
    }
    /**
     * Создание программы из файла mp3.
     * @param therapyComplex
     * @param name
     * @param description
     * @param filePath абсолютный путь к файлу в системе
     * @return
     * @throws Exception
     */
    public TherapyProgram createTherapyProgramMp3(TherapyComplex therapyComplex, String name,String description, String filePath) throws Exception
    {
        TherapyProgram tc=new TherapyProgram();
        tc.setTherapyComplex(therapyComplex);
        tc.setName(name);
        tc.setDescription(description);
        tc.setFrequencies(filePath);
        tc.setTimeMarker(0);
        tc.setChanged(false);//по умолчанию файлы mp3 выбиратся из папок, значит они сразу существуют
        tc.setMp3(true);
        tc.setOname("");
        tc.setSrcUUID("");
        try
        {

            therapyProgramDAO.create(tc);
            tc.setPosition(tc.getId());
            therapyProgramDAO.edit(tc);


        }catch(Exception e){ Log.logger.error("",e);tc=null;throw new Exception("Ошибка создания терапевтической программы",e); }

        Platform.runLater(()->onInProfileChanged.change(therapyComplex.getProfile().getId()));
        return tc;
    }

      public void removeTherapyProgram(TherapyProgram th) throws NonexistentEntityException
      {
          therapyProgramDAO.destroy(th.getId());
          Platform.runLater(()->onInProfileChanged.change(th.getTherapyComplex().getProfile().getId()));
      }


      public void updateTherapyProgram(TherapyProgram prg) throws Exception
      {
          therapyProgramDAO.edit(prg);
          Platform.runLater(()->onInProfileChanged.change(prg.getTherapyComplex().getProfile().getId()));
      }



    public TherapyComplex getLastTherapyComplex()
    {

        Query query = emf.createEntityManager().createQuery("Select p From TherapyComplex p   order by p.id desc");
        query.setMaxResults(1);
        return (TherapyComplex)query.getSingleResult();
    }


    public List<TherapyComplex> getLastTherapyComplexes(int n) {
        Query query = this.emf.createEntityManager().createQuery("Select p From TherapyComplex p   order by p.id desc");
        query.setMaxResults(n);
        List resultList = query.getResultList();
        Collections.reverse(resultList);
        return resultList;
    }
    public boolean isNeedGenerateFilesInProfile(Profile profile)
    {

        Query query = emf.createEntityManager().createQuery("Select count(c) From TherapyComplex c WHERE c.profile.id=:profile and c.changed=true");
        query.setParameter("profile", profile.getId());
        long count= (Long)query.getSingleResult();
        if(count!=0) return true;


        //просмотрим есть ли изменения в программах комплексов профиля
        Query query2 = emf.createEntityManager().createQuery("Select count(c) From TherapyProgram c WHERE c.therapyComplex.profile.id=:profile and c.changed=true");
        query2.setParameter("profile", profile.getId());

        long count2= (Long)query2.getSingleResult();
        if(count2!=0) return true;
        else return false;

    }

    /**
     * MP3 программы в профиле
     * @param profile
     * @return
     */
    public List<TherapyProgram> mp3ProgramInProfile(Profile profile)
    {

        //просмотрим есть ли изменения в программах комплексов профиля
        Query query2 = emf.createEntityManager().createQuery("Select c From TherapyProgram c WHERE c.therapyComplex.profile.id=:profile and c.mp3=true order by c.position asc");
        query2.setParameter("profile", profile.getId());
        return query2.getResultList();


    }

    public List<String> mp3ProgramPathsInProfile(Profile profile)
    {

        //просмотрим есть ли изменения в программах комплексов профиля
        Query query2 = emf.createEntityManager().createQuery("Select c.frequencies From TherapyProgram c WHERE c.therapyComplex.profile.id=:profile and c.mp3=true");
        query2.setParameter("profile", profile.getId());
        return query2.getResultList();


    }

    public List<TherapyProgram> mp3ProgramInComplex(TherapyComplex tc) {
        Query query2 = this.emf.createEntityManager().createQuery("Select c From TherapyProgram c WHERE c.therapyComplex.id=:tc and c.mp3=true  order by c.position asc");
        query2.setParameter("tc", tc.getId());
        return query2.getResultList();
    }
    public List<String> mp3ProgramPathsInComplex(TherapyComplex tc) {
        Query query2 = this.emf.createEntityManager().createQuery("Select c.frequencies From TherapyProgram c WHERE c.therapyComplex.id=:tc and c.mp3=true");
        query2.setParameter("tc", tc.getId());
        return query2.getResultList();
    }


    /**
     * Список программ для регенерации файлов, с учетом того что это может быть инициировано therapyComplex.changed=true
     * @param profile
     * @return
     */
    public List<TherapyProgram> findNeedGenerateList(Profile profile)
    {

        Query query = emf.createEntityManager().createQuery("Select tp From TherapyProgram tp WHERE (tp.therapyComplex.changed=true or tp.changed=true) and tp.therapyComplex.profile.id=:profile");
        query.setParameter("profile", profile.getId());
        return query.getResultList();


    }

    public List<TherapyProgram> findNeedGenerateList(TherapyComplex c) {
        Query query = this.emf.createEntityManager().createQuery("Select tp From TherapyProgram tp WHERE (tp.therapyComplex.changed=true or tp.changed=true) and tp.therapyComplex.id=:c");
        query.setParameter("c", c.getId());
        return query.getResultList();
    }


    /**
     * Список комплексов для генерации(те что были помечены)
     * @param profile
     * @return
     */
    public List<TherapyComplex> findNeedGenerateListComplex(Profile profile)
    {

        Query query = emf.createEntityManager().createQuery("Select tp From TherapyComplex tp WHERE tp.changed=true  and tp.profile=:profile");
        query.setParameter("profile", profile);
        return query.getResultList();


    }



    /**
     *  Есть ли еще программы для генерации в комплексе - с пометкой change!
     * @param tcID
     * @return
     */
    public boolean hasNeedGenerateProgramInComplex(long tcID)
    {

        Query query = emf.createEntityManager().createQuery("Select count(c) From TherapyProgram c WHERE c.therapyComplex.id=:tc and c.changed=true");
        query.setParameter("tc", tcID);
        long count= (Long)query.getSingleResult();
        if(count!=0) return true;
        else return false;

    }


    /**
     * Есть ли еще программы для генерации в комплексе - с пометкой change!
     * @param tc
     * @return
     */
    public boolean hasNeedGenerateProgramInComplex(TherapyComplex tc)
    {

        Query query = emf.createEntityManager().createQuery("Select count(c) From TherapyProgram c WHERE c.therapyComplex=:tc and c.changed=true");
        query.setParameter("tc", tc);
        long count= (Long)query.getSingleResult();
        if(count!=0) return true;
        else return false;

    }



    /**
     * Список ID терап. копмлексов профиля
     * @param p
     * @return
     */
    public List<Integer> getAllTherapyComplexID(Profile p)
    {
        Query query = emf.createEntityManager().createQuery("Select c.id From TherapyComplex c WHERE c.profile=:profile order by c.position asc");
        query.setParameter("profile", p);
        return query.getResultList();

    }




    public TherapyProgram getTherapyProgram(long id)
    {

       return this.therapyProgramDAO.findTherapyProgram(id);
    }

    /**
     * Список всех ID тер.программ по профилю
     * @param p
     * @return
     */
    public List<Integer> getAllTherapyProgramID(Profile p)
    {
        Query query = emf.createEntityManager().createQuery("Select c.id From TherapyProgram c WHERE c.therapyComplex.profile=:profile  order by c.id asc");
        query.setParameter("profile", p);
        return query.getResultList();

    }
    /**
     * Список всех  тер.программ по профилю
     * @param p
     * @return
     */
    public List<TherapyProgram> getAllTherapyProgram(Profile p)
    {
        Query query = emf.createEntityManager().createQuery("Select c From TherapyProgram c WHERE c.therapyComplex.profile=:profile  order by c.position asc");
        query.setParameter("profile", p);
        return query.getResultList();

    }


       /******************************************/
        
        
        
        
        public Profile findProfile(long id)
        {
           return this.profileDAO.findProfile(id);
        }


    public Section findSection(String uuid)
    {
        Query query = emf.createEntityManager().createQuery("Select t from Section t where t.uuid = :p").setMaxResults(1);
        query.setParameter("p", uuid);
        return (Section)query.getSingleResult();
    }
         
          public Section findSection(long id)
        {
           return this.sectionDAO.findSection(id);
        }
           public Complex findComplex(long id)
        {
           return this.complexDAO.findComplex(id);
        }
           
           public Program findProgram(long id)
        {
           return this.programDAO.findProgram(id);
        }
           
             public TherapyComplex findTherapyComplexes(long id)
        {
           return this.therapyComplexDAO.findTherapyComplex(id);
        }
             




    public int countProfile(){return profileDAO.getProfileCount();}
    public int countSection(){return sectionDAO.getSectionCount();}
    public int countComplex(){return complexDAO.getComplexCount();}
    public int countProgram(){return programDAO.getProgramCount();}
    public int countTherapyComplex(){return therapyComplexDAO.getTherapyComplexCount();}
    public int countTherapyProgram(){return therapyProgramDAO.getTherapyProgramCount();}
    public int countStrings(){return stringsDAO.getStringsCount();}
    public int countLanguages(){return  languageDAO.getLanguageCount();}


    /**
     * Имеются ли дочерние элемиенты в разделе
     * @param section
     * @return
     */
    public boolean hasChildrenSection(Section section)
    {
        EntityManager em = emf.createEntityManager();
        Query query;
        Long result;
         query = em.createQuery("Select count(s) from Section s WHERE s.parent=:section");
        query.setParameter("section", section);
        result = (Long)query.getSingleResult();
        if(result.intValue()>0) return true;
        else
        {
            query = em.createQuery("Select count(s) from Program s WHERE s.section=:section");
            query.setParameter("section", section);
            result = (Long)query.getSingleResult();
            if(result.intValue()>0) return true;
            else
            {
                query = em.createQuery("Select count(s) from Complex s WHERE s.section=:section");
                query.setParameter("section", section);
                result = (Long)query.getSingleResult();
                if(result.intValue()>0) return true;
            }
        }
        return false;
    }

    public boolean hasChildrenComplex(Complex complex)
    {
        EntityManager em = emf.createEntityManager();
        Query query;
        Long result;
        query = em.createQuery("Select count(s) from Program s WHERE s.complex=:complex");
        query.setParameter("complex", complex);
        result = (Long)query.getSingleResult();
        if(result.intValue()>0) return true;
        else   return false;
    }


      public long countSectionChildren(Section section)
      {
          //TODO многотабличный запрос выполнится в 100 раз дольше чем эта последовательность!!!!!!

          EntityManager em = emf.createEntityManager();
          Query query;
          long count=0;
          query = em.createQuery("Select count(s) from Section s WHERE s.parent=:section");
          query.setParameter("section", section);
          count+= (Long)query.getSingleResult();

          query = em.createQuery("Select count(s) from Complex s WHERE s.section=:section");
          query.setParameter("section", section);
          count+= (Long)query.getSingleResult();

          query = em.createQuery("Select count(s) from Program s WHERE s.section=:section");
          query.setParameter("section", section);
          count+= (Long)query.getSingleResult();

          return count;
      }


        public long countComplexChildren(Complex complex)
        {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("Select count(p) from   Program p WHERE p.complex=:complex");
            query.setParameter("complex", complex);

            return (Long)query.getSingleResult();

        }



    public List<Long> getTherapyComplexFiles(TherapyComplex tc)
    {
        Query query=emf.createEntityManager().createQuery("Select s.id From TherapyProgram s WHERE s.therapyComplex=:tc and s.mp3=false");
        query.setParameter("tc",tc);
        return query.getResultList();
    }

    public List<Long> getProfileFiles(Profile pf)
    {
        Query query=emf.createEntityManager().createQuery("Select s.id From TherapyProgram s WHERE s.therapyComplex.profile=:pf  and s.mp3=false");
        query.setParameter("pf", pf);
        return query.getResultList();
    }

    /**
     * Получает ID тер.комплекса по ID тер.программы
     * @param id
     * @return
     */
    public Long getTherapyProgramIDByTherapyComplexID(long id)
    {
        Query query=emf.createEntityManager().createQuery("Select s.id From TherapyProgram s WHERE s.therapyComplex.id=:id");
        query.setParameter("id", id);
        query.setMaxResults(1);
        return (Long)query.getSingleResult();

    }


    /**
     * Поиск разделов по языку и тексту. Ищится по названи
     * @param text
     * @param lang
     * @return
     */
    public List<Section> searchSectionInAllBase(String text,Language lang)
    {
            //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Section sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON (l=:lang and s.language=l) or (l=:user_lang and s.language=l)");

        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("txt", "%"+text+"%");


        query.setParameter("lang", lang);

        return query.getResultList();

    }

    public List<Program> searchProgramInAllBase(String text,Language lang)
    {
        //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Program sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON (l=:lang and s.language=l) or (l=:user_lang and s.language=l)");

        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("txt", "%"+text+"%");

        query.setParameter("lang", lang);

        return query.getResultList();

    }
    public List<Complex> searchComplexInAllBase(String text,Language lang)
    {
        //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Complex sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON (l=:lang and s.language=l) or (l=:user_lang and s.language=l)");

        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("txt", "%"+text+"%");

        query.setParameter("lang", lang);

        return query.getResultList();

    }


    public List<Section> searchSectionInParent(String text,Language lang,Section parent)
    {
        //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Section sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON  (l=:lang and s.language=l) or (l=:user_lang and s.language=l) where sec.parent.id=:parent");
        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("parent", parent.getId());
        query.setParameter("txt", "%"+text+"%");
        query.setParameter("lang", lang);

        return query.getResultList();

    }

    public List<Program> searchProgramInParent(String text,Language lang,Section parent)
    {
        //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Program sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON   (l=:lang and s.language=l) or (l=:user_lang and s.language=l)  where sec.section.id=:parent");

        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("parent", parent.getId());
        query.setParameter("txt", "%"+text+"%");

        query.setParameter("lang", lang);

        return query.getResultList();

    }
    public List<Complex> searchComplexInParent(String text,Language lang,Section parent)
    {
        //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Complex sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON   (l=:lang and s.language=l) or (l=:user_lang and s.language=l)   where sec.section.id=:parent");

        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("parent", parent.getId());
        query.setParameter("txt", "%"+text+"%");

        query.setParameter("lang", lang);

        return query.getResultList();

    }
    public List<Program> searchProgramInComplex(String text,Language lang,Complex parent)
    {
        //если добавить критерий по описанию то запрос начинает занимать оооочень много времени, проще сделать их последовательными если надо
        Query query=emf.createEntityManager().createQuery("Select sec From Program sec INNER JOIN LocalizedString s ON sec.name=s.strings and (lower(s.content) like lower(:txt)) INNER JOIN Language l ON   (l=:lang and s.language=l) or (l=:user_lang and s.language=l)  where sec.complex.id=:parent");

        query.setParameter("user_lang", getUserLanguage());
        query.setParameter("parent", parent.getId());
        query.setParameter("txt", "%"+text+"%");

        query.setParameter("lang", lang);

        return query.getResultList();

    }




    public Program getProgram(String uuid){

        Query query=emf.createEntityManager().createQuery("Select p From Program p WHERE p.uuid=:uuid");
        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        Program res=null;
        try{
            res=  (Program)query.getSingleResult();
        }catch (NoResultException ex){

        }
        return res;

    }
    public Section getSection(String uuid){

        Query query=emf.createEntityManager().createQuery("Select p From Section p WHERE p.uuid=:uuid");
        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        Section res=null;
        try{
        res=  (Section)query.getSingleResult();
        }catch (NoResultException ex){

        }
        return res;
    }
    public Complex getComplex(String uuid){

        Query query=emf.createEntityManager().createQuery("Select p From Complex p WHERE p.uuid=:uuid");
        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        Complex res=null;
         try{
             res = (Complex)query.getSingleResult();
         }catch (NoResultException ex){

         }
        return res;
    }

    /**
     * Список всех пользовательских программ
     * @return
     */
    public List<Program> findAllUserProgramm(){
        EntityManager entityManager = emf.createEntityManager();
        Query query= entityManager.createQuery("Select p From Program p  where p.ownerSystem=false");
        List resultList = query.getResultList();
        if(entityManager!=null)entityManager.close();
        return resultList;
    }

    /**
     * Список всех пользовательских программ
     * @return
     */
    public List<Complex> findAllUserComplex(){
        EntityManager entityManager = emf.createEntityManager();
        Query query= entityManager.createQuery("Select p From Complex p  where p.ownerSystem=false");
        List resultList = query.getResultList();
        if(entityManager!=null)entityManager.close();
        return resultList;
    }

    /**
     * Список всех пользовательских программ
     * @return
     */
    public List<Section> findAllUserEmptySection(){
        EntityManager entityManager = emf.createEntityManager();
        Query query= entityManager.createQuery("Select p From Section p  where p.ownerSystem=false and (select count(s) from Section s where s.parent=p)  = 0");
        List resultList = query.getResultList();
        if(entityManager!=null)entityManager.close();
        return resultList;
    }


    /**
     * Очистка пользовательской базы и профилей
     * Профиль биофона не удаляется
     */
    public void clearUserBaseAndProfiles() throws Exception {

        //очистка профилей
        for (Profile profile : findAllProfiles())
        {
            if(profile.getName().equals(App.BIOFON_PROFILE_NAME)){


                    for (TherapyComplex therapyComplex : findTherapyComplexes(profile)) removeTherapyComplex(therapyComplex);


            }else   removeProfile(profile);


        }




        //очистка пользовательской базы
        //найдем все программы с ownerSystem=false и удалим
        for (Program program : findAllUserProgramm()) {
            removeProgram(program);
        }

        //найдем все комплексы с ownerSystem=false и удалим
        for (Complex complex : findAllUserComplex()) {
            removeComplex(complex);
        }
        //найдем все разделы с ownerSystem=false и удалим
        List<Section> allUserEmptySection = findAllUserEmptySection();
        Section userSection=null;
        do{


            allUserEmptySection = findAllUserEmptySection();
            for (Section section : allUserEmptySection) {
                if(section.getTag()!=null){
                    if(section.getTag().equals("USER")){ userSection=section; continue;}
                }
                removeSection(section);
            }
            if(userSection!=null) allUserEmptySection.remove(userSection);
        }while (allUserEmptySection.size()>0);




    }




}




 

   