package ru.biomedis.biomedismair3.Converters;

import javafx.util.StringConverter;
import ru.biomedis.biomedismair3.entity.INamed;
import ru.biomedis.biomedismair3.entity.Section;

/**
 * Created by Anama on 04.09.2015.
 */
public class NamedConverter extends StringConverter<INamed> {
    @Override
    public String toString(INamed val) {
        if(val==null) return "-";
        else if(val.getId()==0)return "-";
        else return val.getNameString();
    }

    @Override
    public Section fromString(String val) {
        return null;
    }
}
