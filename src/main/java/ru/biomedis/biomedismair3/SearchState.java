package ru.biomedis.biomedismair3;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/**
 * Статус поиска по базе
 * Раздел открытый до поиска чиатается из ComboBox тк любое его изменение отменит поиск
 */
class SearchState
{
   private   boolean search=false;
    private String searchText="";

    private ReadOnlyBooleanWrapper searched=new ReadOnlyBooleanWrapper(false);




    public ReadOnlyBooleanProperty searchedProperty() {
        return searched.getReadOnlyProperty();
    }



    public boolean isSearch() {
        return this.search;
    }

    public void setSearch(boolean search) {
        this.search = search;
        searched.set(search);
    }

    public String getSearchText() {
        return this.searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void clear(){

       setSearch(false);
       setSearchText("");

    }
}
