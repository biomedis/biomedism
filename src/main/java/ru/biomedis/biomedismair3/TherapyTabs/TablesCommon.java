package ru.biomedis.biomedismair3.TherapyTabs;

import java.util.Arrays;

public class TablesCommon {

    /**
     * Можно ли вставлять(после вырезанного) комплексы, программы или профили  в той же таблице в этом месте
     * @param dropIndex
     * @param ind индексы строк для перемещения
     * @return
     */
    public static boolean isEnablePaste(int dropIndex, Integer[] ind) {
        if (Arrays.stream(ind).filter(i -> i.intValue() == dropIndex).count() == 0) {
            int startIndex = ind[0];
            int lastIndex = ind[ind.length - 1];
            if (dropIndex < startIndex || dropIndex > lastIndex) return true;
            else return false;
        }else return false;
    }
}
