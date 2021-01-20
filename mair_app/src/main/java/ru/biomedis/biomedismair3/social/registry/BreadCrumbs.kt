package ru.biomedis.biomedismair3.social.registry

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import ru.biomedis.biomedismair3.social.remote_client.dto.DirectoryData

class BreadCrumbs(val handler: (directory: DirectoryData?)->Boolean) : HBox() {

    @FXML
    fun initialize() {
        this.alignment = Pos.CENTER_LEFT
    }


    fun destination(dir: DirectoryData) {
        if(this.children.isEmpty()){
            createLink("../", null).let { children.add(it) }
        }else {
           val label =  (children.last() as Label)
            createLink(label.text, label.userData as DirectoryData?).let {
                children.remove(label)
                children.add(it)
            }
        }
        addLabel(dir.name + "/", dir)

    }

    private fun onClick(event : ActionEvent){
            val link = event.source as Hyperlink
            if(!handler(link.userData as DirectoryData?)) return //если ошибка в хэндлере, то не нужно ничего изменять
            val linkIndex = children.indexOf(link)
            children.forEachIndexed{
                    index, node ->
                    if(index >= linkIndex && node is Hyperlink){
                        node.onAction = null
                    }
            }
            children.remove(linkIndex, children.size)
            if(children.size==0) return//не нужно показывать корневую папку если мы в корне находимся
            addLabel(link.text, link.userData as DirectoryData?)
    }

    private fun addLabel(text: String, dir: DirectoryData?){
        Label(text).apply {
            userData = dir
            font = Font(14.0)
            padding = Insets(1.0, 0.0, 0.0, 2.0)
        }.let {  children.add(it)  }
    }

    private fun createLink(text: String, dir: DirectoryData?): Hyperlink{
        return Hyperlink(text).apply {
            userData = dir
            setOnAction { onClick(it) }
            style = "-fx-underline: true"
            font = Font(14.0)
            padding = Insets(0.0)
        }
    }

    fun clear(){
        children.forEach{
            if(it is Hyperlink){
                it.onAction = null
            }
        }
        children.clear()
        addLabel( "../",null)
    }
}
