package ru.biomedis.biomedismair3;

import ru.biomedis.biomedismair3.entity.INamed;

import java.text.Collator;
import java.util.Comparator;

/**
 * Сравнивает INamed с учетом языка. Сравнивает только названия
 * Created by Anama on 15.03.2016.
 */
public class INamedComparator implements Comparator<INamed>
{
   private  Collator collator;

    public INamedComparator(Collator collator)
    {
        this.collator = collator;
    }

    @Override
    public int compare(INamed o1, INamed o2)
    {
        if(o1==null && o2==null) return 0;
        if(o1 ==null) return -1;
        else if(o2==null) return 1;
        return collator.compare(o1.getNameString(),o2.getNameString());
    }

}
