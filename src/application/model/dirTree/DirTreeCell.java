package application.model.dirTree;

import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class DirTreeCell extends TreeCell<DirItemData> {
    @Override
    protected void updateItem(DirItemData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setGraphic(null);
            this.setText("");
        } else if (item.getFile() == null) {
            this.setGraphic(new ImageView("/resource/icon/desktop.png"));
            this.setText(item.toString());
        } else if (item.getFile().getPath().endsWith(":" + File.separator)) {
            this.setGraphic(new ImageView("/resource/icon/disk.png"));
            this.setText(item.toString());
        } else {
            this.setGraphic(new ImageView("/resource/icon/folder.png"));
            this.setText(item.toString());
        }
    }
}
