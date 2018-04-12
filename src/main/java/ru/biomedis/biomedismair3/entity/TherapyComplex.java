package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.*;

import javax.persistence.*;
import java.io.Serializable;
@Access(AccessType.PROPERTY)
@Entity
public class TherapyComplex implements Serializable {
  private static final long serialVersionUID = 1L;
  
    private final SimpleLongProperty id=new SimpleLongProperty();
    private final SimpleStringProperty name=new SimpleStringProperty();
    private final SimpleStringProperty description=new SimpleStringProperty();
    private final SimpleObjectProperty<Profile> profile=new SimpleObjectProperty<>();

    private final SimpleBooleanProperty changed=new SimpleBooleanProperty();//маркер генерации файла данных. Если true то требуется регенерация файлов комплекса
    private final SimpleStringProperty oname=new SimpleStringProperty();
    private final SimpleLongProperty time=new SimpleLongProperty();
    private final SimpleLongProperty position=new SimpleLongProperty();
    private final SimpleStringProperty srcUUID=new SimpleStringProperty();

    public TherapyComplex() {

    }

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



    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }
    @Transient
    public SimpleStringProperty nameProperty() {
        return name;
    }
    @Transient
    public SimpleObjectProperty<Profile> profileProperty() {
        return profile;
    }
    @Transient
    public SimpleStringProperty descriptionProperty() {
        return description;
    }
    @Basic
    public String getDescription() {
        return description.get();
    }


    public void setDescription(String description) {
        this.description.set(description);
    }





    @ManyToOne(targetEntity = Profile.class)
    public Profile getProfile() {
        return this.profile.get();
    }

    public void setProfile(Profile profile) {
        this.profile.set(profile);
    }



    
    @Basic
    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    

   
    
     @Id
      @TableGenerator(name="TG2", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG2")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }
    




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



}
