package ru.biomedis.starter;


import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class NewsView implements ViewNode {

private VBox vbox;
private Label date;
private Label text;
private Hyperlink link;



    public NewsView(String url, String text, String dt, String readMoreText,App app) {

        vbox =new VBox();
        vbox.setSpacing(5);
        vbox.setAlignment(Pos.CENTER_LEFT);
        date = new Label(dt);
        this.text =new Label(text);
        link=new Hyperlink(readMoreText);

        vbox.getChildren().addAll(date,this.text,link);
        link.setOnAction(event -> {
            link.setVisited(false);
            DefaultBrowserCaller.openInBrowser(url, app);
        });
    }

    @Override
    public Node getNode() {
        return vbox;
    }
}
