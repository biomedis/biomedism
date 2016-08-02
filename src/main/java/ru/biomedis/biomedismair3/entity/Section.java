package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;

@Access(AccessType.PROPERTY)
@Entity
public class Section implements Serializable,IEntity,INamed,IDescriptioned {
  private static final long serialVersionUID = 1L;
  
      private final SimpleLongProperty id=new SimpleLongProperty();
      private final SimpleBooleanProperty ownerSystem = new SimpleBooleanProperty();
      private final SimpleObjectProperty<Section>  parent=new SimpleObjectProperty<>();
      private final SimpleObjectProperty<Strings>  name=new SimpleObjectProperty<>();
      private final SimpleObjectProperty<Strings>  description=new SimpleObjectProperty<>();
      private final SimpleStringProperty uuid=new SimpleStringProperty();

    private final SimpleStringProperty tag=new SimpleStringProperty();


    private final SimpleStringProperty nameString=new SimpleStringProperty("#");
    private final SimpleStringProperty descriptionString=new SimpleStringProperty("#");





    public Section() {

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
    public SimpleBooleanProperty ownerSystemProperty() {
        return ownerSystem;
    }
    @Transient
    public SimpleObjectProperty<Section> parentProperty() {
        return parent;
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
    public SimpleStringProperty tagProperty() {
        return tag;
    }

    @Column(unique = true,nullable = true)
    public String getTag(){return tag.get();}
    public void setTag(String tag){this.tag.set(tag);}


     @Id
      @TableGenerator(name="TG5", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG5")  
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
    
    @ManyToOne(fetch = FetchType.LAZY,targetEntity = Section.class)
    public Section getParent() {
        return this.parent.get();
    }

    public void setParent(Section parent) {
        this.parent.set(parent);
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


}
