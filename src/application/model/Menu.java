package application.model;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

import application.controller.*;
import application.model.appearance.MainUIBorder;
import application.model.picList.ImageNode;
import application.model.picList.ThumbsReview;

public class Menu {
    public static MainUI mainController;    //记录主窗口控制器

    public static void initMainUI(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Menu.class.getResource("/application/view/MainUI.fxml"));
        Parent root = loader.load();

        stage.getIcons().add(new Image("/resource/icon.png"));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("电子图片管理");
        stage.setScene(new Scene(root));
        stage.setResizable(true);
        stage.setAlwaysOnTop(false);

        MainUIBorder.setCustomResize(stage);    //设置自定义窗口缩放

        mainController = loader.getController();
        mainController.setStage(stage);
    }

    public static void initWelcomePage(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Menu.class.getResource("/application/view/WelcomePage.fxml"));
        Parent root = loader.load();

        Stage welcomeStage = new Stage(StageStyle.UNDECORATED);
        welcomeStage.getIcons().add(new Image("/resource/icon.png"));
        welcomeStage.setScene(new Scene(root));
        welcomeStage.setAlwaysOnTop(true);
        welcomeStage.setResizable(false);

        initMainUI(stage);

        new Thread(() -> {
            Platform.runLater(() -> welcomeStage.show());
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> welcomeStage.close());
            Platform.runLater(() -> stage.show());
        }).start();
    }

    public static void initRenamePage(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Menu.class.getResource("/application/view/RenamePage.fxml"));
        Parent root = loader.load();

        stage.getIcons().add(new Image("/resource/icon/rename.png"));
        stage.setTitle("批量重命名");
        stage.setScene(new Scene(root));
        stage.setAlwaysOnTop(false);
        stage.setResizable(false);

        Rename controller = loader.getController();
        controller.setStage(stage);

        stage.show();
    }

    public static Display initDisplayPage(Stage stage, ThumbsReview thumbsReview, int index) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Menu.class.getResource("/application/view/DisplayPage.fxml"));
        Parent root = loader.load();

        stage.getIcons().add(new Image("/resource/icon.png"));
        stage.setScene(new Scene(root));
        stage.setResizable(true);
        stage.setAlwaysOnTop(false);
        stage.setMinHeight(500);
        stage.setMinWidth(750);

        Display controller = loader.getController();
        controller.setStage(stage);
        controller.setThumbsReview(thumbsReview);
        controller.currentUpdate(index);

        stage.show();

        return controller;
    }
}
