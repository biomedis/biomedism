package ru.biomedis.starter;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class NewsView implements ViewNode {

private VBox vbox;
private Label date;
private Label text;
private Hyperlink link;
private Label sep;



    public NewsView(String url, String text, String dt, String readMoreText,App app) {

        vbox =new VBox();
        vbox.setSpacing(5);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setPadding(new Insets(0,0,5,0));

        date = new Label(dt);
        date.setStyle(" -fx-font-style: italic; -fx-font-weight: bold;");
        this.text =new Label(text);
        link=new Hyperlink(readMoreText);
        link.setEllipsisString("...");
        link.setPrefWidth(250);
        link.setMaxWidth(250);
        sep=new Label(" ");
        sep.setFont(new Font(1));
       // sep.setStyle(" -fx-background-color: linear-gradient(from 0% 0% to 60% 60%, #64daf1, #f3f3f3)");
        sep.setStyle(" -fx-background-color: linear-gradient(to right, #64daf1, #f3f3f3)");
        sep.setMaxWidth(Double.MAX_VALUE);

        vbox.getChildren().addAll(date,this.text,link, sep);

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
