package application.model.display;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.IOException;

import application.model.picList.ThumbsReview;

public class DisplayUtil {

    private static final double INCREMENT = 0.1;   //缩放增量
    private static final double MIN_SCALE = 0.1;   //最小缩放比例
    private static final double MAX_SCALE = 3;     //最大缩放比例

    /**
     * 弹出或隐藏信息面板
     * @param infoPane 信息面板
     * @param isHidden 面板已被隐藏
     */
    public static void showOrHideInfo(AnchorPane infoPane, boolean isHidden) {
        KeyValue initValue, endValue;
        if(isHidden) {
            initValue = new KeyValue(infoPane.translateYProperty(), infoPane.getTranslateY());
            endValue = new KeyValue(infoPane.translateYProperty(), 0);
        } else {
            initValue = new KeyValue(infoPane.translateYProperty(), 0);
            endValue = new KeyValue(infoPane.translateYProperty(), infoPane.getPrefHeight());
        }
        KeyFrame initFrame = new KeyFrame(Duration.ZERO, initValue);
        KeyFrame endFrame = new KeyFrame(Duration.millis(200), endValue);
        Timeline timeline = new Timeline(initFrame, endFrame);
        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * 调整展示面板图片缩放
     * @param picPane 图片面板
     * @param scale 当前缩放值
     * @param isEnlarge 放大为true，缩小为false
     * @return 新缩放值
     */
    public static double modifyScale(StackPane picPane, double scale, boolean isEnlarge) {
        ImageView disPic = (ImageView) picPane.getChildren().get(0);

        double nextScale;
        if(scale <= 0) {    //当前缩放比例非法，恢复至原始大小
            nextScale = 1;
        } else {
            if (isEnlarge) { //放大操作
                if (scale >= MAX_SCALE) { //到达最大缩放比例，返回
                    return scale;
                }
                //计算最近的增量倍数的放大比例
                nextScale = ((int) (scale / INCREMENT) + 1) * INCREMENT;
            } else { //缩小操作
                if (scale <= MIN_SCALE) { //到达最小缩放比例，返回
                    return scale;
                }
                //计算最近的增量倍数的缩小比例
                nextScale = (int) (scale / INCREMENT) * INCREMENT;
                if (nextScale == scale) {
                    nextScale = scale - INCREMENT;
                }
            }
        }

        //根据计算得到的缩放比例调整disPic大小
        disPic.fitHeightProperty().unbind();    //大小属性解绑
        disPic.fitWidthProperty().unbind();
        disPic.setFitHeight(disPic.getImage().getHeight() * nextScale);
        disPic.setFitWidth(disPic.getImage().getWidth() * nextScale);

        return nextScale;
    }

    /**
     * 全屏显示图片
     * @param image 图片
     */
    public static void displayAtFullscreen(Image image) {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: black");
        stage.setScene(new Scene(pane));
        ImageView iv = new ImageView(image);
        iv.setPreserveRatio(true);
        pane.getChildren().add(iv);

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        iv.setFitWidth(screenWidth);
        iv.setFitHeight(screenHeight);

        showTip(pane, "按下 ESC 键退出");

        //设置ESC按键监听器，退出全屏
        stage.getScene().setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ESCAPE)) {
                stage.close();
            }
        });

        stage.show();
    }

    /**
     * 以指定的间隔依次全屏播放图片
     * @param delay 时间间隔
     * @param thumbsReview 当前窗口对应的缩略图预览
     * @param start 起始幻灯片
     */
    public static void displayAtSlide(double delay, ThumbsReview thumbsReview, int start) {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: black");
        stage.setScene(new Scene(pane));
        ImageView view = new ImageView();
        view.setPreserveRatio(true);
        ImageView nextView = new ImageView();
        nextView.setPreserveRatio(true);
        pane.getChildren().addAll(nextView, view);
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        view.setFitWidth(screenWidth);
        view.setFitHeight(screenHeight);
        nextView.setFitWidth(screenWidth);
        nextView.setFitHeight(screenHeight);

        showTip(pane, "按下 ESC 键退出");

        //创建幻灯片播放列表
        //首元素从index开始
        Image[] slideList = new Image[thumbsReview.getImageNodes().size() + 1];
        slideList[0] = new Image("/resource/icon.png");  //占位图

        //借助数组在lambda表达式内对局部变量进行修改
        int[] index = {start};  //当前图片索引
        boolean[] isPlaying = {false};   //播放状态

        try (FileInputStream fis = new FileInputStream(thumbsReview.getImageNodes().get(start).getFile())) {
            Image img = new Image(fis);
            slideList[start + 1] = img;
            view.setImage(slideList[index[0] + 1]);    //显示首张图片
        } catch (IOException e) {
            e.printStackTrace();
        }


        stage.show();

        //若播放列表有效长度大于1，则依次播放幻灯片
        if(thumbsReview.getImageNodes().size() > 1) {
            isPlaying[0] = true;
            new Thread(() -> {
                while (isPlaying[0]) {
                    //准备下张图片
                    int nextIndex = index[0] + 1;
                    if(nextIndex >= thumbsReview.getImageNodes().size() + 1) {     //越界，返回到1号
                        nextIndex = 1;
                    }
                    if(slideList[nextIndex] == null) {
                        try (FileInputStream fis = new FileInputStream(thumbsReview.getImageNodes().get(nextIndex - 1).getFile())) {
                            Image img = new Image(fis);
                            slideList[nextIndex] = img;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    nextView.setImage(slideList[nextIndex]);
                    nextView.setOpacity(0);


                    try {
                        Thread.sleep((int) (delay * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    slideFadeOut(view, nextView);     //播放动画，将前景（当前图片）淡出的同时将背景（下张图片）淡入

                    //更新并显示前景图片
                    view.setImage(nextView.getImage());
                    view.setOpacity(1);

                    index[0] = nextIndex;   //索引指向下一张
                }
            }).start();
        }

        //设置ESC按键监听器，退出全屏
        stage.getScene().setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ESCAPE)) {
                isPlaying[0] = false;   //停止播放
                stage.close();
            }
        });


    }

    /**
     * 在面板上显示提示
     * @param pane 面板
     * @param s 提示内容
     */
    private static void showTip(StackPane pane, String s) {
        Label label = new Label("  " + s + "  ");
        label.setFont(new Font("等线", 30));
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-background-color: black");
        label.setOpacity(0.7);
        label.setTranslateY(- Screen.getPrimary().getBounds().getHeight() / 3);
        pane.getChildren().add(label);

        new Thread(() -> {
            KeyValue initValue, endValue;
            initValue = new KeyValue(label.opacityProperty(), 0.7);
            endValue = new KeyValue(label.opacityProperty(), 0);
            KeyFrame initFrame = new KeyFrame(Duration.millis(1500), initValue);
            KeyFrame endFrame = new KeyFrame(Duration.millis(2000), endValue);
            Timeline timeline = new Timeline(initFrame, endFrame);
            timeline.setCycleCount(1);
            timeline.play();;
        }).start();
    }

    /**
     * 对ImageView播放淡入淡出动画
     * @param view  当前图片
     * @param nextView 下张图片
     */
    private static void slideFadeOut(ImageView view, ImageView nextView) {
        KeyValue initValue_view, endValue_view, initValue_nextView, endValue_nextView;

        initValue_view = new KeyValue(view.opacityProperty(), 1);
        endValue_view = new KeyValue(view.opacityProperty(), 0);
        initValue_nextView = new KeyValue(nextView.opacityProperty(), 0);
        endValue_nextView = new KeyValue(nextView.opacityProperty(), 1);

        //前景图片逐渐变为透明，背景图片逐渐变为不透明
        KeyFrame initFrame = new KeyFrame(Duration.millis(0), initValue_view, initValue_nextView);
        KeyFrame endFrame = new KeyFrame(Duration.millis(600), endValue_view, endValue_nextView);

        Timeline timeline = new Timeline(initFrame, endFrame);
        timeline.setCycleCount(1);
        timeline.play();
    }
}
