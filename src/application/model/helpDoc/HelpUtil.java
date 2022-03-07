package application.model.helpDoc;

import application.model.Util;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

public class HelpUtil {

    /**
     * 创建一个网页视图窗口打开帮助文档
     */
    public static void openDoc() {
        Stage stage = new Stage();
        stage.setTitle("使用帮助");
        AnchorPane base = new AnchorPane();
        base.setPrefWidth(1280);
        base.setPrefHeight(800);
        Scene scene = new Scene(base);
        stage.setScene(scene);

        WebView webView = new WebView();
        base.getChildren().add(webView);
        Util.setAnchor(webView, 0.0, 0.0, 0.0, 0.0);
        webView.getEngine().load(HelpUtil.class.getResource("web/index.html").toExternalForm());

        stage.show();
    }

}
