package ru.biomedis.biomedismair3.Layouts.LeftPanel;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.AppController;
import ru.biomedis.biomedismair3.BaseController;
import ru.biomedis.biomedismair3.Converters.SectionConverter;
import ru.biomedis.biomedismair3.Dialogs.NameDescroptionDialogController;
import ru.biomedis.biomedismair3.Dialogs.ProgramDialogController;
import ru.biomedis.biomedismair3.Layouts.ProgressPanel.ProgressAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Complex.ComplexAPI;
import ru.biomedis.biomedismair3.TherapyTabs.Profile.ProfileTable;
import ru.biomedis.biomedismair3.UserUtils.Export.ExportUserBase;
import ru.biomedis.biomedismair3.UserUtils.Import.ImportUserBase;
import ru.biomedis.biomedismair3.Waiter;
import ru.biomedis.biomedismair3.entity.*;
import ru.biomedis.biomedismair3.utils.Files.FilesProfileHelper;
import ru.biomedis.biomedismair3.utils.Files.ProgramFileData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Slf4j
public class LeftPanelController extends BaseController implements LeftPanelAPI{


    @FXML private Button searchReturn;
    @FXML private ComboBox<Section> baseCombo;//первый уровень разделов( типа выбор базы)
    @FXML private ComboBox<Section> sectionCombo;//второй уровень разделов
    @FXML private TreeView<INamed> sectionTree;//дерево разделов
    @FXML private HBox userActionPane;//панель пользовательских действий
    @FXML private Button createUserBtn;//создание в пользовательской базе
    @FXML private Button editUserBtn;//редакт. в пользовательской базе
    @FXML private Button delUserBtn;//удал. в пользовательской базе
    @FXML private TextArea programInfo;
    @FXML private TextArea programDescription;
    @FXML private TextField searchPatternField;
    @FXML private Button searchBtn;
    private ContextMenu searchMenu=new ContextMenu();
    private SearchState searchState=new SearchState();
    private ContextMenu deleteMenu=new ContextMenu();

    private NamedTreeItem rootItem=new NamedTreeItem();//корень дерева разделов(всегда есть, мы в нем мменяем только дочерние элементы)

    //экстрактор для событий обновления комбобокса
    //private Callback<Section ,Observable [] > extractor = param -> new Observable[]{param.nameStringProperty(),param.desriptionStringProperty()};
    private List<Section> sectionsBase=new ArrayList<>();//основные разделы
    private ResourceBundle res;
    private TreeActionListener treeActionListener;

    private ContextMenu developerMenu = new ContextMenu();



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
        res = resources;

        initSearchUI();
        initDeleteFromUserBaseMenu(res);

        userActionPane.setDisable(true);
        programInfo.setEditable(false);
        programInfo.setWrapText(true);
        programDescription.setEditable(false);
        programDescription.setWrapText(true);

        initBaseCombo();
        initSectionCombo();
        initSectionTree();
        if(getApp().isDeveloped())initDeveloperContextMenu();


    }

    private void initDeveloperContextMenu() {
        MenuItem itemShowInfo = new MenuItem("Показать информацию");
        itemShowInfo.setOnAction(event -> {
            INamed item = selectedSectionTreeItem();

            System.out.println(item);
        });
        developerMenu.getItems().addAll(itemShowInfo);
        sectionTree.setContextMenu(developerMenu);


    }

    private ComplexAPI getComplexAPI(){return AppController.getComplexAPI();}
    private ProgressAPI getProgressAPI(){ return AppController.getProgressAPI(); }

    private void initSearchUI() {
        searchReturn.setDisable(true);
        searchReturn.disableProperty().bind(searchState.searchedProperty().not());
        initSearchContextMenu();
    }

    private void initSearchContextMenu() {
        MenuItem smi1=new MenuItem(res.getString("app.text.search_in_dep"));
        MenuItem smi2=new MenuItem(res.getString("app.text.search_in_cbase"));
        MenuItem smi3=new MenuItem(res.getString("app.text.search_in_allbase"));
        SeparatorMenuItem spmi=new SeparatorMenuItem();
        MenuItem smi4=new MenuItem(res.getString("app.back"));

        smi1.setOnAction(event2 ->  fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_DEP,searchPatternField.getText())));
        smi2.setOnAction(event2 ->  fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_BASE, searchPatternField.getText())));
        smi3.setOnAction(event2 ->  fillTreeFind(new FindFilter(SearchActionType.IN_ALL_BASE,searchPatternField.getText())));
        smi4.setOnAction(event2 ->    clearSearch(true,true));


        searchMenu.getItems().addAll(smi3, smi2, smi1, spmi, smi4);
        searchBtn.setOnAction(event1 ->
        {
            //покажем пункты меню в зависимости от выбранных элементов базы и режима поиска!
            if (!searchState.isSearch()) smi4.setDisable(true);
            else smi4.setDisable(false);

            if (sectionCombo.getValue().getId().longValue() == 0) smi1.setDisable(true);
            else smi1.setDisable(false);


            //используем searchState объект
            if(!searchMenu.isShowing())searchMenu.show(searchBtn, Side.BOTTOM, 0, 0);
            else searchMenu.hide();
        });

        //нажатие на ввод вызовет поиск по всей базе
        searchPatternField.setOnAction(event1 ->
        {
            fillTreeFind(new FindFilter(SearchActionType.IN_ALL_BASE, searchPatternField.getText()));
            /*
              //нажатие на ввод вызовет поиск по выбранному разделу или базе
            if (sectionCombo.getValue().getId().longValue() == 0) fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_BASE, searchPatternField.getText()));
            else fillTreeFind(new FindFilter(SearchActionType.IN_SELECTED_DEP, searchPatternField.getText()));
            */
        });
