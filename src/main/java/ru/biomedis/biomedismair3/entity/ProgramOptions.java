package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;

@Access(AccessType.PROPERTY)
@Entity
public class ProgramOptions implements Serializable {
  private static final long serialVersionUID = 1L;

    private final SimpleLongProperty id=new SimpleLongProperty();
    private final SimpleStringProperty name=new SimpleStringProperty();
    private final SimpleStringProperty value=new SimpleStringProperty();





    public ProgramOptions() {  }

    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }
    @Transient
    public SimpleStringProperty nameProperty() {
        return name;
    }

    @Transient
    public SimpleStringProperty valueProperty() {
        return value;
    }

    @Basic
    public String getValue() {
        return value.get();
    }


    public void setValue(String value) {
        this.value.set(value);
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
    @Column(unique = true)
    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    
    
}
