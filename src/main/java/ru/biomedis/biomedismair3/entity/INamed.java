package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Anama on 07.09.2015.
 */
public interface INamed
{

    public Long getId();
    public void setId(Long id);
    public void setNameString(String val);
    public String getNameString();
    public SimpleStringProperty nameStringProperty();
}
