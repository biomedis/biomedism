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

        langlist.getItems().addAll(getModel().findAvaliableLangs());

        try {
            String abbr =   getModel().getOption("app.lang_insert_complex");
            if(abbr.isEmpty()) langlist.getSelectionModel().select(0);
            else
            {
                int index=-1;

                for (int i=0;i<langlist.getItems().size();i++) {
                    if(langlist.getItems().get(i).getAbbr().equals(abbr))  {index=i;break;}

                }


                if(index!=-1) langlist.getSelectionModel().select(index);
                else langlist.getSelectionModel().select(0);
            }

        } catch (Exception e) {
            logger.error("",e);
            langlist.getSelectionModel().select(0);
        }


        langlist.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue.intValue()==0)
            {
                try {
                    getModel().setOption("app.lang_insert_complex","en");
                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка применения параметра языка","","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }
            }else
            {
                try {
                    getModel().setOption("app.lang_insert_complex",langlist.getItems().get(newValue.intValue()).getAbbr());
                } catch (Exception e) {
                    logger.error("",e);
                    showExceptionDialog("Ошибка применения параметра языка", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }
            }
            showInfoDialogNoHeader(resources.getString("app.menu.insert_language"),resources.getString("app.success"),getApp().getMainWindow(),Modality.WINDOW_MODAL);

            window.close();


        });


    }

}
