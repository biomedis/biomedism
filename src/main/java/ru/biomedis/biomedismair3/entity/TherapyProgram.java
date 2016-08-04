package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;

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


    private String uuid;
   
   
   
   
   
   
    

    public TherapyProgram()
    {


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

}
