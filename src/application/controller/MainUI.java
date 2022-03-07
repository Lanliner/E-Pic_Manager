package application.controller;

import application.model.Menu;
import application.model.picList.ThumbsReviewCache;
import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.*;

import application.model.*;
import application.model.appearance.Config;
import application.model.appearance.CustomStyle;
import application.model.dirTree.*;
import application.model.helpDoc.HelpUtil;
import application.model.picList.ThumbsReview;

public class MainUI implements Initializable {

    private Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public Stage getStage(){
        return stage;
    }

    /**
     * 初始化工作
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //导入配置文件
        Util.configInput();

        //标题栏初始化
        createTitle();
        //目录树初始化
        createTree();
        //路径面板初始化
        initPathPane();
        //操作面板初始化
        initControlPane();
        //更新主题
        updateTheme();

    }

    @FXML
    private AnchorPane baseAnchor;

    /*******************************
     * 标题栏                       *
     *******************************/
    @FXML
    private AnchorPane titleBar;

    @FXML
    private Text titleText;

    @FXML
    private Button helpButton;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private Button exitButton;

    @FXML
    void helpAction(ActionEvent event) {
        HelpUtil.openDoc();
    }

    @FXML
    void colorPickerAction(ActionEvent event) {
        Config.setThemeColor(Util.convertColorToString(colorPicker.getValue()));
        if((colorPicker.getValue().getRed() > 0.5)  //偏白色背景使用黑色标题字
                && (colorPicker.getValue().getGreen() > 0.5)
                && (colorPicker.getValue().getBlue()) > 0.5) {
            Config.setTitleColor("#000000");
        }else if((colorPicker.getValue().getRed() > 0.7)    //黄色背景使用黑色标题字
                && (colorPicker.getValue().getGreen() > 0.7)
                && (colorPicker.getValue().getBlue() < 0.5)) {
            Config.setTitleColor("#000000");
        }else {                                     //偏黑色背景使用白色标题字
            Config.setTitleColor("#ffffff");
        }
        updateTheme();
    }

    @FXML
    void minimizeAction(ActionEvent event) {
        stage.setIconified(true);
    }

    @FXML
    void maximizeAction(ActionEvent event) {
        stage.setMaximized(!stage.isMaximized());
        if(stage.isMaximized()){
            maximizeButton.setEffect(new ImageInput(new Image("/resource/icon/unmaximize.png")));
            stage.setX(0);
            stage.setY(0);
        }else {
            maximizeButton.setEffect(new ImageInput(new Image("/resource/icon/maximize.png")));
        }
    }

    @FXML
    void exitAction(ActionEvent event) {
        System.exit(0);
    }

    private double offSetX;     //鼠标按下标题栏时对应窗体左上角的偏移量X
    private double offSetY;     //偏移量Y

