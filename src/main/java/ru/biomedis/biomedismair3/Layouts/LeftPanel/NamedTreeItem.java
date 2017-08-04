package ru.biomedis.biomedismair3.Layouts.LeftPanel;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.biomedis.biomedismair3.App;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.INamed;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;

import java.net.URL;
import java.util.List;

/**
 * Created by Anama on 07.09.2015.
 */
public class NamedTreeItem extends TreeItem<INamed>
{
    private boolean childrenLoaded = false;
    private boolean leafPropertyComputed = false;
    private boolean leafNode = false;
    private ImageView icon=new ImageView();

    //смотрим изменения списка дочерних элементов
 // private   ListChangeListener<TreeItem<INamed>> changeListener  = ev-> {};






    public NamedTreeItem()
    {
        this(null);
    }

    public NamedTreeItem(INamed value) {
       this(value, (Node) null);
    }

    /**
     * позволяет отключить автозагрузку дочерних элементов
     * @param value
     * @param populateChildren
     */
    public NamedTreeItem(INamed value,boolean populateChildren)
    {

        this(value);
        childrenLoaded=true;
    }



    public NamedTreeItem(INamed value, Node graphic) {
        super(value, graphic);
        this.setGraphic(icon);

        //getChildren().addListener(changeListener);//листнер изменения списка дочерних элементов
        setValueAction(value);//установит иконку элемента итп
        //this.valueProperty().addListener((observable, oldValue, newValue) -> setValueAction(newValue));//при изменении значения будет меняться иконка

    }

    public void setLeafNode(boolean leafNode) {
        this.leafNode = leafNode;
    }

    public void setIcon(ImageView icon) {
        this.icon = icon;
    }

    /**
     * Выполнит действие при изменеии значения Value treeItem
     * @param value
     */
    private void setValueAction(INamed value)
    {
        if(value==null)
        {
            this.setGraphic(null);
            return;
        }


        setNodeIcon(value);

    }

    @Override
    public ObservableList<TreeItem<INamed>> getChildren() {

       // INamed value = getValue();
       // String name="";
       // if(value!=null)name=getValue().getNameString();
       // else name="null";
       // System.out.println("getChildren() "+name);
        //обеспечит однократную загрузку из базы по требованию, в дальнейшем только возврат уже заполненной структуры
        if (!childrenLoaded)
        {
            childrenLoaded = true;
            populateChildren(this);
        }
        return super.getChildren();
    }


    private void populateChildren(TreeItem<INamed> item)
    {


        ObservableList<TreeItem<INamed>> children = item.getChildren();
        List<Section> sections=null;
        List<Complex> complexes=null;
        List<Program> programs=null;
        //если у нас нод раздела то можем загружать 3 типа листьев
        if(item.getValue() instanceof Section)
        {
            sections=  App.getStaticModel().findAllSectionByParent((Section) item.getValue());
            complexes=App.getStaticModel().findAllComplexBySection((Section) item.getValue());
            programs = App.getStaticModel().findAllProgramBySection((Section) item.getValue());

            //здесь учитывается сортировка по алфавиту, с применением языка. Язык определяется примерно, по результату возвращенному getSmartLang для 1 элемента массива

            if(!sections.isEmpty())
            {
                App.getStaticModel().initStringsSection(sections);
                String lang= App.getStaticModel().getSmartLang(sections.get(0).getName());
                sections.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(section -> children.add(new NamedTreeItem(section)));
            }
            if(!complexes.isEmpty()) {
                App.getStaticModel().initStringsComplex(complexes);
                String lang= App.getStaticModel().getSmartLang(complexes.get(0).getName());
                complexes.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(complex -> children.add(new NamedTreeItem(complex)));
            }
            if(!programs.isEmpty()) {
                App.getStaticModel().initStringsProgram(programs);
                String lang= App.getStaticModel().getSmartLang(programs.get(0).getName());
                programs.stream().sorted(App.getStaticModel().getComparator(lang)).forEach(program -> children.add(new NamedTreeItem(program)));
            }





            //если нод комплекс то можно загрузить только програмы
        }else if(item.getValue() instanceof Complex)
        {
            programs=App.getStaticModel().findAllProgramByComplex((Complex) item.getValue());
            if(!programs.isEmpty())
            {
                App.getStaticModel().initStringsProgram(programs);
                programs.forEach(program -> children.add(new NamedTreeItem(program)));
            }

        }


    }




    @Override
    public boolean isLeaf()
    {
       // INamed value = getValue();
       // String name="";
        //if(value!=null)name=getValue().getNameString();
       // else name="null";
      //  System.out.println("isLeaf() " + name);


        if(getValue() instanceof Program) return true;


        if (!leafPropertyComputed) {

            leafPropertyComputed = true;
            if(getValue() instanceof Section)
            {
                leafNode= !App.getStaticModel().hasChildrenSection((Section) getValue());
            }
            else   if(getValue() instanceof Complex) leafNode=    !App.getStaticModel().hasChildrenComplex((Complex)getValue());


            //TODO нужно учесть что мы можемдобавить элементы позже в базу!!!!

        }
        return leafNode;
    }


    /**
     * Установит изображение для иконки в соответствии с типом элемента дерева
     * @param value
     */
    private void setNodeIcon(INamed value)
    {

        URL location=null;

        if(value instanceof Section)
        {
            location = getClass().getResource("/images/section.png");
           // icon.setImage( new Image(App.locationSectionIcon.toExternalForm()));
        }else if(value instanceof Complex)
        {
            location = getClass().getResource("/images/complex.png");
           // icon.setImage( new Image(App.locationComplexIcon.toExternalForm()));
        }
        else if(value instanceof Program)
        {
            location = getClass().getResource("/images/program.png");
           // icon.setImage( new Image(App.locationProgramIcon.toExternalForm()));
        }

        icon.setImage( new Image(location.toExternalForm()));



    }




}
