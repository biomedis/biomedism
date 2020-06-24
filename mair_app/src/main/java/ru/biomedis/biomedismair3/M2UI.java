package ru.biomedis.biomedismair3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;
import ru.biomedis.biomedismair3.entity.TherapyComplex;
import ru.biomedis.biomedismair3.entity.TherapyProgram;
import ru.biomedis.biomedismair3.m2.M2BinaryFile;
import ru.biomedis.biomedismair3.m2.M2Complex;
import ru.biomedis.biomedismair3.m2.M2Program;
import ru.biomedis.biomedismair3.utils.Date.DateUtil;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static ru.biomedis.biomedismair3.m2.M2.PAUSE_BETWEEN_PROGRAM;

/**
 * Created by Ananta on 17.02.2017.
 */
public class M2UI extends BaseController {

@FXML private ListView<TherapyComplex> complexesList;
    @FXML private ListView<TherapyProgram> programsesList;
    @FXML private Label timeComplex;
    @FXML private Label timeProfile;
    private Image m2ComplexImage;
    private ImageView m2ComplexImageView;
    private ResourceBundle resources;


    private ObservableList<TherapyComplex> m2Complexes = FXCollections.observableArrayList();
    private ObservableList<TherapyProgram> m2Programs = FXCollections.observableArrayList();
    private ModelDataApp mda ;
    private Map<Long,List<TherapyProgram>> programsCache=new HashMap<>();

    /**
     * Очистка контента при отсоединении устройства
     */
    public void cleanView(){
        m2Complexes.clear();
        m2Programs.clear();
        programsCache.clear();
        timeComplex.setText("");
        timeProfile.setText("");
    }

    /**
     * Парсит файл с прибора
     * @param m2BinaryFile
     * @param dstComplexes куда поместить компоексы
     * @param dstProgramsCache программы комплексов
     * @return общее время
     */
    public int parseFile(M2BinaryFile m2BinaryFile,List<TherapyComplex> dstComplexes,Map<Long,List<TherapyProgram>> dstProgramsCache){
        int totalTime=0;
        long i=0;//фейковые ID для комплексов
        TherapyComplex tc;
        for (M2Complex complex : m2BinaryFile.getComplexesList()) {
            tc=new TherapyComplex();
            tc.setId(++i);
            tc.setName(complex.getName());
            tc.setBundlesLength(complex.getBundlesLength());
            tc.setTimeForFrequency(complex.getTimeByFrequency());
            tc.setDescription("");


            List<TherapyProgram> ltp=new ArrayList<>();
            TherapyProgram tp;
            long j=0;//фейковые ID для программ
            for (M2Program program : complex.getPrograms()) {

                tp=new TherapyProgram();
                tp.setId(++j);
                tp.setName(program.getName());
                tp.setDescription("");
                tp.setFrequencies(program.getFrequencies().stream().map(String::valueOf).collect(Collectors.joining(";")));
                ltp.add(tp);
            }
            if(complex.getCountPrograms()==0) dstProgramsCache.put(tc.getId(),Collections.EMPTY_LIST);
            else dstProgramsCache.put(tc.getId(),ltp);

            dstComplexes.add(tc);
            totalTime+=calcComplexTime(dstProgramsCache.get(tc.getId()),tc.getBundlesLength(),PAUSE_BETWEEN_PROGRAM,tc.getTimeForFrequency());
        }
        return totalTime;
    }


    /**
     * Установка контента по файлу с устройства
     * @param m2BinaryFile
     */
    public void setContent(M2BinaryFile m2BinaryFile){

        cleanView();
        int totalTime=parseFile(m2BinaryFile,m2Complexes,programsCache);
        timeProfile.setText(DateUtil.convertSecondsToHMmSs(totalTime));

    }

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
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
         mda = App.getStaticModel();

        URL loc = getClass().getResource("/images/medical_record.png");
        m2ComplexImage = new Image(loc.toExternalForm());
        m2ComplexImageView = new ImageView(m2ComplexImage);

        initListComplexes();
        initListPrograms();
    }

    private void initListComplexes() {
        complexesList.setPlaceholder(new Label(resources.getString("app.table.complex_placeholder")));

        complexesList.setCellFactory(param -> new ListCell<TherapyComplex>() {
            private ImageView imgv;

            @Override
            protected void updateItem(TherapyComplex item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                } else {
                    this.setText(item.getName());
                    if (imgv == null) imgv = new ImageView(m2ComplexImage);

                    setGraphic(imgv);
                }


            }

        });
        complexesList.setItems(m2Complexes);

        complexesList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> viewM2ProgramsOnComplexClick());
    }

    public void viewM2ProgramsOnComplexClick() {

        TherapyComplex selectedItem = complexesList.getSelectionModel().getSelectedItem();
        if (selectedItem==null) return;
        m2Programs.clear();
        m2Programs.addAll(programsCache.get(selectedItem.getId()));

       int time=calcComplexTime(programsCache.get(selectedItem.getId()),selectedItem.getBundlesLength(),PAUSE_BETWEEN_PROGRAM,selectedItem.getTimeForFrequency());
        timeComplex.setText(DateUtil.convertSecondsToHMmSs(time));
    }

    private void initListPrograms() {

        programsesList.setPlaceholder(new Label(resources.getString("app.table.programm_placeholder")));
        programsesList.disableProperty().bind(complexesList.getSelectionModel().selectedItemProperty().isNull());
        programsesList.setCellFactory(param -> new ListCell<TherapyProgram>() {
            private Text name;
            private Text freqs;
            private VBox vbox;

            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                } else {
                    if(vbox==null){
                        name  = new Text(item.getName());
                        name.setFont(Font.font(null, FontWeight.BOLD, 12));
                        freqs = new Text(item.getFrequencies().replace(";","; ").replace("+","; "));
                        freqs.setWrappingWidth(getListView().getWidth()-30); // Setting the wrapping width to the Text
                        freqs.wrappingWidthProperty().bind(getListView().widthProperty().subtract(30));
                        vbox=new VBox();
                        vbox.getChildren().addAll(name,freqs);
                        vbox.setSpacing(4);


                    }else {
                        name.setText(item.getName());
                        freqs.setText(item.getFrequencies().replace(";","; ").replace("+","; "));
                    }

                    setGraphic(vbox);
                }


            }

        });
        programsesList.setItems(m2Programs);
    }


    private int calcComplexTime(List<TherapyProgram> tpl,int bundlesLength,int pauseBetweenProgramm,int timeForFreq){
        // количество программ

        int resTimeSec=0;
        for (TherapyProgram tp : tpl) {
            if(bundlesLength!=1){

                int numFreqsForce = tp.getNumFreqsForce();
                int freqBundlesCount=(int)Math.ceil((float)numFreqsForce/(float)bundlesLength);
                resTimeSec += freqBundlesCount * timeForFreq ;
                resTimeSec+=pauseBetweenProgramm;
            }else {
                resTimeSec +=(timeForFreq+pauseBetweenProgramm);
            }

        }
        if(resTimeSec==0) return 0;
        else  return resTimeSec-pauseBetweenProgramm;
    }
}
