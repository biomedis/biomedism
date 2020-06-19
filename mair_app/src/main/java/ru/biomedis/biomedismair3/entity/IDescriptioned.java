package ru.biomedis.biomedismair3.entity;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Anama on 07.09.2015.
 */
public interface IDescriptioned
{
    public void setDescriptionString(String val);
    public String getDescriptionString();
    public SimpleStringProperty desriptionStringProperty();
}
