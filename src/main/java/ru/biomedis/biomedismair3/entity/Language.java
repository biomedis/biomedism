package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;
@Access(AccessType.PROPERTY)
@Entity
public class Language implements Serializable {
  private static final long serialVersionUID = 1L;
  
      private final SimpleLongProperty id=new SimpleLongProperty();
      private final SimpleStringProperty  name =new SimpleStringProperty();
      private final SimpleStringProperty  abbr =new SimpleStringProperty();
      private  final SimpleBooleanProperty avaliable=new SimpleBooleanProperty();
    
   
    
   
   

    public Language() {

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
    public SimpleStringProperty abbrProperty() {
        return abbr;
    }

    @Id
      @TableGenerator(name="TG9", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG9")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    public boolean isAvaliable() {
        return avaliable.get();
    }
    @Transient
    public SimpleBooleanProperty avaliableProperty() {
        return avaliable;
    }

    public void setAvaliable(boolean avaliable) {
        this.avaliable.set(avaliable);
    }

    @Basic
    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    
   
    
    
    @Basic
    public String getAbbr() {
        return this.abbr.get();
    }

    public void setAbbr(String abbr) {
        this.abbr.set(abbr);
    }
}
