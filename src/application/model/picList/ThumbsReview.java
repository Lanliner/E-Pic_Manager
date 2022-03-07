package application.model.picList;

import application.controller.Display;
import application.model.appearance.Config;
import application.model.picList.acSearch.ACAutomation;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.model.Menu;
import application.model.Util;
import javafx.stage.Stage;

public class ThumbsReview {
    /**
     * members
     */
    private static ArrayList<ImageNode> copyImages = new ArrayList<>();

    private File directory; // 当前目录

    private ArrayList<Display> displayWindows = new ArrayList<>();

    private ContextMenuItem.Order currentOrder = ContextMenuItem.Order.NAME;

    private AnchorPane anchorPane;
    private FlowPane flowPane;
    private ScrollPane scrollPane;

    private ArrayList<ImageNode> imageNodes;

    private ArrayList<ImageNode> selectedImages;

    private RectangleDragged rectangleDragged;
    private ContextMenuItem imageContextMenu;
    private ContextMenuItem blankContextMenu;

    private static String imageSelectedColor;
    private static String imageMouseEnteredColor;
    private static String rectangleDraggedColor;



    /**
     * methods
     */

    public ContextMenuItem.Order getCurrentOrder() {
        return currentOrder;
    }

    public File getDirectory() {
        return directory;
    }

    public Parent getRootPane() {
        return anchorPane;
    }

    public boolean isEmpty() {
        return imageNodes.isEmpty();
    }

    public ArrayList<ImageNode> getImageNodes() {
        return imageNodes;
    }

    public ArrayList<ImageNode> getSelectedImages() {
        return selectedImages;
    }

    public ArrayList<Display> getDisplayWindows() {
        return displayWindows;
    }

    public ThumbsReview(File directory) {
        double margin=10;

        this.directory=directory;

        imageNodes = new ArrayList<>();

        flowPane = new FlowPane();

        File[] subFiles=directory.listFiles();
        if(subFiles.length != 0) {
            for(File subFile:subFiles){
                String name=subFile.getName();
                if(subFile.isFile() && !subFile.isHidden() &&
                        (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".png") || name.endsWith(".bmp"))){
                    ImageNode imageNode = new ImageNode(subFile);
                    imageNodes.add(imageNode);
                    flowPane.getChildren().add(imageNode);
                }
            }
        }

        flowPane.setHgap(2 * margin);
        flowPane.setVgap(margin);
        flowPane.setRowValignment(VPos.TOP);
        flowPane.setPadding(new Insets(margin, margin, margin, margin));

        scrollPane=new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(flowPane);

        anchorPane=new AnchorPane();
        Util.setAnchor(scrollPane, 0.0, 0.0, 0.0, 0.0);
        anchorPane.getChildren().add(scrollPane);

