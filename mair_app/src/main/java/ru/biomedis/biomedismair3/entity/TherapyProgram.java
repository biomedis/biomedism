package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Access(AccessType.PROPERTY)
@Entity
public class TherapyProgram implements Serializable {
  private static final long serialVersionUID = 1L;
  
  
   private final SimpleLongProperty id=new SimpleLongProperty();  
   private final SimpleStringProperty name=new SimpleStringProperty();
    private final SimpleStringProperty description=new SimpleStringProperty();
   private final SimpleLongProperty position=new SimpleLongProperty();
     private final SimpleObjectProperty<TherapyComplex> therapyComplex=new SimpleObjectProperty<>();
    private final SimpleStringProperty frequencies=new SimpleStringProperty();

    private final SimpleBooleanProperty changed=new SimpleBooleanProperty();//если true значит нужна регенерация файла программы bss
    private final SimpleLongProperty timeMarker=new SimpleLongProperty();//маркер даты и времени последней регенерации файла програмы

    private final SimpleBooleanProperty fakeChange=new SimpleBooleanProperty();//свойство для инициализации перерасчета наличия файла частот

    private final SimpleBooleanProperty mp3=new SimpleBooleanProperty();//свойство для указания что программа  - на основе mp3 файла, путь к файлу будет в поле частот
    private final SimpleStringProperty oname=new SimpleStringProperty();
    private final SimpleBooleanProperty multyFreq=new SimpleBooleanProperty();
    private final SimpleStringProperty srcUUID=new SimpleStringProperty();

    private String uuid;
   
   
   //в таблице даные отображаются черех этот объект
   private SearchFreqs searchFreqs =new SearchFreqs();
   private SearchName searchName =new SearchName();


    public String getSrcUUID() {
        return srcUUID.get();
    }
    @Transient
    public SimpleStringProperty srcUUIDProperty() {
        return srcUUID;
    }

    public void setSrcUUID(String srcUUID) {
        this.srcUUID.set(srcUUID);
    }

    /**
     * Было ли совпадение при последнем поиске
     * @return
     */
   @Transient
   public boolean isMatchedFreqs(){return searchFreqs.hasMatching();}


    /**
     * Было ли совпадение вообще в любом имени
     * @return
     */
    @Transient
    public boolean isMatchedAnyName(){return searchName.hasMatching();}
    /**
     * Было ли совпадение при последнем поиске
     * @return
     */
    @Transient
    public boolean isMatchedName(){return searchName.isMatchedName();}
    /**
     * Было ли совпадение при последнем поиске
     * @return
     */
    @Transient
    public boolean isMatchedOName(){return searchName.isMatchedOName();}
    /**
     * поиск по частотам. Частоты - строка с частотами через пробел
     * @param pattern
     * @return
     */
    @Transient
    public List<SearchFreqs.Freq> searchFreqsResult(String pattern){return searchFreqs.searchFreqsResult(pattern);}

    /**
     * Было ли полное совпадение списка при поиске. Можно проверять если поиск дал true
     * @return
     */
    @Transient
    public boolean hasAllFreqListMatching(){return searchFreqs.isAllListMatching();}

    /**
     * поиск по частотам. Частоты - строка с частотами через пробел
     * @param pattern
     * @return
     */
    @Transient
    public boolean searchFreqs(String pattern){return searchFreqs.searchFreqs(pattern);}

    /**
     * Вернет частоты с разделителями и указание совпали ли они с прошлым поиском. Рекомендуется проверка с isMatchedFreqs(), чтобы упростить алг.отображения
     * @return
     */
    @Transient
    public List<SearchFreqs.Freq> getSearchResultFreqs(){return searchFreqs.getFreqs();}
    /**
     * Очистить состояние поиска и данные
     */
    @Transient
    public void cleanSearch( ){
       searchFreqs.clean();
        searchName.clean();
    }

    @Transient
    public List<SearchName.NamePart> getSearchResultName(){
        return searchName.getNameParts();
    }
    @Transient
    public List<SearchName.NamePart> getSearchResultOName(){
        return searchName.getONameParts();
    }

    /**
     * Поиск по name и oname
     * @param pattern
     * @return
     */
    @Transient
    public boolean searchNames(String pattern){return searchName.search(getName(),getOname(),pattern);}


    public TherapyProgram()
    {


    }

    public boolean isMultyFreq() {
        return multyFreq.get();
    }
    @Transient
    public SimpleBooleanProperty multyFreqProperty() {
        return multyFreq;
    }

    public void setMultyFreq(boolean multyFreq) {
        this.multyFreq.set(multyFreq);
    }

