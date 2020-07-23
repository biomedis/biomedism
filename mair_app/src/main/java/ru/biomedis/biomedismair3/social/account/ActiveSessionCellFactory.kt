package ru.biomedis.biomedismair3.social.account

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import java.io.IOException
import java.text.SimpleDateFormat


class ActiveSessionCellFactory: Callback<ListView<ActiveSession>, ListCell<ActiveSession>> {
    override fun call(param: ListView<ActiveSession>?): ListCell<ActiveSession> {
    return TaskCell()

    }


    class TaskCell : ListCell<ActiveSession>() {
        private  val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        @FXML
        private lateinit var ipLbl: Label

        @FXML
        private lateinit var osLbl: Label

        @FXML
        private lateinit var browserLbl: Label

        @FXML
        private lateinit var createdLbl: Label

        @FXML
        private lateinit var expectedLbl: Label

        @FXML
        private lateinit var countryLbl: Label

        @FXML
        private lateinit var regionLbl: Label

        @FXML
        private lateinit var cityLbl: Label

        private fun loadFXML() {
            try {
                val loader = FXMLLoader(javaClass.getResource("fxml/ActiveSessionListCell.fxml"))
                loader.setController(this)
                loader.setRoot(this)
                loader.load<Any>()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun updateItem(item: ActiveSession, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty) {
                text = null
                contentDisplay = ContentDisplay.TEXT_ONLY
            } else {
                ipLbl.text = item.ip
                osLbl.text = item.os
                browserLbl.text = item.browser
                countryLbl.text = item.country
                cityLbl.text = item.city
                regionLbl.text = item.region
                expectedLbl.text = dateFormat.format(item.expired)
                createdLbl.text = dateFormat.format(item.created)
                contentDisplay = ContentDisplay.GRAPHIC_ONLY
            }
        }

        init {
            loadFXML()
        }
    }
}
