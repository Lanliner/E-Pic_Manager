package application.controller;

import application.model.picList.ThumbsReview;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.model.appearance.Config;
import application.model.appearance.CustomStyle;
import application.model.display.DisplayUtil;
import application.model.picList.ImageNode;
import application.model.Util;


public class Display implements Initializable {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * 初始化工作
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //图片面板初始化
        initPicPane();

        //操作面板初始化
        initControlPane();

        //信息面板初始化
        infoPane.setOpacity(0.8);
        infoPane.setTranslateY(infoPane.getPrefHeight());
        isInfoPaneHidden = true;

    }


    /*******************************
     * 图片面板                      *
     *******************************/
    private ThumbsReview thumbsReview;     //当前窗口所在文件夹的缩略图列表对象

    private int curImageNodeIndex;    //当前图片节点在缩略图列表中的索引

    @FXML
    private AnchorPane basePane;

    @FXML
    private ImageView disPic;   //当前展示图片

    @FXML
    private StackPane disPicPane;    //使用StackPane安放ImageView，默认居中

    private ContextMenu contextMenu = new ContextMenu();    //右键菜单

    private double initX;     //鼠标按下图片时的初值X
    private double initY;     //初值Y

    public void setThumbsReview(ThumbsReview thumbsReview) {
        this.thumbsReview = thumbsReview;
    }

    public int getCurImageNodeIndex() {
        return curImageNodeIndex;
    }

    /**
     * 图片面板初始化
     */
    private void initPicPane() {

        //对根面板设置方向键事件处理，进行图片切换
        basePane.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if(event.getCode().equals(KeyCode.UP) || event.getCode().equals(KeyCode.LEFT)) {
                lastAction(null);
            } else if(event.getCode().equals(KeyCode.DOWN) || event.getCode().equals(KeyCode.RIGHT)){
                nextAction(null);
            }
        });

        //初始化右键菜单
        contextMenu.getItems().addAll(new MenuItem("图片复位"), new MenuItem("适应窗口"), new MenuItem("查看原始大小"));
        contextMenu.getItems().get(0).setOnAction(event -> {    //图片复位
            disPic.setTranslateX(0);
            disPic.setTranslateY(0);
            rotateDegree = 0;
            disPic.setRotate(0);
        });
        contextMenu.getItems().get(1).setOnAction(event -> {    //适应窗口
            if(!disPic.fitWidthProperty().isBound()) {
                disPic.fitWidthProperty().bind(disPicPane.widthProperty());
                disPic.fitHeightProperty().bind(disPicPane.heightProperty());
                disPic.setTranslateX(0);
                disPic.setTranslateY(0);
            }
        });
        contextMenu.getItems().get(2).setOnAction(event -> {    //查看原始大小
            DisplayUtil.modifyScale(disPicPane, 0, true);
            disPic.setTranslateX(0);
            disPic.setTranslateY(0);
        });

        //对图片ImageView宽度和高度属性设置监听器，计算缩放比例
        disPic.fitWidthProperty().addListener(((observable, oldValue, newValue) -> {
            double ratioW = newValue.doubleValue() / disPic.getImage().getWidth();  //宽度比
            double ratioH = disPic.getFitHeight() / disPic.getImage().getHeight();  //高度比
            scale = Math.min(ratioW, ratioH);   //缩放比例为小者
            scaleText.setText((int)(scale * 100) + "%");
        }));
        disPic.fitHeightProperty().addListener(((observable, oldValue, newValue) -> {
            double ratioW = disPic.getFitWidth() / disPic.getImage().getWidth();  //宽度比
            double ratioH = newValue.doubleValue() / disPic.getImage().getHeight();  //高度比
            scale = Math.min(ratioW, ratioH);   //缩放比例为小者
            scaleText.setText((int)(scale * 100) + "%");
        }));

        //对图片面板设置鼠标滚轮事件处理，进行图片缩放
        disPicPane.setOnScroll(event -> {
            if(event.getDeltaY() > 0) {     //滚轮向上，放大
                zoomInAction(null);
            } else {    //滚轮向下，缩小
                zoomOutAction(null);
            }
        });

        //对图片面板设置鼠标点击和拖拽事件监听，进行图片移动或菜单调出
        disPicPane.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)) {
                disPicPane.setCursor(Cursor.CLOSED_HAND);
                disPic.setCursor(Cursor.CLOSED_HAND);
                //定义初值
                initX = event.getSceneX() - disPic.getTranslateX();
                initY = event.getSceneY() - disPic.getTranslateY();
            }
        });
        disPicPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                //设置位移量
                disPic.setTranslateX(event.getSceneX() - initX);
                disPic.setTranslateY(event.getSceneY() - initY);

                //阻止图片被移出边界
                if(disPic.getTranslateX() < - disPicPane.getWidth() / 2 - disPic.getFitWidth() / 2) {
                    disPic.setTranslateX(- disPicPane.getWidth() / 2 - disPic.getFitWidth() / 2);
                }
                if(disPic.getTranslateX() > disPicPane.getWidth() / 2 + disPic.getFitWidth() / 2) {
                    disPic.setTranslateX(disPicPane.getWidth() / 2 + disPic.getFitWidth() / 2);
                }
                if(disPic.getTranslateY() < - disPicPane.getHeight() / 2 - disPic.getFitHeight() / 2) {
                    disPic.setTranslateY(- disPicPane.getHeight() / 2 - disPic.getFitHeight() / 2);
                }
                if(disPic.getTranslateY() > disPicPane.getHeight() / 2 + disPic.getFitHeight() / 2) {
                    disPic.setTranslateY(disPicPane.getHeight() / 2 + disPic.getFitHeight() / 2);
                }
            }
        });
        disPicPane.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                disPicPane.setCursor(Cursor.DEFAULT);
                disPic.setCursor(Cursor.DEFAULT);
                contextMenu.hide();
            } else if(event.getButton().equals(MouseButton.SECONDARY)) {
                contextMenu.show(disPicPane, event.getScreenX(), event.getScreenY());
            }
        });

    }

    /**
     * 更新当前图片
     * @param index 新索引
     */
    public void currentUpdate(int index) {

        curImageNodeIndex = index;  //更新索引

        ImageNode curImageNode = thumbsReview.getImageNodes().get(index);

        stage.setTitle(String.format("%s (%d/%d)",
                curImageNode.getFile().getName(), index + 1, thumbsReview.getImageNodes().size()));

        try(FileInputStream fis = new FileInputStream(curImageNode.getFile())) {
            disPic.setImage(new Image(fis));   //设置展示图片
        } catch (Exception e) {
            e.printStackTrace();
        }

        disPic.fitWidthProperty().bind(disPicPane.widthProperty());     //将ImageView与其父StackPane绑定大小属性
        disPic.fitHeightProperty().bind(disPicPane.heightProperty());   // 以适应屏幕大小
        rotateDegree = 0;   //默认旋转角度
        disPic.setRotate(rotateDegree);

        //如果当前图片过小，则不作缩放调整
        if(disPic.getImage().getWidth() < 500 && disPic.getImage().getHeight() < 500) {
            DisplayUtil.modifyScale(disPicPane, 0, true);
        }

        //更新信息面板
        nameText.setText(curImageNode.getFile().getName());
        dirHyperlink.setText(curImageNode.getFile().getParent());
        resolutionText.setText((int)disPic.getImage().getWidth() + " x " + (int)disPic.getImage().getHeight());
        sizeText.setText(Util.translateSize(curImageNode.getFile().length()));
        try (FileInputStream fis = new FileInputStream(curImageNode.getFile())) {
            BufferedImage bufferedImage = ImageIO.read(fis);
            depthText.setText(bufferedImage.getColorModel().getPixelSize() + "位");
        } catch (Exception e) {
            e.printStackTrace();
        }
        modifyDateText.setText(Util.translateDate(curImageNode.getFile().lastModified()));

        //计算初始缩放比例
        double ratioW = disPic.getFitWidth() / disPic.getImage().getWidth();  //宽度比
        double ratioH = disPic.getFitHeight() / disPic.getImage().getHeight();  //高度比
        scale = Math.min(ratioW, ratioH);   //缩放比例为小者
        scaleText.setText((int)(scale * 100) + "%");
    }


    /*******************************
     * 信息面板                      *
     *******************************/
    @FXML
    private AnchorPane infoPane;

    private boolean isInfoPaneHidden;

    @FXML
    private Text nameText;

    @FXML
    private Hyperlink dirHyperlink;

    @FXML
    private Text resolutionText;

    @FXML
    private Text sizeText;

    @FXML
    private Text depthText;

    @FXML
    private Text modifyDateText;

    @FXML
    void hyperlinkAction(ActionEvent event) {
        try {
            Desktop.getDesktop().open(
                    thumbsReview.getImageNodes().get(curImageNodeIndex).getFile().getParentFile());  //打开目录文件夹
        } catch (IOException e) {
            Util.showMessage(Alert.AlertType.ERROR, "错误", "目录不存在", true);
        }
    }


    /*******************************
     * 操作面板                      *
     *******************************/
    @FXML
    private AnchorPane controlPane;

    @FXML
    private Button infoButton;

    @FXML
    private Button fullscreenButton;

    @FXML
    private Button rotateButton;
    private double rotateDegree;

    @FXML
    private Button deleteButton;

    @FXML
    private GridPane midButtonPane;

    @FXML
    private Button lastButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button playButton;

    @FXML
    private Button zoomInButton;

    @FXML
    private Button zoomOutButton;

    @FXML
    private Text scaleText;
    private double scale;

    @FXML
    void infoAction(ActionEvent event) {
        DisplayUtil.showOrHideInfo(infoPane, isInfoPaneHidden);
        isInfoPaneHidden = !isInfoPaneHidden;
    }

    @FXML
    void fullscreenAction(ActionEvent event) {
        DisplayUtil.displayAtFullscreen(disPic.getImage());
    }

    @FXML
    void rotateAction(ActionEvent event) {
        rotateDegree = (rotateDegree + 90) % 360;
        disPic.setRotate(rotateDegree);
    }

    @FXML
    void deleteAction(ActionEvent event) {
        thumbsReview.unSelectImages();
        thumbsReview.getSelectedImages().add(thumbsReview.getImageNodes().get(curImageNodeIndex));
        thumbsReview.deleteImages();
    }

    @FXML
    void lastAction(ActionEvent event) {
        try {
            currentUpdate((curImageNodeIndex - 1 + thumbsReview.getImageNodes().size())
                    % thumbsReview.getImageNodes().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void nextAction(ActionEvent event) {
        try {
            currentUpdate((curImageNodeIndex + 1) % thumbsReview.getImageNodes().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void playAction(ActionEvent event) throws Exception {
        DisplayUtil.displayAtSlide(Config.getSliceDelay(), thumbsReview, curImageNodeIndex);
    }

    @FXML
    void zoomInAction(ActionEvent event) {
        scale = DisplayUtil.modifyScale(disPicPane, scale, true);
    }

    @FXML
    void zoomOutAction(ActionEvent event) {
        scale = DisplayUtil.modifyScale(disPicPane, scale, false);
    }

    private void initControlPane() {
        CustomStyle.setButtonStyleAlpha(
                infoButton, fullscreenButton, rotateButton, lastButton, nextButton, playButton, zoomInButton, zoomOutButton
        );
        CustomStyle.setButtonStyleBeta(deleteButton);
        infoButton.setGraphic(new ImageView("/resource/icon/info.png"));
        fullscreenButton.setGraphic(new ImageView("/resource/icon/fullscreen.png"));
        rotateButton.setGraphic(new ImageView("/resource/icon/rotate.png"));
        deleteButton.setGraphic(new ImageView("/resource/icon/delete.png"));
        lastButton.setGraphic(new ImageView("/resource/icon/left.png"));
        nextButton.setGraphic(new ImageView("/resource/icon/right.png"));
        playButton.setGraphic(new ImageView("/resource/icon/play.png"));
        zoomInButton.setGraphic(new ImageView("/resource/icon/zoom_in.png"));
        zoomOutButton.setGraphic(new ImageView("/resource/icon/zoom_out.png"));
        Tooltip.install(infoButton, new Tooltip("图片信息"));
        Tooltip.install(fullscreenButton, new Tooltip("全屏"));
        Tooltip.install(rotateButton, new Tooltip("旋转"));
        Tooltip.install(deleteButton, new Tooltip("删除"));
        Tooltip.install(lastButton, new Tooltip("上一张"));
        Tooltip.install(nextButton, new Tooltip("下一张"));
        Tooltip.install(playButton, new Tooltip("幻灯片放映"));
        Tooltip.install(zoomInButton, new Tooltip("放大"));
        Tooltip.install(zoomOutButton, new Tooltip("缩小"));

        //对操作面板宽度设置监听器，保持元素居中
        controlPane.widthProperty().addListener(((observable, oldValue, newValue) -> {
            midButtonPane.setLayoutX(newValue.doubleValue() / 2 - midButtonPane.getPrefWidth() / 2);    //按钮居中
        }));
    }

}
