package ru.biomedis.biomedismair3.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.entity.Language;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.biomedis.biomedismair3.Log.logger;

/**
 * Created by Anama on 30.09.2015.
 */
public class LangInsertComplexController extends BaseController {

    @FXML private ComboBox<Language> langlist;



    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        initComboBoxConverter();
        fillComboBox();
        selectSavedLang();
        setSelectLanguageHandler(resources);
    }

    private void setSelectLanguageHandler(ResourceBundle resources) {
        langlist.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {

                try {
                    getModel().setOption("app.lang_insert_complex", getLangAbbrByComboboxIndex(newValue));
                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка применения параметра языка", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }

            showInfoDialogNoHeader(resources.getString("app.menu.insert_language"),resources.getString("app.success"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

            window.close();


        });
    }

    private String getLangAbbrByComboboxIndex(Number newValue) {
        return langlist.getItems().get(newValue.intValue()).getAbbr();
    }

    private void selectSavedLang() {
        try {
            String abbr =   getModel().getOption("app.lang_insert_complex");
            if(abbr.isEmpty()) abbr = getModel().getDefaultLanguage().getAbbr();
            int index =  findIndexByLangAbbr(abbr);
            if(index == -1)  index = findIndexByLangAbbr(getModel().getDefaultLanguage().getAbbr());
            if(index!=-1) langlist.getSelectionModel().select(index);
            langlist.getSelectionModel().select(index);

        } catch (Exception e) {
            logger.error("",e);
            langlist.getSelectionModel().select(0);
        }
    }

    private int findIndexByLangAbbr(String abbr){
        int index=-1;
        for (int i=0;i<langlist.getItems().size();i++) {
            if(langlist.getItems().get(i).getAbbr().equals(abbr))  {
                index=i;
                break;
            }
        }
        return index;
    }
    private void fillComboBox() {
        langlist.getItems().addAll(getModel().findAvaliableLangs());
    }

    private void initComboBoxConverter() {
        langlist.setConverter(new StringConverter<Language>() {
            @Override
            public String toString(Language object) {
                if (object.getId().longValue() == 0) return "Язык системы или английский";
                return object.getName();
            }

            @Override
            public Language fromString(String string) {
                return null;
            }
        });
    }

}
