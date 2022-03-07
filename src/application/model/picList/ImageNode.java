package application.model.picList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.*;

public class ImageNode extends BorderPane {

    public enum Size {
        Big, Small
    }

    private static final double bigThumbWidth = 280;
    private static final double bigThumbHeight = 350;

    private static final double smallThumbWidth = 150;
    private static final double smallThumbHeight = 200;

    private static double maxWidth = smallThumbWidth;
    private static double maxHeight = smallThumbHeight;
    private static double margin = 10;

    private File file;
    private ImageView thumbnailVw;
    private TextNode textNode;
    private BorderPane thumbBorderPane;
    private boolean isSelected =false;


    /**
     * Getter & Setter
     */
    public File getFile() {
        return file;
    }
    public ImageView getThumbnailVw() {
        return thumbnailVw;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public TextNode getTextNode() {
        return textNode;
    }

    public static double getMyImageMaxWidth() {
        return maxWidth;
    }
    public static double getMyImageMaxHeight() {
        return maxHeight;
    }
    public static double getMyImageMargin() {
        return margin;
    }

    /**
     *  默认小图标
     */
    public ImageNode(File file) {

        this.file = file;
        try (FileInputStream fis = new FileInputStream(file)) {
            // 修改Image构造方法的 requestedWidth 和 requestedHeight 来调节Image的大小
            Image img = new Image(fis, smallThumbWidth, smallThumbHeight, true, true);
            thumbnailVw = new ImageView(img);
        } catch (IOException e) {
            e.printStackTrace();
        }

        thumbnailVw.setPreserveRatio(true);
        thumbnailVw.setFitWidth(maxWidth);
        thumbnailVw.setFitHeight(2.0 / 3 * maxHeight);

        BorderPane.setAlignment(thumbnailVw, Pos.CENTER);
        BorderPane.setMargin(thumbnailVw, new Insets(margin, margin, margin, margin));

        thumbBorderPane = new BorderPane();
        thumbBorderPane.setMinHeight(2.4 / 3 * maxHeight);
        thumbBorderPane.setBottom(thumbnailVw);

        textNode = new TextNode(file, maxWidth);
        BorderPane.setAlignment(textNode, Pos.CENTER);
        BorderPane.setMargin(textNode, new Insets(margin, margin, margin, margin));

        setMaxSize(maxWidth, maxHeight);
        setCenter(thumbBorderPane);
        setBottom(textNode);
    }

    /**
     * 设置静态的Size参数，大图标或小图标，默认为小图标
     * @param size Big or Small
     */
    public static void setSize(Size size){
        if(size == Size.Big){
            maxWidth = bigThumbWidth;
            maxHeight = bigThumbHeight;

        }else if(size == Size.Small){
            maxWidth = smallThumbWidth;
            maxHeight = smallThumbHeight;
        }
    }

    /**
     *  设置完静态的size后，需要对每个对象重新设置大小
     */
    public void resize() {
        thumbnailVw.setFitWidth(maxWidth);
        thumbnailVw.setFitHeight(2.0 / 3 * maxHeight);
        thumbBorderPane.setMinHeight(2.4 / 3 * maxHeight);
        textNode.setWidth(maxWidth);
        setMaxSize(maxWidth, maxHeight);
    }
}