/*
        searchTooltip.setText("Происк производится по нажатию кнопки 'Найти'\nили по нажатию кнопки 'Enter' на клавиатуре\n( произойдет поиск в выбранном разделе или базе если раздел не выбран).");
        searchPatternField.setTooltip(searchTooltip);
        hackTooltipStartTiming(searchTooltip, 250, 15000);
*/
    }

    /**
     *  возврат к состоянию дерева как до поиска, возврат состояния других элементов
     * @param restoreState восстановить старое дерево до поиска?
     */
    private void clearSearch(boolean restoreState,boolean resetSearchField) {
        if (searchState.isSearch()) {
            //произведем подчистку, иначе
            if (restoreState) {
                //восстановление дерева как до поиска

                fillTree(sectionCombo.getValue());


            }
        }
        if (resetSearchField) searchPatternField.setText("");
        //вернем в открытое состояние если у нас выбрана родительская база
        String tag = baseCombo.getSelectionModel().getSelectedItem().getTag();
        if (tag != null ? tag.equals("USER") : false) userActionPane.setDisable(false);
        else userActionPane.setDisable(true);

        searchState.clear();
    }

    private void initBaseCombo() {
        List<Section> allRootSection;// разделы старая и новая база
        allRootSection = getModel().findAllRootSection();// разделы разных баз(старая и новая)
        getModel().initStringsSection(allRootSection);
        baseCombo.setConverter(new SectionConverter(getModel().getProgramLanguage().getAbbr()));
        baseCombo.getItems().addAll(allRootSection);
        baseCombo.setVisibleRowCount(5);

        //  sectionCombo.setPlaceholder(new Label(rb.getString("ui.main.empty_list")));
        //выбор базы
        baseCombo.setOnAction(event ->
        {
            programDescription.setText("");
            programInfo.setText("");
            clearSearch(false,false);//очистка состояния поиска
            sectionTree.setShowRoot(false);
            //переключение панели кнопок действий для пользовательского раздела
            String tag = baseCombo.getSelectionModel().getSelectedItem().getTag();
            if (tag != null ? tag.equals("USER") : false) userActionPane.setDisable(false);
            else userActionPane.setDisable(true);

            fillSectionsSelectedBase();

            //если список подразделов пуст, то попробуем заполнить дерево из корня, те из выбранной базы. Для тринити сейчас так
            if(sectionsBase.size()<=1 && !tag.equals("USER")){
                clearSearch(false,false);//очистка состояния поиска
                Section selectedItem = baseCombo.getSelectionModel().getSelectedItem();
                programDescription.setText("");
                programInfo.setText("");
                fillTree(selectedItem);//очистит и заполнит дерево, родительский раздел передается как параметр
            }


        });

        //откроем первую базу
        baseCombo.getSelectionModel().select(0);
        baseCombo.fireEvent(new ActionEvent());//создадим эвент для baseCombo.setOnAction и заполним комбобок тем самым
    }

    private void initSectionCombo() {
        sectionCombo.setConverter(new SectionConverter(getModel().getProgramLanguage().getAbbr()));//конвертер секции в строку
        sectionCombo.setVisibleRowCount(10);

        //выбор рездела. Заполнение дерева после выбора раздела
        sectionCombo.setOnAction(event ->
        {
            clearSearch(false,false);//очистка состояния поиска
            INamed value = rootItem.getValue();
            Section selectedItem = sectionCombo.getSelectionModel().getSelectedItem();
            programDescription.setText("");
            programInfo.setText("");
            fillTree(sectionCombo.getSelectionModel().getSelectedItem());//очистит и заполнит дерево, родительский раздел передается как параметр
        });

        sectionCombo.getSelectionModel().select(1);
        sectionCombo.getOnAction().handle(new ActionEvent());
    }

    private void fillSectionsSelectedBase() {
        sectionsBase.clear();
        sectionsBase.add(new Section());//пустой элемент вставим для выбора он с ID =0
        sectionsBase.addAll(getModel().findAllSectionByParent(baseCombo.getSelectionModel().getSelectedItem()));
        getModel().initStringsSection(sectionsBase);
        //очистка и заполение комбобокса разделов 2 уровня согласно выбранному 1 разделу
        sectionCombo.getItems().clear();
        sectionCombo.getItems().addAll(sectionsBase);
        rootItem.setValue(null);
        if(baseCombo.getSelectionModel().getSelectedIndex()<=1)sectionCombo.getSelectionModel().select(1);
        else sectionCombo.getSelectionModel().select(0);//автоматически очистит дерево, тк сработает sectionCombo.setOnAction(event....
    }

    private void initDeleteFromUserBaseMenu(ResourceBundle rb) {
        MenuItem mi1=new MenuItem(rb.getString("app.delete"));
        MenuItem mi2=new MenuItem(rb.getString("app.clear"));

        mi1.setOnAction(event2 -> onDeleteItm());
        mi2.setOnAction(event2 -> onClearItm());

        deleteMenu.getItems().addAll(mi1, mi2);

        delUserBtn.setOnAction(event3 ->
        {

            if (sectionTree.getSelectionModel().getSelectedItem() == null )
            {
                if(sectionCombo.getSelectionModel().getSelectedItem().getId()==0)return;
                else
                {
                    //это для выбранного меню разделов в комбо для секций
                    mi1.setDisable(false);
                    mi2.setDisable(true);

                    deleteMenu.show(delUserBtn, Side.BOTTOM, 0, 0);
                    return;

                }
            }
            mi1.setDisable(false);
            mi2.setDisable(false);


            if (sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Section)
            {
                //можно удалять разделы если в них все пустые разделы, но есть програмы и комплексы
                long count = sectionTree.getSelectionModel().getSelectedItem().getChildren().stream().filter(itm -> (itm.getValue() instanceof Section && !itm.getChildren().isEmpty())).count();
                if(count!=0)  mi1.setDisable(true);

                // if (!sectionTree.getSelectionModel().getSelectedItem().getChildren().isEmpty())
            } else if (sectionTree.getSelectionModel().getSelectedItem().getValue() instanceof Program)
                mi2.setDisable(true);


            deleteMenu.show(delUserBtn, Side.BOTTOM, 0, 0);
        });
    }

    /**
     * Заполнит дерево разделов
     * @param containerNode  Section  тот который хранит дерево элементов в базе. те начало отсчета выборки
     *
     */
    private void fillTree(Section containerNode)
    {

        if (containerNode==null || rootItem==null) {  /*System.out.println("FILL -containerNode==null || rootItem==null");*/return;}

        clearTree();
//если выбрали пустой элемент списка(фейковый) то не станем заполнять ничего
        if(containerNode.getId()==0) {
            sectionTree.setShowRoot(false);
            editUserBtn.setDisable(true);
            delUserBtn.setDisable(true);
            return;
        }
        editUserBtn.setDisable(false);
        delUserBtn.setDisable(false);

        //TimeMesure tm=new TimeMesure("Инициализация списка ");

        // tm.start();

        rootItem.setValue(containerNode);

        //загрузим разделы(все вложенные элементы будут грузиться автоматически, благодаря NamedTreeItem, который сам подгрузит доччерние элементы)
        List<Section> allSectionByParent = getModel().findAllSectionByParent(containerNode);
        getApp().getModel().initStringsSection(allSectionByParent);

        String lang=getModel().getProgramLanguage().getAbbr();
        if(!allSectionByParent.isEmpty())
        {
            lang = App.getStaticModel().getSmartLang(allSectionByParent.get(0).getName());
        }



        sectionTree.setShowRoot(true);
        allSectionByParent.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(section -> rootItem.getChildren().add(new NamedTreeItem(section)));
//загрузим прогрмы и комплексы корня.
        List<Complex> allComplexBySection = getModel().findAllComplexBySection(containerNode);
        List<Program> allProgramBySection = getModel().findAllProgramBySection(containerNode);

        if(!allComplexBySection.isEmpty())
        {
            getApp().getModel().initStringsComplex(allComplexBySection);//строки инициализируются тк у нас многоязыковое приложение. Тут инициализируются строки выбранной локали или в первую очередь пользовательские
            lang = App.getStaticModel().getSmartLang(allComplexBySection.get(0).getName());

            allComplexBySection.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(complex -> rootItem.getChildren().add(new NamedTreeItem(complex)));
        }

        if(!allProgramBySection.isEmpty())
        {
            getApp().getModel().initStringsProgram(allProgramBySection);
            lang = App.getStaticModel().getSmartLang(allProgramBySection.get(0).getName());
            allProgramBySection.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(program -> rootItem.getChildren().add(new NamedTreeItem(program)));
        }

        allSectionByParent.clear();allSectionByParent=null;
        allComplexBySection.clear();allComplexBySection=null;
        allProgramBySection.clear();allProgramBySection=null;

        //  tm.stop();



    }



    //очистка дерева( корень остается всегда)
    private void clearTree()
    {
        rootItem.setValue(null);
        rootItem.getChildren().forEach(this::removeRecursively);

        rootItem.getChildren().clear();

    }

    enum SearchActionType {IN_SELECTED_DEP,IN_SELECTED_BASE,IN_ALL_BASE};
    class FindFilter
    {

        String searchPattern;//строка которую ищем
        SearchActionType actionType;


        public FindFilter( SearchActionType actionType, String searchPattern) {
            this.actionType = actionType;
            this.searchPattern = searchPattern;
        }

        public SearchActionType getActionType() {
            return actionType;
        }







        public String getSearchPattern() {
            return searchPattern;
        }


    }



    private void fillTreeFind(FindFilter ff)
    {

        if(ff.searchPattern.length()<=2) { showInfoDialog(res.getString("app.search"),res.getString("app.search_1"),"",getApp().getMainWindow(),
                Modality.WINDOW_MODAL);return;}
        //необходимо сохранить раздел который открыт. Также заблокировать возможность удалять и добавлять и редактировать(стоит установить например старую базу по умолчанию) а может ничего менять не надо!!! Просто учесть режим поиска.
        //любой выбор в списке разделов  или баз отключает режим поиска!
        //нужно учесть состояние поиска чтобы во время него не искать где попало. Те после поиска мы можем продолжать искать исходя из старой ситуации.
        //стоит сделать так чтобы нажатие на ввод искало по текцщему выбору , а меню его меняло бы.

        //сохраним
        // searchState.setRoot(baseCombo.getValue());
        //searchState.setRoot2(sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());
        searchState.setSearch(true);
        searchState.setSearchText(ff.searchPattern);
        userActionPane.setDisable(true);

        List<Section> sections=null;
        List<Complex>  complexes=null;
        List<Program>  programs=null;
        switch (ff.actionType)
        {
            case IN_ALL_BASE:

                sections = getModel().searchSectionInAllBase(ff.searchPattern, getModel().getProgramLanguage());//поиск пользовательских данных в любом случае произойдет
                complexes= getModel().searchComplexInAllBase(ff.searchPattern, getModel().getProgramLanguage());//поиск пользовательских данных в любом случае произойдет
                programs= getModel().searchProgramInAllBase(ff.searchPattern, getModel().getProgramLanguage());//поиск пользовательских данных в любом случае произойдет
                break;
            case IN_SELECTED_BASE:
                sections = findSectionIn(ff.searchPattern, baseCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                complexes = findComplexIn(ff.searchPattern, baseCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                programs = findProgramIn(ff.searchPattern, baseCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                break;
            case IN_SELECTED_DEP:
                sections = findSectionIn(ff.searchPattern,sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                complexes = findComplexIn(ff.searchPattern, sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                programs = findProgramIn(ff.searchPattern,sectionCombo.getValue().getId().longValue() == 0 ? null : sectionCombo.getValue());//поиск пользовательских данных в любом случае произойдет
                break;

        }
        if(sections.isEmpty() && complexes.isEmpty() && programs.isEmpty()){
            showInfoDialog(res.getString("app.search_res"),res.getString("app.search_res_1"),"",
                    getApp().getMainWindow(),Modality.WINDOW_MODAL);

            //clearSearch(false,false);

            return;
        }

        clearTree();

        //заполним дерево данными
        Section section = new Section();
        section.setNameString("Результаты поиска");
        rootItem.setValue(section);

        getApp().getModel().initStringsSection(sections);

        sectionTree.setShowRoot(true);

        if(!sections.isEmpty())
        {
            //отфильтруем первые 2 уровня базы, чтобы их не искать и заполним дерево разделами
            sections.stream().filter(itm -> itm.getParent() != null && itm.getParent() != null ? itm.getParent().getParent() != null : false).forEach(itm -> rootItem.getChildren().add(new NamedTreeItem(itm)));

        }

        if(!complexes.isEmpty())
        {
            getApp().getModel().initStringsComplex(complexes);//строки инициализируются тк у нас многоязыковое приложение. Тут инициализируются строки выбранной локали или в первую очередь пользовательские
            complexes.forEach(complex -> rootItem.getChildren().add(new NamedTreeItem(complex)));
        }

        if(!programs.isEmpty())
        {
            getApp().getModel().initStringsProgram(programs);
            programs.forEach(program -> rootItem.getChildren().add(new NamedTreeItem(program)));
        }

        sections.clear();sections=null;
        complexes.clear();complexes=null;
        programs.clear();programs=null;

        //возможно стоит отфильтровывать программы которые родительские найденным разделам?? + возможно стоит отображать для программ комплексы и их разделы? но они автоматом загрузят все содержимое!! мо в конструкторе NamedTreeItem отключать это??
    }

    /**
     * Указынный элемент не удаляется, его если надо нужно удалить вручную из родительского контейнера, также не обнуляется его значение Value
     * рекурсивная очистка ссылок дерева, удаляются все дочерние элементы, начиная от указанного элемента(очищаются дочерние и их Value, также указанный элемент)
     * @param item
     */
    private void removeRecursively(TreeItem<INamed>  item) {

        if (!item.getChildren().isEmpty())
        {
            item.getChildren().forEach(itm -> {
                itm.setGraphic(null);//очистим ссылку на изображение
                itm.setValue(null);//очистим ссылку на Entity иначе утечка памяти
                removeRecursively(itm);
            });
            item.getChildren().clear();

        }

    }




    /**
     * Указынный элемент не удаляется, его если надо нужно удалить вручную из родительского контейнера, также не обнуляется его значение Value
     * рекурсивная очистка ссылок дерева, удаляются все дочерние элементы, начиная от указанного элемента(очищаются дочерние и их Value, также указанный элемент)
     * @param item
     */
    private void removeRecursively(TreeItem<INamed>  item, Predicate< TreeItem<INamed> > filter) {




        if (!item.getChildren().isEmpty())
        {
            item.getChildren().stream().filter(filter).forEach(itm -> {
                itm.setGraphic(null);//очистим ссылку на изображение
                itm.setValue(null);//очистим ссылку на Entity иначе утечка памяти
                removeRecursively(itm);
            });

            Iterator<TreeItem<INamed>> itr = item.getChildren().iterator();
            while(itr.hasNext()) if(filter.test(itr.next())) itr.remove();
            itr=null;



        }

    }
    /**
     * полное удаление элементов, включая тот что мы указали
     * @param item
     */
    private void clearTree(TreeItem<INamed>  item)
    {
        if(item==null) return;

        removeRecursively(item);
        item.setGraphic(null);//очистим ссылку на изображение
        item.setValue(null);//очистим ссылку на Entity иначе утечка памяти

        if(item.getParent()!=null)item.getParent().getChildren().remove(item);

    }

    /**
     * полное удаление элементов, включая тот что мы указали, предикат отфильтрует нужные дочерние
     * @param item
     * @param filter предикат, который отфильтрует элементы которые мы хотим удалить и очистить
     */
    private void clearTree(TreeItem<INamed>  item, Predicate<NamedTreeItem> filter)
    {
        if(item==null) return;

        removeRecursively(item);
        item.setGraphic(null);//очистим ссылку на изображение
        item.setValue(null);//очистим ссылку на Entity иначе утечка памяти

        if(item.getParent()!=null)item.getParent().getChildren().remove(item);

    }

    public void onSearchReturn(){
        clearSearch(true,true);

    }

    List<Section> findSectionIn(String text,Section sec)
    {
        if(sec==null) return new ArrayList<>();

        List<Section> sections =getModel().searchSectionInParent(text,getModel().getProgramLanguage(),sec);
        getModel().findAllSectionByParent(sec).forEach(section -> sections.addAll(findSectionIn(text, section)));
        return sections;
    }

    List<Complex> findComplexIn(String text,Section sec)
    {
        if(sec==null) return new ArrayList<>();

        List<Complex> complexes = getModel().searchComplexInParent(text, getModel().getProgramLanguage(), sec);

        getModel().findAllSectionByParent(sec).forEach(section -> complexes.addAll(findComplexIn(text, section)));
        return complexes;
    }
    List<Program> findProgramIn(String text,Section sec)
    {
        if(sec==null) return new ArrayList<>();

        List<Program> programms = getModel().searchProgramInParent(text, getModel().getProgramLanguage(), sec);
        getModel().findAllSectionByParent(sec).forEach(section -> programms.addAll(findProgramIn(text, section)));
        getModel().findAllComplexBySection(sec).forEach(complex ->  programms.addAll(getModel().searchProgramInComplex(text, getModel().getProgramLanguage(), complex)));
        return programms;
    }


    public void setTreeActionListener(TreeActionListener treeActionListener){
        this.treeActionListener = treeActionListener;
    }



    private void initSectionTree() {
        sectionTree.setShowRoot(false);
        sectionTree.setRoot(rootItem);
        rootItem.setExpanded(true);

        sectionTree.setCellFactory(param -> new SectionTreeCell());
        sectionTree.setOnMouseClicked(this::sectionTreeClickAction);
    }

    private void sectionTreeClickAction(MouseEvent event) {
        //по одиночному клику

        TreeItem<INamed> selectedItem = sectionTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        singleClickOnSectionTreeAction(selectedItem);
        if (event.getClickCount() == 2)doubleClickOnSectionTreeAction(selectedItem);
    }

    private void singleClickOnSectionTreeAction(TreeItem<INamed> selectedItem) {
        if (selectedItem.getValue() instanceof Program)
        {
            singleClickOnSectionTreeProgramItemAction(selectedItem);
        } else  if (selectedItem.getValue() instanceof Section)
        {
            singleClickOnSectionTreeSectionItemAction(selectedItem);
        } else  if (selectedItem.getValue() instanceof Complex)
        {
            singleClickOnSectionTreeComplexItemAction(selectedItem);
        }
    }

    private void doubleClickOnSectionTreeAction(TreeItem<INamed> selectedItem) {
            if(treeActionListener ==null) throw new RuntimeException("Не установлен treeActionListener");
        //перенос программы в текущий комплекс  в такблицу справа.
        if (selectedItem.getValue() instanceof Program)
        {
            treeActionListener.programItemDoubleClicked(selectedItem);
        } else if (selectedItem.getValue() instanceof Complex) {
            treeActionListener.complexItemDoubleClicked(selectedItem);
        }
    }





    private void singleClickOnSectionTreeComplexItemAction(TreeItem<INamed> selectedItem) {
        String pathtext="";

        if(((Complex) selectedItem.getValue()).getSection()!=null)
        {
            Section tS=  ((Complex) selectedItem.getValue()).getSection();
            getModel().initStringsSection(tS);
            pathtext=tS.getNameString();
            if(tS.getParent()!=null)
            {
                Section tS1= tS.getParent();
                getModel().initStringsSection(tS1);
                pathtext=tS1.getNameString()+" -> "+pathtext;
            }
        }
        if(!pathtext.isEmpty()) pathtext+=" -> "+selectedItem.getValue().getNameString(); else pathtext=selectedItem.getValue().getNameString();
        programDescription.setText(pathtext+"\n"+((IDescriptioned) selectedItem.getValue()).getDescriptionString());
        programInfo.setText("");
        createUserBtn.setDisable(false);
    }

    private void singleClickOnSectionTreeSectionItemAction(TreeItem<INamed> selectedItem) {
        String pathtext="";
        if(((Section) selectedItem.getValue()).getParent()!=null)
        {
            Section tS=  ((Section) selectedItem.getValue()).getParent();
            getModel().initStringsSection(tS);
            pathtext=tS.getNameString();

            if(tS.getParent()!=null)
            {
                Section tS1= tS.getParent();
                getModel().initStringsSection(tS1);
                pathtext=tS1.getNameString()+" -> "+pathtext;
            }
        }
        if(!pathtext.isEmpty()) pathtext+=" -> "+selectedItem.getValue().getNameString(); else pathtext=selectedItem.getValue().getNameString();
        programDescription.setText(pathtext+"\n"+((IDescriptioned) selectedItem.getValue()).getDescriptionString());
        programInfo.setText("");
        createUserBtn.setDisable(false);
    }

    private void singleClickOnSectionTreeProgramItemAction(TreeItem<INamed> selectedItem) {
        String pathtext="";
        if(((Program) selectedItem.getValue()).getComplex()!=null)
        {
            Complex tC=  ((Program) selectedItem.getValue()).getComplex();
            getModel().initStringsComplex(tC);
            pathtext=tC.getNameString();

            if(tC.getSection()!=null)
            {
                Section tS= tC.getSection();
                getModel().initStringsSection(tS);
                pathtext=tS.getNameString()+" -> "+pathtext;
            }
        }else if(((Program) selectedItem.getValue()).getSection()!=null)
        {
            Section tS=  ((Program) selectedItem.getValue()).getSection();
            getModel().initStringsSection(tS);
            pathtext=tS.getNameString();

            if(tS.getParent()!=null)
            {
                Section tS1= tS.getParent();
                getModel().initStringsSection(tS1);
                pathtext=tS1.getNameString()+" -> "+pathtext;
            }
        }

        if(!pathtext.isEmpty()) pathtext+=" -> "+selectedItem.getValue().getNameString(); else pathtext=selectedItem.getValue().getNameString();
        programDescription.setText(pathtext+"\n"+((Program) selectedItem.getValue()).getDescriptionString());
        programInfo.setText(((Program) selectedItem.getValue()).getFrequencies().replace(";", ";  "));
        createUserBtn.setDisable(true);
    }

    private void createSection(Section selectedSection,TreeItem<INamed> selectedItem)throws Exception
    {
        Section parent = null;
        if(selectedSection==null && selectedItem==null)parent = getModel().findAllSectionByTag("USER");
        else  if(selectedSection!=null && selectedItem==null)parent=selectedSection;
        else if(selectedSection!=null && selectedItem!=null)
        {
            if(selectedItem.getValue()==null)throw new Exception("selectedItem.getValue() == null");
            if(!(selectedItem.getValue() instanceof Section)) return;
            parent=(Section)selectedItem.getValue();
        }else return;


        //выведем диалог ввода данных
        NameDescroptionDialogController.Data data =null;
        try {
            data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title1"), false,
                    StageStyle.DECORATED, 0, 0, 0, 0, new NameDescroptionDialogController.Data("",""));


        } catch (IOException e) {
            log.error("",e);
            data =null;
        }

        if(data ==null){BaseController.showErrorDialog("Ошибка создания раздела", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}




        //проверим полученные данные из диалога, создали ли имя
        if( data.isNameChanged())
        {


            Section createdSection=null;
            try {
                createdSection= getModel().createSection(parent, data.getNewName(), data.getNewDescription(),false,getModel().getUserLanguage());

                createdSection.setNameString(data.getNewName());
                createdSection.setDescriptionString(data.getNewDescription());

                if(parent.getTag()!=null ? parent.getTag().equals("USER"): false)
                {
                    //добавим в комбобокс
                    sectionCombo.getItems().add(createdSection);
                    sectionCombo.getSelectionModel().selectLast();
                }
                else if( selectedItem==null)
                {
                    rootItem.getChildren().add(new NamedTreeItem(createdSection));//вставим в корен дерева, если не выбран элемент в дереве
                    sectionTree.getSelectionModel().select(rootItem.getChildren().get(rootItem.getChildren().size() - 1));//выделим добавленный пункт
                }
                else {
                    if(selectedItem.isLeaf())((NamedTreeItem)selectedItem).setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо вставлять их тут
                    else  selectedItem.getChildren().add(new NamedTreeItem(createdSection));//добавим в дерево, если унас уже есть дочернии в ветке
                    if(!selectedItem.isExpanded()) selectedItem.setExpanded(true);
                    if(selectedItem!=null)sectionTree.getSelectionModel().select(selectedItem.getChildren().get(selectedItem.getChildren().size()-1));//выделим добавленный пункт
                }



            } catch (Exception e) {
                data=null;
                log.error("",e);
                BaseController.showExceptionDialog("Ошибка создания раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
            }

        }

        data =null;//очистка ссылки


    }


    private void createComplex(  TreeItem<INamed> selectedItem) throws Exception
    {
        if(selectedItem==null) return;
        if(selectedItem.getValue()==null)throw new Exception("selectedItem.getValue() == null");

        if(selectedItem.getValue() instanceof Section)
        {

            //выведем диалог ввода данных
            NameDescroptionDialogController.Data data =null;
            try {
                data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title2"), false,
                        StageStyle.DECORATED, 0, 0, 0, 0, new NameDescroptionDialogController.Data("",""));


            } catch (IOException e) {
                log.error("",e);
                data =null;
            }

            if(data ==null){BaseController.showErrorDialog("Ошибка создания комплекса", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}

            //создаем комплекс
            if( data.isNameChanged() )
            {
                Complex complex=null;

                try {
                    complex= getModel().createComplex(data.getNewName(),data.getNewDescription(),(Section)selectedItem.getValue(),false,getModel().getUserLanguage());
                    complex.setNameString(data.getNewName());
                    complex.setDescriptionString(data.getNewDescription());

                    if(selectedItem.isLeaf())((NamedTreeItem)selectedItem).setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо вставлять их тут
                    else  selectedItem.getChildren().add(new NamedTreeItem(complex));//добавим в дерево, если унас уже есть дочернии в ветке
                    if(!selectedItem.isExpanded()) selectedItem.setExpanded(true);
                    sectionTree.getSelectionModel().select(selectedItem.getChildren().get(selectedItem.getChildren().size()-1));//выделим добавленный пункт


                } catch (Exception e) {
                    data=null;
                    log.error("",e);
                    BaseController.showExceptionDialog("Ошибка создания комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);return;
                }


            }




            data=null;
        }

    }

    private void createProgram(  TreeItem<INamed> selectedItem) throws Exception {

        if(selectedItem==null) return;
        if(selectedItem.getValue()==null)throw new Exception("selectedItem.getValue() == null");

        if(selectedItem.getValue() instanceof Section || selectedItem.getValue() instanceof Complex )
        {

            //выведем диалог ввода данных
            ProgramDialogController.Data data =null;
            try {
                data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/ProgramDialog.fxml", res.getString("app.title3"), false,
                        StageStyle.DECORATED, 0, 0, 0, 0, new ProgramDialogController.Data("","",""));


            } catch (IOException e) {
                log.error("",e);
                data =null;
            }

            if(data ==null){BaseController.showErrorDialog("Ошибка создания програмы", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}

            //создаем комплекс
            if(data.isNameChanged())
            {
                Program program=null;

                try {
                    if(selectedItem.getValue() instanceof Section) program= getModel().createProgram(data.getNewName(),data.getNewDescription(),data.getNewFreq(),(Section)selectedItem.getValue(),false,getModel().getUserLanguage());
                    else program= getModel().createProgram(data.getNewName(),data.getNewDescription()
                            ,data.getNewFreq(),(Complex)selectedItem.getValue(),false,getModel().getUserLanguage());


                    program.setNameString(data.getNewName());
                    program.setDescriptionString(data.getNewDescription());

                    if(selectedItem.isLeaf())((NamedTreeItem)selectedItem).setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо вставлять их тут
                    else  selectedItem.getChildren().add(new NamedTreeItem(program));//добавим в дерево, если унас уже есть дочернии в ветке
                    if(!selectedItem.isExpanded()) selectedItem.setExpanded(true);



                } catch (Exception e) {
                    data=null;
                    log.error("",e);
                    BaseController.showExceptionDialog("Ошибка создания комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);return;
                }


            }




            data=null;
        }

    }


    public void onCreateUserBtn()
    {

        TreeItem<INamed> selectedItem=null;
        Section selectedSection =null;


        //если у нас выбран пустой пункт, то мы должны иметь selectedItem ==null и selectedSection ==null иначе они должны иметь значения, но  selectedItem может быть null
        selectedSection = sectionCombo.getSelectionModel().getSelectedItem();
        if(selectedSection.getId()!=0) selectedItem = sectionTree.getSelectionModel().getSelectedItem();
        else selectedSection =null;




        if(!baseCombo.getSelectionModel().getSelectedItem().getTag().equals("USER")) return;//чтобы случайно активированная кнопка не позволила создать раздел, проверим какая бааза выбрана(корневой раздел Section)


        try {


            //создание корневого раздела если у нас юзерская база и не выбран ее раздел
            if(selectedSection==null)createSection(null,null);
            else //если выбран раздел, но не выбран подраздел в дереве разделов, создадим раздел уже в дереве
                if(selectedSection!=null && selectedItem==null) createSection(selectedSection,null);



                else if(selectedSection!=null && selectedItem!=null)
                {
                    //создание подраздела в подразделе дерева или комплекса
                    if(selectedItem.getValue() instanceof Section)
                    {

                        //дадим пользователю выбор
                        String choice = BaseController.showChoiceDialog(res.getString("ui.msg.create_section_or_complex"), "", res.getString("ui.msg.section_or_ui_content"), Arrays.asList(res.getString("ui.section"), res.getString("ui.complex"),res.getString("ui.program")),res.getString("ui.section"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        if(choice==null) return;

                        if(choice.equals(res.getString("ui.section"))) createSection(selectedSection, selectedItem);//создание раздела
                        else if(choice.equals(res.getString("ui.complex"))) createComplex(selectedItem);//создание компелекса
                        else  createProgram(selectedItem);//создание программы в разделе
                    }
                    else   if(selectedItem.getValue() instanceof Complex) createProgram(selectedItem);


                }
        }catch (Exception e)
        {
            log.error("",e);
            BaseController.showExceptionDialog("Ошибка создания элемента", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);return;
        }

    }



    public void onEditUserBtn()
    {
        NamedTreeItem selectedTreeItem = (NamedTreeItem)sectionTree.getSelectionModel().getSelectedItem();
        Section selectedComboItem = sectionCombo.getSelectionModel().getSelectedItem();


        if( selectedComboItem.getId()==0) return;//выбран пустой элемент в комбобоксе
        else
            //выбран элемент дерева
            if(selectedTreeItem!=null)
            {
                if(selectedTreeItem.getValue() instanceof Section)
                {
                    //выведем диалог ввода данных
                    NameDescroptionDialogController.Data data = new NameDescroptionDialogController.Data(selectedTreeItem.getValue().getNameString(),((Section)selectedTreeItem.getValue()).getDescriptionString());


                    try {
                        data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title4"), false,
                                StageStyle.DECORATED, 0, 0, 0, 0, data);


                    } catch (IOException e) {
                        log.error("",e);
                        data =null;
                    }

                    if(data ==null){ BaseController.showErrorDialog("Ошибка редакетирования раздела", " data==null", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}


                    if(data.isChanged())
                    {

                        try {

                            selectedTreeItem.getValue().setNameString(data.getNewName());
                            ((Section)selectedTreeItem.getValue()).setDescriptionString(data.getNewDescription());

                            LocalizedString localStringName = getModel().getLocalString( ((Section) selectedTreeItem.getValue()).getName(), getModel().getUserLanguage());
                            localStringName.setContent(data.getNewName());
                            getModel().updateLocalString(localStringName);

                            LocalizedString localStringDesc = getModel().getLocalString(((Section) selectedTreeItem.getValue()).getDescription(), getModel().getUserLanguage());
                            localStringDesc.setContent(data.getNewDescription());
                            getModel().updateLocalString(localStringDesc);

                            if(selectedTreeItem.getParent()==null)//мы выбрали корень дерева. Изменим элемент и отображение в комбо и в дереве иначе только в дереве
                            {
                                int i = sectionCombo.getSelectionModel().getSelectedIndex();
                                ObservableList<Section> items = sectionCombo.getItems();
                                sectionCombo.setItems(null);
                                sectionCombo.setItems(items);
                                sectionCombo.getSelectionModel().select(i);
                                items=null;

                                INamed value = selectedTreeItem.getValue();
                                selectedTreeItem.setValue(null);
                                selectedTreeItem.setValue(value);
                                value=null;

                            }else
                            {
                                INamed value = selectedTreeItem.getValue();
                                selectedTreeItem.setValue(null);
                                selectedTreeItem.setValue(value);
                                value=null;

                            }

                            programDescription.setText(data.getNewDescription());
                            programInfo.setText("");
                            data=null;
                        } catch (Exception e) {
                            data=null;
                            log.error("",e);
                            BaseController.showExceptionDialog("Ошибка редакетирования раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                            return;
                        }



                    }


                }else
                if(selectedTreeItem.getValue() instanceof Complex)
                {
                    //выведем диалог ввода данных
                    NameDescroptionDialogController.Data data = new NameDescroptionDialogController.Data(selectedTreeItem.getValue().getNameString(),((Complex)selectedTreeItem.getValue()).getDescriptionString());


                    try {
                        data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title5"), false,
                                StageStyle.DECORATED, 0, 0, 0, 0, data);


                    } catch (IOException e) {
                        log.error("",e);
                        data =null;
                    }

                    if(data ==null){ BaseController.showErrorDialog("Ошибка редакетирования комплекса", " data==null", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}



                    if(data.isChanged())
                    {

                        try {

                            selectedTreeItem.getValue().setNameString(data.getNewName());
                            ((Complex)selectedTreeItem.getValue()).setDescriptionString(data.getNewDescription());

                            LocalizedString localStringName = getModel().getLocalString(((Complex) selectedTreeItem.getValue()).getName(), getModel().getUserLanguage());
                            localStringName.setContent(data.getNewName());
                            getModel().updateLocalString(localStringName);

                            LocalizedString localStringDesc = getModel().getLocalString(((Complex) selectedTreeItem.getValue()).getDescription(), getModel().getUserLanguage());
                            localStringDesc.setContent(data.getNewDescription());
                            getModel().updateLocalString(localStringDesc);


                            INamed value = selectedTreeItem.getValue();
                            selectedTreeItem.setValue(null);
                            selectedTreeItem.setValue(value);
                            value=null;


                            programDescription.setText(data.getNewDescription());
                            programInfo.setText("");

                            data=null;


                        } catch (Exception e) {
                            data=null;
                            log.error("",e);
                            BaseController.showExceptionDialog("Ошибка редакетирования комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                            return;
                        }



                    }


                }else
                if(selectedTreeItem.getValue() instanceof Program)
                {

                    ProgramDialogController.Data data = new ProgramDialogController.Data(selectedTreeItem.getValue().getNameString(),((Program)selectedTreeItem.getValue()).getDescriptionString(),((Program)selectedTreeItem.getValue()).getFrequencies());


                    try {
                        data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/ProgramDialog.fxml", res.getString("app.title6"), false,
                                StageStyle.DECORATED, 0, 0, 0, 0, data);


                    } catch (IOException e) {
                        log.error("",e);
                        data =null;
                    }

                    if(data ==null){ BaseController.showErrorDialog("Ошибка редакетирования програмы", " data==null", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}



                    if(data.isChanged())
                    {

                        try {

                            selectedTreeItem.getValue().setNameString(data.getNewName());
                            ((Program)selectedTreeItem.getValue()).setDescriptionString(data.getNewDescription());
                            ((Program)selectedTreeItem.getValue()).setFrequencies(data.getNewFreq());

                            LocalizedString localStringName = getModel().getLocalString( ((Program) selectedTreeItem.getValue()).getName(), getModel().getUserLanguage());
                            localStringName.setContent(data.getNewName());
                            getModel().updateLocalString(localStringName);

                            LocalizedString localStringDesc = getModel().getLocalString(((Program) selectedTreeItem.getValue()).getDescription(), getModel().getUserLanguage());
                            localStringDesc.setContent(data.getNewDescription());
                            getModel().updateLocalString(localStringDesc);

                            if(data.isFreqChanged())getModel().updateProgram((Program)selectedTreeItem.getValue());//если изменились частоты то сохраним прогрму иначе только строки


                            INamed value = selectedTreeItem.getValue();
                            selectedTreeItem.setValue(null);
                            selectedTreeItem.setValue(value);
                            value=null;


                            programDescription.setText(data.getNewDescription());
                            programInfo.setText(data.getNewFreq().replace(";",";  "));
                            data=null;

                        } catch (Exception e) {
                            data=null;
                            log.error("",e);
                            BaseController.showExceptionDialog("Ошибка редакетирования програмы", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                            return;
                        }



                    }






                }
            }else
            {
                //выбран раздел 2 уровня в комбобокс, но не в дереве

                //выведем диалог ввода данных
                NameDescroptionDialogController.Data data = new NameDescroptionDialogController.Data(selectedComboItem.getNameString(),selectedComboItem.getDescriptionString());


                try {
                    data = BaseController.openDialogUserData(getApp().getMainWindow(), "/fxml/SectionDialog.fxml", res.getString("app.title7"), false,
                            StageStyle.DECORATED, 0, 0, 0, 0, data);


                } catch (IOException e) {
                    log.error("",e);
                    data =null;
                }

                if(data ==null){BaseController.showErrorDialog("Ошибка редакетирования раздела", "", "",  getApp().getMainWindow(),Modality.WINDOW_MODAL);return;}


                if(data.isChanged() && data.isNameChanged())
                {
                    try {

                        selectedComboItem.setNameString(data.getNewName());
                        selectedComboItem.setDescriptionString(data.getNewDescription());

                        LocalizedString localStringName = getModel().getLocalString(selectedComboItem.getName(), getModel().getUserLanguage());
                        localStringName.setContent(data.getNewName());
                        getModel().updateLocalString(localStringName);

                        LocalizedString localStringDesc = getModel().getLocalString(selectedComboItem.getDescription(), getModel().getUserLanguage());
                        localStringDesc.setContent(data.getNewDescription());
                        getModel().updateLocalString(localStringDesc);


                        int i = sectionCombo.getSelectionModel().getSelectedIndex();
                        ObservableList<Section> items = sectionCombo.getItems();
                        sectionCombo.setItems(null);
                        sectionCombo.setItems(items);
                        sectionCombo.getSelectionModel().select(i);
                        items=null;
                        INamed value = rootItem.getValue();
                        rootItem.setValue(null);
                        rootItem.setValue(value);
                        value=null;




                        data=null;
                    } catch (Exception e) {
                        data=null;
                        log.error("",e);
                        BaseController.showExceptionDialog("Ошибка редакетирования раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        return;
                    }


                }

            }


    }

    public void onDeleteItm()
    {

        TreeItem<INamed> itemSelected=sectionTree.getSelectionModel().getSelectedItem();
        Section comboSelected=sectionCombo.getSelectionModel().getSelectedItem();

        if (itemSelected == null )
        {
            if(comboSelected.getId()==0)return;
            else
            {
                //это для выбранного меню разделов в комбо для секций. Если только оно выбрано

                if(getModel().countSectionChildren(comboSelected)!=0)
                {
                    showInfoDialogNoHeader(res.getString("app.title8"),res.getString("app.title9"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
                    return;
                }

                try {
                    getModel().removeSection(comboSelected);
                    sectionCombo.getItems().remove(comboSelected);
                    sectionCombo.getSelectionModel().select(0);

                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка удаления раздела", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }


                return;
            }
        }







        if(itemSelected==null ? true :itemSelected.getValue()==null) return ;

        if(itemSelected.getValue() instanceof Program)
        {

            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title10"), "", res.getString("app.title11"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {
                    getModel().removeProgram((Program) itemSelected.getValue());
                    clearTree(itemSelected);

                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка удаления програмы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }


        }else if(itemSelected.getValue() instanceof Complex)
        {

            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title12"), "", res.getString("app.title13"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {
                    getModel().removeComplex((Complex) itemSelected.getValue());

                    clearTree(itemSelected);


                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка удаления комплекса","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }


        }else  if(itemSelected.getValue() instanceof Section)
        {

            //если есть непустые разделы то не станем удалять
            long count = itemSelected.getChildren().stream().filter(itm -> (itm.getValue() instanceof Section && !itm.getChildren().isEmpty())).count();
            if(count!=0) return;
            // if(!itemSelected.getChildren().isEmpty()) return;

            //здесь у нас пустой раздел. Мы можем его просто удалить

            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title14"), "", res.getString("app.title15"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {





                try {

                    //удалим пустые разделы и комплексы с программами
                    for (TreeItem<INamed> item : itemSelected.getChildren()) {

                        if(item.getValue() instanceof Program) getModel().removeProgram((Program)item.getValue());
                        else if(item.getValue() instanceof Complex)  getModel().removeComplex((Complex)item.getValue());
                        else if(item.getValue() instanceof Section && item.getChildren().isEmpty()) getModel().removeSection((Section)item.getValue());
                    }


                    getModel().removeSection((Section) itemSelected.getValue());
                    if(itemSelected.getParent()==null)
                    {
                        //это корень. Удалим из комбо, активируем пустой элемент. дерево замо очистится
                        sectionCombo.getItems().remove((Section) itemSelected.getValue());
                        sectionCombo.getSelectionModel().select(0);

                    }else  clearTree(itemSelected);//если удалили элемент дерева, то очистим нижлежайшие ветви




                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка удаления раздела","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }


        }

    }

    public void onClearItm()
    {
        TreeItem<INamed> itemSelected=sectionTree.getSelectionModel().getSelectedItem();



        if(itemSelected==null ? true :itemSelected.getValue()==null) return ;

        if(itemSelected.getValue() instanceof Section)
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title16"), "", res.getString("app.title17"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {
                    getModel().clearSection((Section) itemSelected.getValue());

                    removeRecursively(itemSelected, item -> !(item.getValue() instanceof Section));//очистим все кроме разделов



                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка удаления раздела","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
                }

            }

        }else  if(itemSelected.getValue() instanceof Complex)
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title18"), "", res.getString("app.title19"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType: false)
            {
                try {

                    for (TreeItem<INamed> item : itemSelected.getChildren())
                    {
                        getModel().removeProgram((Program) item.getValue());

                    }
                    removeRecursively(itemSelected);



                } catch (Exception e) {
                    log.error("",e);
                    showExceptionDialog("Ошибка очистки комплекса", "", "", e, getApp().getMainWindow(), Modality.WINDOW_MODAL);
                }

            }

        }


    }

    /**** API ***/


    /**
     * Вставка  комплекса в пользовательскую базу из папки - из сгенерированных для M файлов
     * @param dir
     * @param treeItem выбранный элемент дерева - должен быть разделом или
     *                 @param createComplex создать комплекс и вставить в него програмы или просто вставить в раздел
     * @return
     */
    @Override
    public boolean loadComplexToBase(File dir, NamedTreeItem treeItem, boolean createComplex)
    {
        Map<Long, ProgramFileData> programms= FilesProfileHelper.getProgrammsFromComplexDir(dir);

        try {
            if (programms!=null)
            {


                String name = dir.getName();
                int ind = name.indexOf('-');
                if (ind != -1) name = name.substring(ind + 1);

                ind = name.indexOf('(');
                if (ind != -1) name = name.substring(0,ind);
                name= name.trim();

                if(programms.isEmpty())
                {
                    return false;

                }else
                {

                    if(createComplex)
                    {
                        Complex complex = getModel().createComplex(name, "", (Section) treeItem.getValue(), false, getModel().getUserLanguage());
                        getModel().initStringsComplex(complex);

                        for (Map.Entry<Long, ProgramFileData> entry : programms.entrySet()) {

                            if(entry.getValue().isMp3())continue;//пропустим мп3
                            getModel().createProgram(entry.getValue().getName(), "", entry.getValue().getFreqs(), complex, false, getModel().getUserLanguage());

                        }


                        if (treeItem.isLeaf())treeItem.setLeafNode(false);//установим что он теперь не лист., автоматом подгрузятся дочернии, поэтому не надо ставлять их тут
                        else
                            treeItem.getChildren().add(new NamedTreeItem(complex));//добавим в дерево, если унас уже есть дочернии в ветке
                        if (!treeItem.isExpanded()) treeItem.setExpanded(true);
                        if (treeItem != null)
                            sectionTree.getSelectionModel().select(treeItem.getChildren().get(treeItem.getChildren().size() - 1));//выделим
                    }else
                    {


                        Program p;
                        for (Map.Entry<Long, ProgramFileData> entry : programms.entrySet())
                        {
                            if(entry.getValue().isMp3())continue;//пропустим мп3
                            p =  getModel().createProgram(entry.getValue().getName(), "", entry.getValue().getFreqs(),
                                    (Section)treeItem.getValue(), false, getModel().getUserLanguage());

                            if (!treeItem.isLeaf())  {getModel().initStringsProgram(p); treeItem.getChildren().add(new NamedTreeItem(p));}

                        }
                        boolean isleaf=treeItem.isLeaf();
                        if (treeItem.isLeaf())treeItem.setLeafNode(false);
                        if (!treeItem.isExpanded()) treeItem.setExpanded(true);
                        //  if (treeItem != null && !isleaf) sectionTree.getSelectionModel().select(treeItem.getChildren().get(treeItem.getChildren().size() - 1));//выделим
                    }
                    // добавленный пункт
                }




            }

        }catch (Exception e){ log.error("",e);return false;}

        if(programms==null) return false;
        else return true;

    }

    @Override
    public boolean isInUserBaseSectionSelected() {
        if (selectedBase().getTag() != null ? selectedBase().getTag().equals("USER") : false) {

            if (selectedSectionTreeItem() == null) return false;
            else if (selectedSectionTreeItem() instanceof Section)   return true;
            else return false;
        }else  return false;
    }
    @Override
    public boolean isInUserBaseComplexSelected() {
        if (selectedBase().getTag() != null ? selectedBase().getTag().equals("USER") : false) {

            if (selectedSectionTreeItem() == null) return false;
            else if (selectedSectionTreeItem() instanceof Complex)   return true;
            else return false;
        }else  return false;
    }

    @Override
    public Section selectedBase() {
        return baseCombo.getSelectionModel().getSelectedItem();
    }

    @Override
    public int selectedBaseIndex() {
        return baseCombo.getSelectionModel().getSelectedIndex();
    }

    @Override
    public ObservableList<Section> getBaseAllItems() {
        return baseCombo.getItems();
    }

    @Override
    public void selectBase(int index) {
        baseCombo.getSelectionModel().select(index);
    }

    @Override
    public void selectBase(Section section) {
        baseCombo.getSelectionModel().select(section);
    }

    @Override
    public Section selectedRootSection() {
        return sectionCombo.getSelectionModel().getSelectedItem();
    }

    @Override
    public int selectedRootSectionIndex() {
        return sectionCombo.getSelectionModel().getSelectedIndex();
    }

    @Override
    public ObservableList<Section> getRootSectionAllItems() {
        return sectionCombo.getItems();
    }

    @Override
    public void selectRootSection(int index) {
        sectionCombo.getSelectionModel().select(index);
    }

    @Override
    public void selectRootSection(Section section) {
        sectionCombo.getSelectionModel().select(section);
    }

    @Override
    public INamed selectedSectionTreeItem() {
        return selectedSectionTree()==null ? null : selectedSectionTree().getValue();
    }

    @Override
    public void addTreeItemToTreeRoot(NamedTreeItem item) {
        rootItem.getChildren().add(item);
    }

    @Override
    public void addTreeItemToSelected(NamedTreeItem item) {
        if(selectedSectionTree()==null) return;
        selectedSectionTree().getChildren().add(item);
    }

    @Override
    public NamedTreeItem selectedSectionTree() {
        return (NamedTreeItem)sectionTree.getSelectionModel().getSelectedItem();
    }

    @Override
    public void exportUserBase()
    {
        Section start=null;

        List<Section> collect = getBaseAllItems().stream().filter(section -> "USER".equals(section.getTag())).collect(
                Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;



        //если у не выбран раздел пользовательский
        if(!"USER".equals(selectedBase().getTag()) )
        {
            Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title20"), "", res.getString("app.title21"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(buttonType.isPresent() ? buttonType.get()==okButtonType :false) start= userSection; else return;

        }else
        {
            //выбрана пользовательская база.

            //не выбран раздел в комбобоксе
            if(selectedRootSection().getId()==0)
            {
                Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.title22"), "", res.getString("app.title23"), getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(buttonType.isPresent() ? buttonType.get()==okButtonType :false) start= userSection; else return;

            }else if(selectedSectionTree()==null)
            {
                //выбран раздел в комбобоксе но не выбран в дереве
                start=selectedRootSection();
            }else
            {
                //выбран элемент дерева и выбран раздел

                //если выбран не раздел
                if(!(selectedSectionTreeItem() instanceof Section))
                {

                    showWarningDialog(res.getString("app.title24"),"",res.getString("app.title25"),getApp().getMainWindow(),Modality.WINDOW_MODAL );
                    return;


                }

                start=(Section)selectedSectionTreeItem();//выберем стартовым раздел
            }


        }


        //получим путь к файлу.
        File file=null;

        getModel().initStringsSection(start);
        FileChooser fileChooser =new FileChooser();
        if("USER".equals(start.getTag()))  fileChooser.setTitle(res.getString("app.title26"));
        else fileChooser.setTitle(res.getString("app.title27")+" - " + start.getNameString());
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlb", "*.xmlb"));
        fileChooser.setInitialFileName("ubase.xmlb");
        file= fileChooser.showSaveDialog(getApp().getMainWindow());

        if(file==null)return;
        getModel().setLastExportPath(file.getParentFile());

        // запишем файл экспорта

        final Section sec=start;
        final File fileToSave=file;


        getProgressAPI().setProgressIndicator(res.getString("app.title28"));
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                boolean res= ExportUserBase.export(sec, fileToSave, getModel());
                if(res==false) {this.failed();return false;}
                else return true;

            }
        };


        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(-1.0, res.getString("app.title29")));
        task.setOnSucceeded(event ->
        {
            if (task.getValue()) getProgressAPI().setProgressIndicator(1.0, res.getString("app.title30"));
            else getProgressAPI().setProgressIndicator( res.getString("app.title31"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(res.getString("app.title31"));
            getProgressAPI().hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();
    }


    /**
     * пользователь может не выбрать раздел ни в выборе базы, ни в выборе разделов 2 уровня ни в дереве. Также можно выбрать любой раздел.
     * В нем создастся контейнер
     */
    @Override
    public void importUserBase()
    {

        Section start=null;
        String res="";

        List<Section> collect = getBaseAllItems().stream().filter(section -> "USER".equals(section.getTag())).collect(Collectors.toList());
        Section userSection=null;
        if(collect.isEmpty()) return;
        userSection=collect.get(0);
        collect=null;


        if(!"USER".equals(selectedBase().getTag()) )
        {
            //не выбран пользовательский раздел
            res =  showTextInputDialog(this.res.getString("app.title47"), "", this.res.getString("app.title48"),"", getApp().getMainWindow(), Modality.WINDOW_MODAL);

            if(res==null ? false: !res.isEmpty()) start=userSection;
            else return;


        }else
        {
            if(selectedRootSection().getId()==0)
            {
                //не выбран пользовательский раздел
                res =  showTextInputDialog(this.res.getString("app.title49"), "", this.res.getString("app.title50"),"", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty()) start=userSection;
                else return;

            }else if(selectedSectionTree()==null)
            {
                //выбран раздел в комбобоксе но не выбран в дереве


                res =  showTextInputDialog(this.res.getString("app.title51"), "", this.res.getString("app.title52"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty()) start=selectedRootSection();
                else return;

            }else
            {
                //выбран элемент дерева и выбран раздел

                //если выбран не раздел
                if(!(selectedSectionTreeItem() instanceof Section))
                {

                    showWarningDialog(this.res.getString("app.title49"),"",this.res.getString("app.title53"),getApp().getMainWindow(),Modality.WINDOW_MODAL );
                    return;


                }

                res =  showTextInputDialog(this.res.getString("app.title49"), "", this.res.getString("app.title54"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);

                if(res==null ? false: !res.isEmpty())  start=(Section)selectedRootSection();//выберем стартовым раздел
                else return;

            }

        }

        if(res==null ? true: res.isEmpty())
        {

            showWarningDialog( this.res.getString("app.title55"),"", this.res.getString("app.title56"),getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;
        }

        //теперь можновыбрать файл

        //получим путь к файлу.
        File file=null;
        getModel().initStringsSection(start);
        FileChooser fileChooser =new FileChooser();
        if("USER".equals(start.getTag()))  fileChooser.setTitle(this.res.getString("app.title57"));
        else fileChooser.setTitle(this.res.getString("app.title58")+" - " + start.getNameString()+".  "+this.res.getString("app.title59"));
        fileChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xmlb", "*.xmlb"));
        file= fileChooser.showOpenDialog(getApp().getMainWindow());

        if(file==null)return;



        ImportUserBase imp=new ImportUserBase();



        Section container=null;
        final Section startFinal=start;
        try {
            container = getModel().createSection(start, res, "", false, getModel().getUserLanguage());
            container.setDescriptionString("");
            container.setNameString(res);
        } catch (Exception e) {
            log.error("",e);

            showExceptionDialog("Ошибка создания раздела контейнера для импорта базы","","",e,getApp().getMainWindow(),Modality.WINDOW_MODAL);
            return;

        }

        final Section sec=container;
        final File fileToSave=file;
        final String resName=res;

        final ResourceBundle rest=this.res;
        Task<Boolean> task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception
            {

                imp.setListener(new ImportUserBase.Listener() {
                    @Override
                    public void onStartParse() {
                        updateProgress(10, 100);
                    }

                    @Override
                    public void onEndParse() {
                        updateProgress(30, 100);
                    }

                    @Override
                    public void onStartAnalize() {
                        updateProgress(35, 100);
                    }

                    @Override
                    public void onEndAnalize() {
                        updateProgress(50, 100);
                    }

                    @Override
                    public void onStartImport() {
                        updateProgress(55, 100);
                    }

                    @Override
                    public void onEndImport() {
                        updateProgress(90, 100);
                    }

                    @Override
                    public void onSuccess() {
                        updateProgress(98, 100);
                    }

                    @Override
                    public void onError(boolean fileTypeMissMatch) {
                        imp.setListener(null);

                        if (fileTypeMissMatch) {
                            showErrorDialog(rest.getString("app.title41"), "", rest.getString("app.title42"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
                        }
                        failed();

                    }
                });


                boolean res= imp.parse( fileToSave, getModel(),sec);
                if(res==false)
                {
                    imp.setListener(null);
                    this.failed();
                    return false;}
                else {
                    imp.setListener(null);
                    return true;
                }



            }


        };


        Section sect=container;
        task.progressProperty().addListener((observable, oldValue, newValue) -> getProgressAPI().setProgressIndicator(newValue.doubleValue()));
        task.setOnRunning(event1 -> getProgressAPI().setProgressIndicator(0.0, rest.getString("app.title43")));

        task.setOnSucceeded(event ->
        {

            if (task.getValue())
            {
                getProgressAPI().setProgressIndicator(1.0, rest.getString("app.title44"));

                //хаполнить структуру дерева и комбо.

                ///вопрос - если выбрана база не пользовательскаяч, нужно во всех случаях проверить что выбрано у нас.!!!!!!!!

                if(startFinal.getParent()==null && "USER".equals(startFinal.getTag()))
                {

                    if("USER".equals(selectedBase().getTag()))
                    {
                        //в момент выборы была открыта пользовательская база
                        //если у нас контейнер создан в корне пользовательской базы.
                        getRootSectionAllItems().add(sect);
                        selectRootSection(getRootSectionAllItems().indexOf(sect));
                    }
                    //если не в пользовательской базе то ничего не делаем

                }
                else {

                    //если внутри пользовательской базы то меняем, иначе ничего не делаем
                    if ("USER".equals(selectedBase().getTag()))
                    {
                        //иначе контейнер создан в дереве
                        if (selectedSectionTree() == null && selectedRootSection().getId() != 0) {
                            //выбран раздел в комбо но не в дереве
                            addTreeItemToTreeRoot(new NamedTreeItem(sect));

                        } else if (selectedSectionTree()!= null) {
                            //выбран раздел в дереве
                            addTreeItemToSelected(new NamedTreeItem(sect));

                        } else
                            showErrorDialog(rest.getString("app.title60"), rest.getString("app.title61"), "", getApp().getMainWindow(), Modality.WINDOW_MODAL);
                    }
                }

            }
            else getProgressAPI().setProgressIndicator(rest.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);
        });

        task.setOnFailed(event -> {
            getProgressAPI().setProgressIndicator(rest.getString("app.title45"));
            getProgressAPI().hideProgressIndicator(true);

            try {
                getApp().getModel().removeSection(sect);
            } catch (Exception e) {
                log.error("",e);
            }

        });

        Thread threadTask=new Thread(task);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressIndicator(0.01, rest.getString("app.title46"));
        threadTask.start();

    }

    /**
     * Импорт терапевтического комплекса в базу частот
     */
    @Override
    public void  importComplexToBaseFromDir()
    {

        DirectoryChooser dirChooser =new DirectoryChooser();
        dirChooser.setTitle(res.getString("app.menu.read_complex_from_dir"));

        dirChooser.setInitialDirectory(new File(getModel().getLastExportPath(System.getProperty("user.home"))));

        File dir= dirChooser.showDialog(getApp().getMainWindow());

        if(dir==null)return;

        boolean createComplex=true;

        Optional<ButtonType> buttonType = showConfirmationDialog(res.getString("app.ui.import_compl_to_base"), "", res.getString("app.ui.import_compl_to_base_q"), getApp().getMainWindow(), Modality.WINDOW_MODAL);
        if(buttonType.isPresent())
        {
            if(buttonType.get()==okButtonType) createComplex=true;
            else  if(buttonType.get()==noButtonType) createComplex=false;
            else return;
        }else return;

        NamedTreeItem treeSelected = selectedSectionTree();
        if(treeSelected==null) return;
        if(!(treeSelected.getValue() instanceof Section)) return;

        Task<Boolean> task=null;
        boolean createComplex2=createComplex;

        //новый профиль
        task =new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                boolean r= loadComplexToBase(dir, treeSelected,createComplex2);
                if(r==false)failed();
                return r;
            }
        };
        Task<Boolean> task2=task;
        task2.setOnRunning(event1 -> getProgressAPI().setProgressBar(0.0, res.getString("app.title102"), ""));

        task2.setOnSucceeded(event ->
        {
            Waiter.closeLayer();
            if (task2.getValue()) {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(1.0, res.getString("app.title103"));

                Profile selectedItem =  ProfileTable.getInstance().getSelectedItem();
                if(selectedItem!=null)   Platform.runLater(() -> {
                    ProfileTable.getInstance().clearSelection();
                    ProfileTable.getInstance().select(selectedItem);
                });
            } else {
                getProgressAPI().hideProgressBar(false);
                getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            }
            getProgressAPI().hideProgressIndicator(true);
        });

        task2.setOnFailed(event -> {
            Waiter.closeLayer();
            getProgressAPI().hideProgressBar(false);
            getProgressAPI().setProgressIndicator(res.getString("app.title93"));
            getProgressAPI().hideProgressIndicator(true);
        });

        Thread threadTask=new Thread(task2);
        threadTask.setDaemon(true);
        getProgressAPI().setProgressBar(0.01, res.getString("app.title102"), "");
        Waiter.openLayer(getApp().getMainWindow(),false);
        threadTask.start();
        Waiter.show();
    }


}
