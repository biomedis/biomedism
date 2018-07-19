package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@Access(AccessType.PROPERTY)
@Entity
public class Profile implements INamed, Serializable {
  private static final long serialVersionUID = 1L;
  
    private final SimpleLongProperty id=new SimpleLongProperty();  
    private final SimpleStringProperty name=new SimpleStringProperty();

    private final SimpleLongProperty time=new SimpleLongProperty();
    private final  SimpleIntegerProperty profileWeight=new SimpleIntegerProperty();//объем файлов
    private final SimpleLongProperty position=new SimpleLongProperty();
    private final SimpleLongProperty lastChange=new SimpleLongProperty();
    // профиля
   private String uuid;

   private static Calendar cal= Calendar.getInstance();
  
   

    public Profile() {  }

    public long getLastChange() {
        return lastChange.get();
    }

    @Transient
    public SimpleLongProperty lastChangeProperty() {
        return lastChange;
    }

    public void setLastChange(long lastChange) {
        this.lastChange.set(lastChange);
    }

    public long getPosition() {
        return position.get();
    }
    @Transient
    public SimpleLongProperty positionProperty() {
        return position;
    }

    public void setPosition(long position) {
        this.position.set(position);
    }

    @Basic
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Transient
    public int getProfileWeight() {
        return profileWeight.get();
    }
    @Transient
    public SimpleIntegerProperty profileWeightProperty() {
        return profileWeight;
    }
    @Transient
    public void setProfileWeight(int profileWeight) {
        this.profileWeight.set(profileWeight);
    }

    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }
    @Transient
    public SimpleStringProperty nameProperty() {
        return name;
    }


    @Id
      @TableGenerator(name="TG7", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG7")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }


    @Basic
    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }


    /**
     * Время профиля, в секундах
     * @return
     */
    @Transient
    public long getTime() {
        return time.get();
    }
    @Transient
    public SimpleLongProperty timeProperty() {
        return time;
    }
    @Transient
    public void setTime(long time) {
        this.time.set(time);
    }


    // Для реализации сортировки имени, тк используется уже готовый INamedComparator !!!, что требует реализации INamed
    @Transient
    @Override
    public void setNameString(String val) {
        setName(val);
    }
    @Transient
    @Override
    public String getNameString() {
        return getName();
    }
    @Transient
    @Override
    public SimpleStringProperty nameStringProperty() {
        return nameProperty();
    }

    @Transient
    public void setNowChanged(){
        setLastChange(cal.getTimeInMillis());
    }
}
