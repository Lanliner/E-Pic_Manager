package application.model.picList;

import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.File;

public class TextNode extends Text {

    private File file;
    private String imgRealName;
    private String nameDisplayed;

    public File getFile() {
        return file;
    }
    public String getImgRealName() {
        return imgRealName;
    }
    public String getNameDisplayed() {
        return nameDisplayed;
    }

    public TextNode(File file, double maxWidth){
        this.file=file;
        imgRealName=file.getName();
        setWrappingWidth(maxWidth);
        setTextAlignment(TextAlignment.CENTER);
        setNameDisplayed(imgRealName);
    }

    public void setNameDisplayed(String imgRealName){
        //图片文件最大显示行数
        int numOfLinesDisplayed=4;
        //每行最大字数（WrappingWidth以像素为单位，自定义的textCharsWidth以字符数为单位）
        double textCharsWidth=getWrappingWidth()/10;

        this.imgRealName=imgRealName;
        if((double)imgRealName.length()<=numOfLinesDisplayed*textCharsWidth){
            nameDisplayed=imgRealName;
        }
        else{
            nameDisplayed=imgRealName.substring(0,(int)(numOfLinesDisplayed*textCharsWidth))+"...";
        }
        setText(nameDisplayed);
    }

    public void setWidth(double maxWidth){
        setWrappingWidth(maxWidth);
        setNameDisplayed(imgRealName);
    }

}
