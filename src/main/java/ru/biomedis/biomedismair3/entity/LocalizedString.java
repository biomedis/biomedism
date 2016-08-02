package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.io.Serializable;
@Access(AccessType.PROPERTY)
@Entity
public class LocalizedString implements Serializable {
  private static final long serialVersionUID = 1L;
  
   private final SimpleLongProperty id=new SimpleLongProperty();  
   private final SimpleObjectProperty<Strings> strings=new SimpleObjectProperty<>();
   private final SimpleObjectProperty<Language> language=new SimpleObjectProperty<>();
   private  final SimpleStringProperty content =new SimpleStringProperty();
    
   
   
  

    public LocalizedString() {

    }
    @Transient
    public SimpleLongProperty idProperty() {
        return id;
    }
    @Transient
    public SimpleObjectProperty<Strings> stringsProperty() {
        return strings;
    }
    @Transient
    public SimpleObjectProperty<Language> languageProperty() {
        return language;
    }
    @Transient
    public SimpleStringProperty contentProperty() {
        return content;
    }

    @Basic
    public String getContent()
    {
       return content.get();
    }
    public void setContent(String content)
    {
        this.content.set(content);
    }
    
   @ManyToOne(targetEntity = Strings.class)
    public Strings getStrings() {
        return this.strings.get();
    }

    public void setStrings(Strings strings) {
        this.strings.set(strings);
    }
    @ManyToOne(targetEntity = Language.class)
    public Language getLanguage() {
        return this.language.get();
    }

    public void setLanguage(Language language) {
        this.language.set(language);
    }
    
    @Id
      @TableGenerator(name="TG8", allocationSize=1)
      @GeneratedValue(strategy = GenerationType.TABLE,generator = "TG8")  
    public Long getId() {
        return this.id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }
}
