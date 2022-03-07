package application.model.renamePic;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;

import application.model.picList.ImageNode;

public class RenameData {

    private ImageNode imageNode;
    private File file;
    private SimpleIntegerProperty num = new SimpleIntegerProperty();
    private SimpleStringProperty oldName = new SimpleStringProperty();
    private SimpleStringProperty newName = new SimpleStringProperty();

    public RenameData(ImageNode imageNode, int num) {
        this.imageNode = imageNode;
        this.file = imageNode.getFile();
        this.num.set(num);
        this.oldName.set(file.getName());
        this.newName.set(file.getName());
    }

    public ImageNode getImageNode() {
        return imageNode;
    }

    public void setImageNode(ImageNode imageNode) {
        this.imageNode = imageNode;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getNum() {
        return num.get();
    }

    public void setNum(int num) {
        this.num.set(num);
    }

    public String getOldName() {
        return oldName.get();
    }

    public void setOldName(String oldName) {
        this.oldName.set(oldName);
    }

    public String getNewName() {
        return newName.get();
    }

    public void setNewName(String newName) {
        this.newName.set(newName);
    }

}
