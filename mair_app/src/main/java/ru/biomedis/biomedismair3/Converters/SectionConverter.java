package ru.biomedis.biomedismair3.Converters;

import javafx.util.StringConverter;
import ru.biomedis.biomedismair3.entity.Section;

/**
 * Created by Anama on 04.09.2015.
 */
public class SectionConverter extends StringConverter<Section>
{
    String lang;

    public SectionConverter(String lang) {
        super();
        this.lang = lang;
    }

    @Override
    public String toString(Section val) {
        if(val.getId()==0)
        {
            if(lang.equals("ru"))return "Выберите раздел";
            else if(lang.equals("de"))return "Wählen Sie die Partition";
            else if(lang.equals("lt")) return "Pasirinkti skyrių";
            else   return "Choose a section";
        }
        else return val.getNameString();
    }

    @Override
    public Section fromString(String val) {
        return null;
    }
}
