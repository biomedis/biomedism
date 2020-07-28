package ru.biomedis.biomedismair3.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.entity.Language;

import java.net.URL;
import java.util.ResourceBundle;



@Slf4j
public class LangController extends BaseController {

    @FXML private ComboBox<Language> langlist;


    @Override
    protected void onCompletedInitialization() {

    }

    @Override
    protected void onClose(WindowEvent event) {

    }

    @Override
    public void setParams(Object... params) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {


        langlist.setConverter(new StringConverter<Language>() {
            @Override
            public String toString(Language object) {
                if (object.getId().longValue() == 0) return resources.getString("app.lang.system_or_eng");
                return object.getName();
            }

            @Override
            public Language fromString(String string) {
                return null;
            }
        });
        Language l=new Language();
        l.setAbbr("");
        l.setName("");
        l.setId(0L);

       // langlist.getItems().add(l);
        langlist.getItems().addAll(getModel().findAvaliableLangs());

        try {
            String abbr =   getModel().getOption("app.lang");
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
            log.error("",e);
            langlist.getSelectionModel().select(0);
        }


        langlist.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
          /*  if(newValue.intValue()==0)
            {
                try {
                    getModel().setOption("app.lang","");
                    if (getModel().getLanguage(getModel().getSystemLocale().getLanguage()).isAvaliable()){
                        setInsertCompexLang(getModel().getSystemLocale().getLanguage());
                    }else  setInsertCompexLang("en");


                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка применения параметра языка","","",e,getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }
            }else
            {  */
                try {
                    getModel().setOption("app.lang",langlist.getItems().get(newValue.intValue()).getAbbr());
                    setInsertCompexLang(langlist.getItems().get(newValue.intValue()).getAbbr());
                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка применения параметра языка", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    return;
                }
            //}
            showInfoDialogNoHeader(resources.getString("app.text.change_lang_title"),resources.getString("app.text.change_lang_text"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                window.close();


        });


    }

    private void setInsertCompexLang(String abbr){
        try {
            getModel().setOption("app.lang_insert_complex",abbr);
        } catch (Exception e) {
            log.error("",e);
            showExceptionDialog("Ошибка применения параметра языка", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            return;
        }

    }

}
