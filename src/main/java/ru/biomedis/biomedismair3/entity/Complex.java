package ru.biomedis.biomedismair3.entity;

import java.io.Serializable;

import javafx.beans.property.*;

import javax.persistence.*;
@Access(AccessType.PROPERTY)
@Entity
public class Complex implements Serializable,IEntity,INamed,IDescriptioned {
    
  private static final long serialVersionUID = 1L;
  
  private final SimpleLongProperty id=new SimpleLongProperty();  
   private final SimpleObjectProperty<Strings> name=new SimpleObjectProperty<>();
   private final SimpleObjectProperty<Strings> description=new SimpleObjectProperty<>();
   private final SimpleObjectProperty<Section> section=new SimpleObjectProperty<>();
   private final SimpleBooleanProperty ownerSystem =new SimpleBooleanProperty();
   private final SimpleStringProperty uuid=new SimpleStringProperty();

    private final SimpleStringProperty nameString=new SimpleStringProperty("#");
    private final SimpleStringProperty descriptionString=new SimpleStringProperty("#");

   

    public Complex() {

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
    public SimpleBooleanProperty ownerSystemProperty() {
        return ownerSystem;
    }

    @Id
      @TableGenerator(name="TG10", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG10")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }
    
    
     @Basic
    public Boolean isOwnerSystem() {
        return this.ownerSystem.get();
    }

    public void setOwnerSystem(Boolean ownerSystem) {
        this.ownerSystem.set(ownerSystem);
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
    
  
}