    private void createTitle() {
        Tooltip.install(helpButton, new Tooltip("获取帮助"));
        maximizeButton.setEffect(new ImageInput(new Image("/resource/icon/maximize.png")));

        //注册标题栏双击放大事件监听器
        titleBar.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                    maximizeAction(null);
                }
            });

        //标题栏按下拖动事件
        titleBar.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)) {
                offSetX = event.getScreenX() - stage.getX();
                offSetY = event.getScreenY() - stage.getY();
            }
        });

        titleBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                stage.setX(event.getScreenX() - offSetX);
                stage.setY(event.getScreenY() - offSetY);
            }
        });
    }

    /**
     * 主题设置
     */
    private void updateTheme() {
        titleBar.setStyle("-fx-background-color: " + Config.getThemeColor());
        baseAnchor.setStyle("-fx-background-color: "+ Config.getThemeColor());
        titleText.setStyle("-fx-fill: "+ Config.getTitleColor());
        CustomStyle.setButtonStyleAlpha(backwardButton, forwardButton, upwardButton);
        CustomStyle.setButtonStyleBeta(openPicButton, copyPicButton, renamePicButton, deletePicButton);

        ThumbsReview.setTheme();

        Util.configOutput();    //输出配置文件
    }


    /*******************************
     * 路径面板                      *
     *******************************/
    @FXML
    private Button backwardButton;

    @FXML
    private Button forwardButton;

    @FXML
    private Button upwardButton;

    @FXML
    void backwardAction(ActionEvent event) {
        //后退
        curIndex--;
        forwardButton.setDisable(false);
        if(curIndex == 0) {
            backwardButton.setDisable(true);    //到达记录首，禁用按钮
        }
        currentItem = history.get(curIndex);
        if(!DirUtil.ifExist(currentItem)) { //目录不复存在
            history.clear();
            currentItem = null;
            recreateTree();
            Util.showMessage(Alert.AlertType.WARNING, "注意", "前一步目录已被删除或移动", true);
        }
        currentUpdate(currentItem);
    }

    @FXML
    void forwardAction(ActionEvent event) {
        //前进
        curIndex++;
        backwardButton.setDisable(false);
        if(curIndex == history.size() - 1) {    //到达记录末尾，禁用按钮
            forwardButton.setDisable(true);
        }
        currentItem = history.get(curIndex);
        if(!DirUtil.ifExist(currentItem)) { //目录不复存在
            history.clear();
            currentItem = null;
            recreateTree();
            Util.showMessage(Alert.AlertType.WARNING, "注意", "后一步目录已被删除或移动", true);
        }
        currentUpdate(currentItem);
    }

    @FXML
    void upwardAction(ActionEvent event) {
        //返回上一级
        if(currentItem.getParent() != null) {
            currentItem.setExpanded(false); //折叠当前节点
            currentItem = currentItem.getParent();  //前往父节点
            if(!DirUtil.ifExist(currentItem)) { //目录不复存在
                history.clear();
                currentItem = null;
                recreateTree();
                Util.showMessage(Alert.AlertType.WARNING, "注意", "上级目录已被删除或移动", true);
            }
            currentUpdate(currentItem);
            addRecord();
        }
    }

    private final ArrayList<TreeItem<DirItemData>> history = new ArrayList<TreeItem<DirItemData>>(); //历史记录数组
    private int curIndex;  //当前历史记录位置索引

    /**
     * 添加当前节点至历史记录
     */
    private void addRecord() {
        if(currentItem != null) {
            while (curIndex < history.size() - 1) { //清除curIndex指向记录后的记录
                history.remove(history.size() - 1);
            }
            if(history.isEmpty() || currentItem != history.get(history.size() - 1)) {
                history.add(currentItem);   //添加记录
                curIndex = history.size() - 1;  //curIndex定位至新的历史记录末尾
                if(curIndex > 0) {
                    backwardButton.setDisable(false);
                }
                forwardButton.setDisable(true);
            }
        }
    }

    @FXML
    private TextField path; //路径框

    public void setPath(File f) {
        String s = "此电脑";
        if (f != null) {
            s += " > " + f.getPath().replaceAll("\\\\", " > ");
        }
        path.setText(s);
    }

    public void clearPath() {
        path.clear();
    }

    @FXML
    public TextField searchField;     //搜索框

    @FXML
    public Button cancelSearchButton;

    @FXML
    void searchAction(ActionEvent event) {
        String name = searchField.getText().trim();
        searchField.setText(name);
        if(name.isEmpty() || currentItem == null || currentItem.getValue().getFile() == null) {
            return;
        }
        cancelSearchButton.setVisible(true);

        //搜索图片
        thumbsReview.searchImages(name);

    }

    @FXML
    void cancelSearchAction(ActionEvent event) {
        searchField.setText("");
        cancelSearchButton.setVisible(false);

        thumbsReview.cancelSearch();
    }

    private void initPathPane() {
        Tooltip.install(backwardButton, new Tooltip("后退"));
        Tooltip.install(forwardButton, new Tooltip("前进"));
        Tooltip.install(upwardButton, new Tooltip("上一级"));

        cancelSearchButton.setVisible(false);
    }

    /*******************************
     * 目录树                       *
     *******************************/
    @FXML
    private TreeView<DirItemData> directoryTree;

    private TreeItem<DirItemData> currentItem;  //当前访问的目录

    /**
     * 新建目录树
     */
    private void createTree() {
        DirItemData data_root = new DirItemData(){
            @Override
            public String toString(){
                return "此电脑";
            }
        };
        TreeItem<DirItemData> item_root = new TreeItem<DirItemData>(data_root);
        DirUtil.addChildrenItems(item_root);
        directoryTree.setRoot(item_root);

        directoryTree.setCellFactory(new Callback<TreeView<DirItemData>, TreeCell<DirItemData>>() {
            @Override
            public TreeCell<DirItemData> call(TreeView<DirItemData> param) {
                return new DirTreeCell();
            }
        });

        //注册鼠标单双击事件监听器
        directoryTree.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if ((event.getButton().equals(MouseButton.PRIMARY))
                    && (event.getClickCount() == 1 && event.getTarget() instanceof LabeledText || event.getClickCount() == 2)) {
                Node node = event.getPickResult().getIntersectedNode();
                if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
                    TreeItem<DirItemData> item = directoryTree.getSelectionModel().getSelectedItem(); //获取选择
                    //选择的目录不复存在，则刷新后改为选择父目录
                    if(!DirUtil.ifExist(item)) {
                        item = item.getParent();
                        directoryTree.getSelectionModel().select(item);
                        recreateTree();
                        history.clear();
                        Util.showMessage(Alert.AlertType.WARNING, "注意", "您选择的目录已被删除或移动", true);
                    }
                    currentUpdate(item);    //更新当前节点
                    addRecord();    //记入历史记录
                }
            }
        });

        currentUpdate(null);
    }


    @FXML
    private MenuItem refreshMenuItem;

    @FXML
    void refreshAction(ActionEvent event) {
        //清空缓冲区
        thumbsReviewCache.clear();
        //重构目录树
        recreateTree();
        //清空历史记录
        history.clear();
        curIndex = 0;
        setMessage2("刷新成功", "green", 1500);
    }

    /**
     * 刷新目录树
     */
    public void recreateTree() {
        File f = null;
        if(currentItem != null) {
            f = currentItem.getValue().getFile();   //记录当前节点文件
        }

        directoryTree.getSelectionModel().clearSelection();
        directoryTree.getRoot().getChildren().clear();
        DirUtil.addChildrenItems(directoryTree.getRoot());
        TreeItem<DirItemData> item = null;

        //定位当前节点
        if(f != null && f.exists()) {
            String[] pathName = f.getAbsolutePath().split("\\\\");  //记录路径上的节点名称
            pathName[0] += "\\";

            item = directoryTree.getRoot();
            for (String s : pathName) {     //由节点名称寻找原节点
                for (TreeItem<DirItemData> x : item.getChildren()) {
                    String name = x.getValue().getFile().getName();
                    if (name.isEmpty()) {    //盘符
                        name = x.getValue().getFile().getPath();
                    }
                    if (s.equals(name)) {
                        item = x;
                        DirUtil.addChildrenItems(item);
                        break;
                    }
                }
            }
        }
        currentUpdate(item);    //更新当前节点
    }

    /**
     * 更新当前节点
     * @param newCurrentItem 新节点
     */
    public void currentUpdate(TreeItem<DirItemData> newCurrentItem) {
        currentItem = newCurrentItem;
        directoryTree.getSelectionModel().clearSelection(); //清除选择节点
        if(currentItem != null) {   //当前节点不为空，即有选中结点
            directoryTree.getSelectionModel().select(currentItem);  //选择节点
            setPath(currentItem.getValue().getFile());  //更新路径
            if(currentItem.getParent() == null) {   //无父节点，禁用上一级按钮，否则启用
                upwardButton.setDisable(true);
            } else {
                upwardButton.setDisable(false);
            }

            thumbsReviewCache.update(); //更新缓冲区

            if(currentItem.getValue().getFile() != null) {
                //更新缩略图面板,保证选中有效文件夹
                ThumbsReview tr = thumbsReviewCache.search(currentItem.getValue().getFile());
                if(tr == null) {    //无缓存
                    thumbsReview = new ThumbsReview(currentItem.getValue().getFile());
                    Util.setAnchor(thumbsReview.getRootPane(), 0.0, 0.0, 0.0, 0.0);
                    thumbsReview.eventHandlingActivates();
                    thumbsReview.getRootPane().requestFocus();

                    thumbsReviewCache.add(thumbsReview);
                } else {    //有缓存
                    thumbsReview = tr;
                    thumbsReview.unSelectImages();
                }
                thumbsBase.getChildren().clear();
                thumbsBase.getChildren().add(thumbsReview.getRootPane());
                thumbsReview.refresh(thumbsReview.getCurrentOrder());
                thumbsReview.updateInterface();

                openPicButton.setDisable(thumbsReview.isEmpty());
            } else {
                //文件夹为null，说明访问此电脑
                openPicButton.setDisable(true);
                thumbsBase.getChildren().clear();
            }
        }
        else {    //当前节点为空
            clearPath();    //清空路径框
            openPicButton.setDisable(true);
            backwardButton.setDisable(true);
            forwardButton.setDisable(true);
            upwardButton.setDisable(true);
        }

    }


    /*******************************
     * 缩略图面板                    *
     *******************************/
    @FXML
    private AnchorPane thumbsBase;

    private ThumbsReviewCache thumbsReviewCache = new ThumbsReviewCache();  //缩略图列表缓存

    private ThumbsReview thumbsReview;  //当前缩略图列表

    public ThumbsReviewCache getThumbsReviewCache() {
        return thumbsReviewCache;
    }

    public ThumbsReview getThumbsReview() {
        return thumbsReview;
    }


    /*******************************
     * 操作面板                      *
     *******************************/
    @FXML
    public Text picCountText;

    @FXML
    public Button openPicButton;

    @FXML
    public Button copyPicButton;

    @FXML
    public Button renamePicButton;

    @FXML
    public Button deletePicButton;

    @FXML
    public void openPicAction(ActionEvent event) throws Exception {
        int index;
        if(thumbsReview.getSelectedImages().isEmpty()) {
            index = 0;
        } else {
            index = thumbsReview.getImageNodes().indexOf(thumbsReview.getSelectedImages().get(0));
        }
        thumbsReview.getDisplayWindows().add(
                Menu.initDisplayPage(new Stage(), thumbsReview, index));
        setMessage2("打开图片完成", "green", 1000);
    }

    @FXML
    void copyPicAction(ActionEvent event) {
        thumbsReview.copyImages(thumbsReview.getSelectedImages());
    }


    @FXML
    void renamePicAction(ActionEvent event) {
        thumbsReview.renameImages(thumbsReview.getSelectedImages());
    }

    @FXML
    void deletePicAction(ActionEvent event) {
        thumbsReview.deleteImages();
    }

    /**
     * 操作面板初始化
     */
    private void initControlPane() {
        openPicButton.setGraphic(new ImageView("/resource/icon/pic.png"));
        copyPicButton.setGraphic(new ImageView("/resource/icon/copy.png"));
        renamePicButton.setGraphic(new ImageView("/resource/icon/rename.png"));
        deletePicButton.setGraphic(new ImageView("/resource/icon/garbage.png"));
        openPicButton.setDisable(true);
        deletePicButton.setDisable(true);
        copyPicButton.setDisable(true);
        renamePicButton.setDisable(true);
    }


    /*******************************
     * 信息栏                       *
     *******************************/
    @FXML
    private Text message1;

    @FXML
    private Text message2;

    public void setMessage1() {
        message1.setText(null);
    }

    public void setMessage1(String s) {
        message1.setText(s);
    }

    public void setMessage2() {
        message2.setText(null);
    }

    public void setMessage2(String s, String color, int millis) {
        new Thread(() -> {
            message2.setStyle("-fx-fill: " + color + ";");
            message2.setText(s);
            try {
                Thread.sleep(millis);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setMessage2();
        }).start();
    }

}