    public String getOname() {
        return oname.get();
    }
    @Transient
    public SimpleStringProperty onameProperty() {
        return oname;
    }

    public void setOname(String oname) {
        this.oname.set(oname);
    }

    public boolean isMp3() {
        return mp3.get();
    }
    @Transient
    public SimpleBooleanProperty mp3Property() {
        return mp3;
    }

    public void setMp3(boolean mp3) {
        this.mp3.set(mp3);
    }

    @Basic
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Transient
    public boolean getFakeChange() {
        return fakeChange.get();
    }
    @Transient
    public SimpleBooleanProperty fakeChangeProperty() {
        return fakeChange;
    }
    @Transient
    public void setFakeChange(boolean fakeChange) {
        this.fakeChange.set(fakeChange);
    }

    public long getTimeMarker() {
        return timeMarker.get();
    }
    @Transient
    public SimpleLongProperty timeMarkerProperty() {
        return timeMarker;
    }

    public void setTimeMarker(long timeMarker) {
        this.timeMarker.set(timeMarker);
    }

    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }


    public boolean isChanged() {
        return changed.get();
    }
    @Transient
    public SimpleBooleanProperty changedProperty() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed.set(changed);
    }

    public String getDescription() {
        return description.get();
    }
    @Transient
    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }


    public String getFrequencies() {
        return frequencies.get();
    }

    @Transient
    public SimpleStringProperty frequenciesProperty() {
        return frequencies;
    }

    public void setFrequencies(String frequencies) {
        this.frequencies.set(frequencies);
        searchFreqs.parseFreqString(frequencies);
    }

    @Transient
    public SimpleStringProperty nameProperty() {
        return name;
    }
    @Transient
    public SimpleLongProperty positionProperty() {
        return position;
    }

    @Transient
    public SimpleObjectProperty<TherapyComplex> therapyComplexProperty() {
        return therapyComplex;
    }

    @Basic
    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
      @Id
      @TableGenerator(name="TG1", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG1")   
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }
    
    @Basic
    public Long getPosition() {
        return this.position.get();
    }

    public void setPosition(Long position) {
        this.position.set(position);
    }

    @ManyToOne(targetEntity = TherapyComplex.class)
    public TherapyComplex getTherapyComplex() {
        return this.therapyComplex.get();
    }

    public void setTherapyComplex(TherapyComplex therapyComplex) {
        this.therapyComplex.set(therapyComplex);
    }


    /**
     * общее число частот - мультичастоты считаются за 1 частоту!
     * @return
     */
    @Transient
    public int getNumFreqs() {
        return frequencies.get().split(";").length;
    }

    /**
     * общее число частот, без учета мультичастот!
     * @return
     */
    @Transient
    public int getNumFreqsForce() {


       String[] split = frequencies.get().split(";");

        int count=0;
        for (String s : split)
        {
            if(s.contains("+"))
            {
                String[] split1 = s.split("\\+");

                count+=split1.length;

            }else count++;

        }
        return count;
    }

    /**
     * Список всех частот программы
     * @return
     */
    @Transient
    public List<Double> parseFreqs(){

       return Arrays.stream( frequencies.get().split(";"))
                    .flatMap(f->Arrays.stream(f.split("\\+")))
                    .map(TherapyProgram::parseSingleFreq)
                    .collect(Collectors.toList());
    }

    @Transient
    public List<String> parseFreqsStrings(){

        return Arrays.stream( frequencies.get().split(";"))
                .flatMap(f->Arrays.stream(f.split("\\+")))
                .map(f->f.replace(",","."))
                .collect(Collectors.toList());
    }

    /**
     * Получает списки с частотами разбитые на группу по ;
     * Если в списке более 1 частоты, то они были набиты с +
     * @return
     */
    @Transient
    public List<List<Double>> parseFreqsSequenceMode(){

        return Arrays.stream( frequencies.get().split(";"))
                     .map(TherapyProgram::parseMultySequence)
                     .collect(Collectors.toList());

    }

    /**
     * Парсит часть часть строки с частотами с учетом + +
     * @param f строка частот с + или без
     * @return
     */
    private static List<Double> parseMultySequence(String f) {
        List<Double> con;
        if(!f.contains("+")) {
            con = new ArrayList();
            con.add(parseSingleFreq(f));
        }
        else {
             con = Arrays.stream(f.split("\\+"))
                         .map(TherapyProgram::parseSingleFreq)
                         .collect(Collectors.toList());
        }
        return con;
    }

    private static Double parseSingleFreq(String f){
        return Double.parseDouble(f.replace(",","."));
    }

}
