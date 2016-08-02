package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleLongProperty;

import javax.persistence.*;
import java.io.Serializable;
@Access(AccessType.PROPERTY)
@Entity
public class Strings implements Serializable {
  private static final long serialVersionUID = 1L;
  
   private final SimpleLongProperty id=new SimpleLongProperty();  
   

    public Strings() {

    }
    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }

    @Id
      @TableGenerator(name="TG4", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG4")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }
    
  
}
