package ru.biomedis.biomedismair3.entity;

import java.io.Serializable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
@Access(AccessType.PROPERTY)
@Entity
public class Program implements Serializable,IEntity,INamed,IDescriptioned  {
  private static final long serialVersionUID = 1L;
  
  
  
    private final SimpleLongProperty id=new SimpleLongProperty();  
    private final SimpleObjectProperty<Strings> name=new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Strings> description=new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Section> section=new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Complex> complex=new SimpleObjectProperty<>();
    private final SimpleStringProperty frequencies =new SimpleStringProperty();
    private final SimpleBooleanProperty ownerSystem =new SimpleBooleanProperty();
    private final SimpleLongProperty position=new SimpleLongProperty();
    private final SimpleStringProperty uuid=new SimpleStringProperty();

    private final SimpleStringProperty nameString=new SimpleStringProperty("#");
    private final SimpleStringProperty descriptionString=new SimpleStringProperty("#");
    

    public Program() {

    }

    public String getUuid() {
        return uuid.get();
    }
    @Transient
    public SimpleStringProperty uuidProperty() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid.set(uuid);
    }


    @Transient
    public String getDescriptionString() {
        return descriptionString.get();
    }
    @Transient
    public SimpleStringProperty desriptionStringProperty() {
        return descriptionString;
    }
    @Transient
    public void setDescriptionString(String desriptionString) {
        this.descriptionString.set(desriptionString);
    }
    @Transient
    public String getNameString() {
        return nameString.get();
    }
    @Transient
    public SimpleStringProperty nameStringProperty() {
        return nameString;
    }
    @Transient
    public void setNameString(String nameString) {
        this.nameString.set(nameString);
    }


    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }
    @Transient
    public SimpleObjectProperty<Strings> nameProperty() {
        return name;
    }
    @Transient
    public SimpleObjectProperty<Strings> descriptionProperty() {
        return description;
    }
    @Transient
    public SimpleObjectProperty<Section> sectionProperty() {
        return section;
    }
    @Transient
    public SimpleObjectProperty<Complex> complexProperty() {
        return complex;
    }
    @Transient
    public SimpleStringProperty frequenciesProperty() {
        return frequencies;
    }


    @Transient
    public SimpleBooleanProperty ownerSystemProperty() {
        return ownerSystem;
    }
    @Transient
    public SimpleLongProperty positionProperty() {
        return position;
    }

    @Basic
    public Long getPosition()
    {return position.get();}
    
    public void setPosition(Long position){this.position.set(position);}
    
     @Basic
    public Boolean isOwnerSystem() {
        return this.ownerSystem.get();
    }

    public void setOwnerSystem(Boolean ownerSystem) {
        this.ownerSystem.set(ownerSystem);
    }
   
     @ManyToOne(targetEntity = Complex.class)
    public Complex getComplex() {
        return this.complex.get();
    }

    public void setComplex(Complex complex) {
        this.complex .set(complex);
    }


     @OneToOne(targetEntity = Strings.class)
    public Strings getName() {
        return this.name.get();
    }

    public void setName(Strings name) {
        this.name.set(name);
    }
    
    
    
   @OneToOne(targetEntity = Strings.class)
    public Strings getDescription() {
        return this.description.get();
    }
  
    public void setDescription(Strings description) {
        this.description.set(description);
    }
    
     @ManyToOne(targetEntity = Section.class)
    public Section getSection() {
        return this.section.get();
    }

    public void setSection(Section section) {
        this.section.set(section);
    }
     @Id
      @TableGenerator(name="TG6", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG6")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }
    
    @Basic
    public String getFrequencies() {
        return this.frequencies.get();
    }

    public void setFrequencies(String frequencies) {
        this.frequencies .set(frequencies);
    }
}