        setTheme();
    }

    public static void setTheme(){
        int[] arrayRGB = Util.convertStringToRGB(Config.getThemeColor());

        imageMouseEnteredColor = String.format("rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.3);
        rectangleDraggedColor = String.format("rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.6);
        imageSelectedColor = String.format("rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.7);
    }


    public void eventHandlingActivates() {
        //记录上次点击选择过的图片框
        selectedImages =new ArrayList<>();
        imageContextMenu =new ContextMenuItem(ContextMenuItem.ContextMenuItemType.IMAGE);
        blankContextMenu =new ContextMenuItem(ContextMenuItem.ContextMenuItemType.BLANK);
        for(ImageNode imageNode : imageNodes){
            imageNodeEventHandling(imageNode);
        }
        anchorPaneEventHandling(imageNodes);
        contextMenuEventHandling();
    }

    /**
     *  取消选中已选择的图片
     */
    public void unSelectImages(){
        while(!selectedImages.isEmpty()){
            int lastImgIndex = selectedImages.size()-1;
            ImageNode imageNode =selectedImages.remove(lastImgIndex);
            imageNode.setSelected(false);
            imageNode.setStyle(null);
        }
    }

    /**
     *  删除选中的一个或多个文件
     */
    public void deleteImages(){
        if(Util.showMessage(Alert.AlertType.CONFIRMATION, "删除图片", "确定删除选中的"
                + (selectedImages.size() == 1 ? "这" : (" " + selectedImages.size()) + " ")
                + "张图片吗？", false)) {

            while(!selectedImages.isEmpty()){

                int lastImgIndex = selectedImages.size()-1;
                ImageNode imageNode = selectedImages.remove(lastImgIndex);

                int[] newIndex = new int[displayWindows.size()];
                for(int i = 0; i < displayWindows.size(); i++) {
                    Display display = displayWindows.get(i);
                    if(imageNodes.indexOf(imageNode) < display.getCurImageNodeIndex()) {
                        //待删节点在当前展示图片节点之前
                        newIndex[i] = display.getCurImageNodeIndex() - 1;
                    } else {
                        //待删节点在当前展示图片节点或之后
                        if(imageNodes.size() == 1) {
                            display.getStage().close();
                            displayWindows.remove(i);
                            i--;
                        } else {
                            newIndex[i] = display.getCurImageNodeIndex() % (imageNodes.size() - 1);
                        }
                    }
                }
                imageNodes.remove(imageNode);

                for(int i = 0; i < displayWindows.size(); i++) {
                    displayWindows.get(i).currentUpdate(newIndex[i]);
                }

                imageNode.getFile().delete();

            }
            refresh(currentOrder);
            updateInterface();
            Menu.mainController.setMessage2("删除图片完成", "green", 1000);
        }
    }

    /**
     * 复制选中的一个或多个文件
     * @param selectedImages 选中的文件们
     */
    public void copyImages(ArrayList<ImageNode> selectedImages){
        copyImages.clear();
        for(ImageNode imgNode:selectedImages){
            copyImages.add(imgNode);
        }
        Menu.mainController.setMessage2("复制图片完成", "green", 1000);
    }

    /**
     * 处理粘贴文件时出现同名的情况，同名文件变为“...-副本.jpg”、“...-副本(2).jpg”等
     */
    private String sameNameHandles(String name) {
        int lastIndexOfDot=name.lastIndexOf(".");
        String newName = name.substring(0,lastIndexOfDot)+" - 副本"+name.substring(lastIndexOfDot);

        int i=1;
        SEARCH_AGAIN:
        while (true){
            for(File file:directory.listFiles()){
                if(file.isFile()&&newName.equals(file.getName())){
                    i++;
                    newName = String.format(file.getName().substring(0,file.getName().lastIndexOf(" - 副本"))
                            +" - 副本(%d)"+file.getName().substring(file.getName().lastIndexOf(".")),i);
                    continue SEARCH_AGAIN;
                }
            }
            break SEARCH_AGAIN;
        }
        return newName;
    }

    /**
     *  粘贴已复制的一个或多个文件，可多次粘贴，并处理同名情况
     */
    private void pasteImages() {
        for(ImageNode imageNode:copyImages){

            String newImageName = imageNode.getFile().getName();
            for(File file:directory.listFiles()){
                if(newImageName.equalsIgnoreCase(file.getName())){
                    newImageName=sameNameHandles(file.getName());
                    break;
                }
            }
            File newImageFile = new File(directory,newImageName);
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageNode.getFile()));
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newImageFile))) {

                byte[] content = new byte[60000];
                while (bis.read(content) != -1) {
                    bos.write(content);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            ImageNode newImageNode = new ImageNode(newImageFile);
            imageNodeEventHandling(newImageNode);
            imageNodes.add(newImageNode);

            ImageNode[] tempNodes = new ImageNode[displayWindows.size()];
            for(int i = 0; i < displayWindows.size(); i++) {
                tempNodes[i] = imageNodes.get(displayWindows.get(i).getCurImageNodeIndex());
            }

            refresh(currentOrder);
            updateInterface();

            for(int i = 0; i < displayWindows.size(); i++) {
                displayWindows.get(i).currentUpdate(imageNodes.indexOf(tempNodes[i]));
            }

        }
    }

    public enum SortFlag {
        SORT
    }

    public void refresh(ContextMenuItem.Order order, SortFlag sortFlag){
        unSelectImages();

        if(isSearchMode){
            isSearchMode = false;
            unSelectImages();
            Menu.mainController.cancelSearchButton.setVisible(false);
            Menu.mainController.searchField.setText("");
            updateInterface();
        }

        flowPane.getChildren().clear();

        currentOrder = order;

        switch (order){
            case DATE:
                Collections.sort(imageNodes,(n1,n2)->Long.compare(n1.getFile().lastModified(),n2.getFile().lastModified()));
                break;
            case TYPE:
                Collections.sort(imageNodes,(n1,n2)->{
                    String name1 = n1.getFile().getName();
                    String type1 = name1.substring(name1.lastIndexOf("."));
                    String name2 = n2.getFile().getName();
                    String type2 = name2.substring(name2.lastIndexOf("."));
                    return type1.compareTo(type2);
                });
                break;
            case FILE_SIZE:
                Collections.sort(imageNodes,(n1,n2)->Long.compare(n1.getFile().length(),n2.getFile().length()));
                break;
            case REVERSE:
                for(int i=0; i<imageNodes.size() / 2; i++){
                    ImageNode temp = imageNodes.get(i);
                    imageNodes.set(i, imageNodes.get(imageNodes.size() - 1 - i ));
                    imageNodes.set(imageNodes.size() - 1 - i, temp);
                }
                break;
            default:
                Collections.sort(imageNodes,(n1,n2)->n1.getFile().getName().compareTo(n2.getFile().getName()));
                break;
        }

        for(ImageNode imageNode : imageNodes){
            imageNode.setStyle(null);
            flowPane.getChildren().add(imageNode);
        }

        if(imageNodes.isEmpty()) {
            flowPane.getChildren().add(new ImageView("/resource/empty.png"));
        }
    }

    /**
     *  清空原来的界面，重新装入图片缩略图节点，按文件字典顺序由小到大排序
     */
    public void refresh(ContextMenuItem.Order order){
        unSelectImages();

        if(isSearchMode){
            isSearchMode = false;
            unSelectImages();
            Menu.mainController.cancelSearchButton.setVisible(false);
            Menu.mainController.searchField.setText("");
            updateInterface();
        }

        flowPane.getChildren().clear();

        currentOrder = order;

        switch (order){
            case DATE:
                Collections.sort(imageNodes,(n1,n2)->Long.compare(n1.getFile().lastModified(),n2.getFile().lastModified()));
                break;
            case TYPE:
                Collections.sort(imageNodes,(n1,n2)->{
                    String name1 = n1.getFile().getName();
                    String type1 = name1.substring(name1.lastIndexOf("."));
                    String name2 = n2.getFile().getName();
                    String type2 = name2.substring(name2.lastIndexOf("."));
                    return type1.compareTo(type2);
                });
                break;
            case FILE_SIZE:
                Collections.sort(imageNodes,(n1,n2)->Long.compare(n1.getFile().length(),n2.getFile().length()));
                break;
            case REVERSE:
                break;
            default:
                Collections.sort(imageNodes,(n1,n2)->n1.getFile().getName().compareTo(n2.getFile().getName()));
                break;
        }

        for(ImageNode imageNode : imageNodes){
            imageNode.setStyle(null);
            flowPane.getChildren().add(imageNode);
        }

        if(imageNodes.isEmpty()) {
            flowPane.getChildren().add(new ImageView("/resource/empty.png"));
        }
    }


    public void updateInterface() {
        if(isSearchMode) {
            return;
        }
        long allImagesSize = 0;
        long selectedImagesSize = 0;

        for(ImageNode imageNode : imageNodes) {
            allImagesSize += imageNode.getFile().length();
            if(imageNode.isSelected()){
                selectedImagesSize += imageNode.getFile().length();
            }
        }

        String allImagesString = String.format("当前目录下共有 %d 张图片(%s)",imageNodes.size(), Util.translateSize(allImagesSize));
        String selImagesString = String.format("已选中 %d 张图片(%s)",selectedImages.size(), Util.translateSize(selectedImagesSize));
        Menu.mainController.picCountText.setText(allImagesString);
        if(selectedImages.size() > 0) {

            Menu.mainController.picCountText.setText(allImagesString + ", " + selImagesString);

            Menu.mainController.copyPicButton.setDisable(false);
            Menu.mainController.renamePicButton.setDisable(false);
            Menu.mainController.deletePicButton.setDisable(false);
        } else {
            Menu.mainController.copyPicButton.setDisable(true);
            Menu.mainController.renamePicButton.setDisable(true);
            Menu.mainController.deletePicButton.setDisable(true);
        }
    }

    /**
     * newName中存在文件名非法字符则返回true
     * @param newName 文件名（包括扩展名）
     * @return newName中存在文件名非法字符则返回true
     */
    private boolean illegalCharsMatches(String newName){
        Pattern pattern = Pattern.compile("[\\\\/:*?\"<>|]");
        Matcher matcher = pattern.matcher(newName);
        if(matcher.find()){
            return true;
        }else{
            return false;
        }
    }

    /**
     *如果该文件夹中存在同名文件则返回true
     * @param newName 文件名（包括扩展名）
     * @return 如果该文件夹中存在同名文件则返回true
     */
    private boolean isSameNameFileExists(String newName){
        for(ImageNode imageNode : imageNodes){
            if(newName.equalsIgnoreCase(imageNode.getFile().getName())){
                return true;
            }
        }
        return false;
    }

    /**
     *  重命名选中的一个或多个文件
     * @param selectedImages 选中的文件们
     */
    public void renameImages(ArrayList<ImageNode> selectedImages){
        // 重命名一个文件
        if(selectedImages.size() == 1){
            ImageNode imageNode = selectedImages.get(selectedImages.size()-1);
            // 打开单个文件重命名窗口
            SingleRenameStage singleRenameStage = new SingleRenameStage(imageNode.getFile().getName());
            singleRenameStage.show();
            // 点击取消
            singleRenameStage.getCancel().setOnMouseClicked(event -> {
                singleRenameStage.close();
            });
            // 点击确认
            singleRenameStage.getConfirm().setOnMouseClicked(event -> {
                // 新文件名（包含扩展名）
                String newName = singleRenameStage.getTextField().getText() + singleRenameStage.getFileExtensions().getText();
                // 是否存在非法字符
                if(illegalCharsMatches(newName)){
                    singleRenameStage.setErrorReport(SingleRenameStage.ErrorType.ILLEGAL_CHARS);
                }// 是否存在同名
                else if(isSameNameFileExists(newName)){
                    singleRenameStage.setErrorReport(SingleRenameStage.ErrorType.SAME_NAME_FILE_EXISTS);
                }
                else {
                    singleRenameStage.clearErrorReport();
                    File source = imageNode.getFile();

                    //记录展示窗口的重命名情况
                    ImageNode[] temp = new ImageNode[displayWindows.size()];
                    boolean[] isRenamed = new boolean[displayWindows.size()];
                    for(int i = 0; i < displayWindows.size(); i++) {
                        temp[i] = imageNodes.get(displayWindows.get(i).getCurImageNodeIndex());
                        isRenamed[i] = temp[i].getFile().equals(source);
                    }

                    imageNodes.remove(imageNode);
                    File target = new File(directory,newName);
                    try {
                        Files.move(source.toPath(),target.toPath());
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    ImageNode newImageNode = new ImageNode(target);
                    imageNodeEventHandling(newImageNode);
                    imageNodes.add(newImageNode);
                    singleRenameStage.close();
                    refresh(currentOrder);
                    updateInterface();

                    //重定位展示窗口
                    for(int i = 0; i < displayWindows.size(); i++) {
                        if(isRenamed[i]) {
                            displayWindows.get(i).currentUpdate(imageNodes.indexOf(newImageNode));
                        } else {
                            displayWindows.get(i).currentUpdate(imageNodes.indexOf(temp[i]));
                        }
                    }
                }
            });


        }else {     //批量重命名
            try {
                Menu.initRenamePage(new Stage());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    /** 设置查看大图标或小图标
     *
     * @param size Big or Small
     */
    private void setView(ImageNode.Size size){
        ImageNode.setSize(size);
        for(ImageNode imageNode : imageNodes){
            imageNode.resize();
        }
        refresh(currentOrder);
    }

    private void contextMenuEventHandling() {
        imageContextMenu.getDelete().setOnAction(event -> deleteImages());
        imageContextMenu.getCopy().setOnAction(event -> copyImages(selectedImages));
        imageContextMenu.getRename().setOnAction(event -> renameImages(selectedImages));

        blankContextMenu.getPaste().setOnAction(event -> pasteImages());
        blankContextMenu.getRefresh().setOnAction(event -> refresh(currentOrder));
        blankContextMenu.getBigView().setOnAction(event -> setView(ImageNode.Size.Big));
        blankContextMenu.getSmallView().setOnAction(event -> setView(ImageNode.Size.Small));

        blankContextMenu.getSortOrderName().setOnAction(event -> refresh(ContextMenuItem.Order.NAME, SortFlag.SORT));
        blankContextMenu.getSortOrderDate().setOnAction(event -> refresh(ContextMenuItem.Order.DATE, SortFlag.SORT));
        blankContextMenu.getSortOrderFileSize().setOnAction(event -> refresh(ContextMenuItem.Order.FILE_SIZE, SortFlag.SORT));
        blankContextMenu.getSortOrderType().setOnAction(event -> refresh(ContextMenuItem.Order.TYPE, SortFlag.SORT));
        blankContextMenu.getSortReverseOrder().setOnAction(event -> refresh(ContextMenuItem.Order.REVERSE, SortFlag.SORT));

        anchorPane.setOnKeyPressed(event -> {
            if( imageContextMenu.isShowing() || ( !selectedImages.isEmpty() && event.isShortcutDown() ) ){
                Boolean contextMenuClosable = true;
                switch (event.getCode()){
                    case D: deleteImages(); break;
                    case C: copyImages(selectedImages); break;
                    case M: renameImages(selectedImages); break;
                    default: contextMenuClosable = false; break;
                }
                if(contextMenuClosable){
                    imageContextMenu.hide();
                }
            }
            else if(blankContextMenu.isShowing() || event.isShortcutDown() ){
                Boolean contextMenuClosable = true;
                switch (event.getCode()){
                    case V: pasteImages(); break;
                    case E: refresh(currentOrder); break;
                }
                if(contextMenuClosable){
                    blankContextMenu.hide();
                }
            }
        });
    }

    private void anchorPaneEventHandling(ArrayList<ImageNode> imageNodes){
        //点击空白处，将之前选择的图片框恢复原样
        anchorPane.setOnMouseClicked(event-> {
            if(event.isStillSincePress()){
                unSelectImages();
                updateInterface();

                imageContextMenu.hide();
                blankContextMenu.hide();
                if(event.getButton() == MouseButton.SECONDARY){
                    blankContextMenu.show(anchorPane,event.getScreenX(),event.getScreenY());
                    if(copyImages.isEmpty()){
                        blankContextMenu.getPaste().setDisable(true);
                    }else {
                        blankContextMenu.getPaste().setDisable(false);
                    }
                }
            }
        });

        /**
         *  生成拖动鼠标矩形
         */
        rectangleDragged = new RectangleDragged(0,0);

        anchorPane.addEventFilter(MouseEvent.MOUSE_PRESSED,event -> {
            if(!event.isStillSincePress()){
                rectangleDragged.setStyle("-fx-fill: " + rectangleDraggedColor);
                Node scrollBar=scrollPane.lookup(".scroll-bar");
                rectangleDragged.setScrollbarVisible(scrollBar.isVisible());
                rectangleDragged.setScrollbarPosX(scrollBar.getBoundsInParent().getMinX());

                anchorPane.getChildren().remove(rectangleDragged);// 清除之前添加的矩形，若没有这句，后面添加新矩形可能报异常
                // 记录拖动鼠标时最初的坐标（矩形锚点）
                rectangleDragged.setAnchorX(event.getX());
                rectangleDragged.setAnchorY(event.getY());

                /**
                 * scrollbar不显示，或scrollbar显示且矩形锚点X不在scrollbar中，才会生成拖拉矩形
                 */
                if( !rectangleDragged.isScrollbarVisible() || rectangleDragged.getAnchorX() < rectangleDragged.getScrollbarPosX() ){
                    rectangleDragged.setX(event.getX());
                    rectangleDragged.setY(event.getY());
                    rectangleDragged.setWidth(0);
                    rectangleDragged.setHeight(0);
                    anchorPane.getChildren().add(rectangleDragged);
                }
            }
        });
        anchorPane.addEventFilter(MouseEvent.MOUSE_DRAGGED,event -> {
            if(!event.isStillSincePress()){
                if(!rectangleDragged.isScrollbarVisible()||rectangleDragged.getAnchorX()<rectangleDragged.getScrollbarPosX()){
                    unSelectImages();

                    double anchorX= rectangleDragged.getAnchorX();
                    double anchorY= rectangleDragged.getAnchorY();
                    double mouseX=event.getX();
                    double mouseY=event.getY();

                    // 改变矩形的长宽和左上角坐标
                    rectangleDragged.setWidth(Math.abs(mouseX-anchorX));//设置矩形框的长和宽
                    rectangleDragged.setHeight(Math.abs(mouseY-anchorY));

                    //向左拉矩形，改变X，不允许处理anchor==mouse的情况  //向右拉矩形，不需要改变
                    if(anchorX>mouseX){
                        rectangleDragged.setX(mouseX);
                    }
                    //向上拉矩形，改变Y  //向下拉矩形，不需要改变
                    if(anchorY>mouseY){
                        rectangleDragged.setY(mouseY);
                    }

                    for(ImageNode image: imageNodes){
                        Bounds imgScreenBounds = image.localToScreen(image.getBoundsInLocal());
                        Bounds rectScreenBounds=rectangleDragged.localToScreen(rectangleDragged.getBoundsInLocal());
                        if (imgScreenBounds != null && imgScreenBounds.intersects(rectScreenBounds)){
                            image.setSelected(true);
                            image.setStyle("-fx-background-color:"+imageMouseEnteredColor);
                        }else{
                            image.setSelected(false);
                            image.setStyle(null);
                        }
                    }
                }
            }
        });
        anchorPane.addEventFilter(MouseEvent.MOUSE_RELEASED,event ->{
            if(!event.isStillSincePress()){
                if(!rectangleDragged.isScrollbarVisible()||rectangleDragged.getAnchorX()<rectangleDragged.getScrollbarPosX()){
                    anchorPane.getChildren().remove(rectangleDragged);
                    for(ImageNode image: imageNodes){
                        if(image.isSelected()){
                            selectedImages.add(image);
                            image.setStyle("-fx-background-color:"+imageSelectedColor);
                        }
                    }
                }
                updateInterface();
            }
        });
    }

    public void imageNodeEventHandling(ImageNode imageNode){
        //图片框上的光标变为手指
        imageNode.setCursor(Cursor.HAND);

        imageNode.getTextNode().setOnMouseClicked(event -> {
            if(event.isStillSincePress()&& imageNode.isSelected()){

            }
        });

        imageNode.setOnMouseClicked(event->{
            if(event.isStillSincePress()){
                // 若为单选，且之前点击过其它图片框，则将这些图片框恢复原样
                if( !event.isShortcutDown() || event.getButton()==MouseButton.SECONDARY ){
                    if(selectedImages.size()<=1 || event.getButton()==MouseButton.PRIMARY || !imageNode.isSelected()){
                        unSelectImages();
                    }
                }
                // 若图片框未被选择，则光标点击图片框，图片框变色
                if(!imageNode.isSelected()){
                    selectedImages.add(imageNode);
                    imageNode.setSelected(true);
                    imageNode.setStyle("-fx-background-color:"+imageSelectedColor);
                } // 若图片框已被选择，且已按下ctrl键，则光标点击图片框，取消选择该图片框
                else if(event.isShortcutDown()) {
                    selectedImages.remove(imageNode);
                    imageNode.setSelected(false);
                    imageNode.setStyle(null);
                }

                imageContextMenu.hide(); // 先隐藏两种contextMenu
                blankContextMenu.hide();
                // 右键显示imageContextMenu
                if(event.getButton()==MouseButton.SECONDARY){
                    imageContextMenu.show(anchorPane,event.getScreenX(),event.getScreenY());
                }

                if(event.getClickCount() == 2){
                    try {
                        Menu.mainController.openPicAction(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                event.consume();//鼠标点击事件不再向上冒泡
                updateInterface();
            }
        });

        //光标移动到图片框上，图片框变色。光标离开图片框，则图片框恢复原样
        imageNode.setOnMouseEntered(event->{
            if(!imageNode.isSelected()){
                imageNode.setStyle("-fx-background-color:"+imageMouseEnteredColor);
            }
        });
        imageNode.setOnMouseExited(event->{
            if(!imageNode.isSelected()){
                imageNode.setStyle(null);
            }
        });
    }

    private boolean isSearchMode = false;

    public void searchImages(String name) {

        isSearchMode = true;

        ArrayList<ImageNode> searchedImages = new ArrayList<>();

        int[] searchedImagesIndex;
        int searchedCount = 0;
        searchedImagesIndex = ACAutomation.fileCheck(imageNodes, name);

        flowPane.getChildren().clear();

        boolean[] vis=new boolean[imageNodes.size()];

        for(int i=0;i<imageNodes.size();i++)    vis[i]=false;

        for(int i=0; i<ACAutomation.getFinalCounts(); i++) {
            if(!vis[searchedImagesIndex[i]]) {

                ImageNode imageNode = imageNodes.get(searchedImagesIndex[i]);

                searchedImages.add(imageNode);

                flowPane.getChildren().add(imageNode);

                vis[searchedImagesIndex[i]]=true;
                searchedCount++;
            }
        }
        if(searchedCount > 0) {

            Menu.mainController.picCountText.setText(
                    String.format("搜索到 %d 张图片", searchedCount));

            Menu.mainController.copyPicButton.setDisable(false);
            Menu.mainController.renamePicButton.setDisable(false);
            Menu.mainController.deletePicButton.setDisable(false);
        } else {
            Menu.mainController.picCountText.setText("没有搜索到图片");
            flowPane.getChildren().add(new ImageView("/resource/empty.png"));

            Menu.mainController.copyPicButton.setDisable(true);
            Menu.mainController.renamePicButton.setDisable(true);
            Menu.mainController.deletePicButton.setDisable(true);
        }
    }

    public void cancelSearch() {
        isSearchMode = false;
        unSelectImages();
        refresh(currentOrder);
        updateInterface();
    }
}