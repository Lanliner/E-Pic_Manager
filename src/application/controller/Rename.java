package application.controller;

import application.model.Menu;
import application.model.Util;
import application.model.picList.ImageNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.model.renamePic.*;

public class Rename implements Initializable {

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
        mode = 0;
        namePane.setVisible(false);

        //初始化表格
        createTable();

        countText.setText(String.valueOf(picTable.getItems().size()));

        modeSwitchButton.setGraphic(new ImageView(new Image("/resource/icon/switch.png")));
    }

    /*******************************
     * 重命名图片文件列表              *
     *******************************/

    @FXML
    private TableView<RenameData> picTable;

    @FXML
    private TableColumn<RenameData, Integer> numColumn;

    @FXML
    private TableColumn<RenameData, String> oldNameColumn;

    @FXML
    private TableColumn<RenameData, String> newNameColumn;

    /**
     * 创建文件列表
     */
    private void createTable() {
        int count = 0;

        for(ImageNode x : Menu.mainController.getThumbsReview().getSelectedImages()) {
            count++;
            picTable.getItems().add(new RenameData(x, count));
        };

        numColumn.setCellValueFactory(new PropertyValueFactory<RenameData, Integer>("num"));
        oldNameColumn.setCellValueFactory(new PropertyValueFactory<RenameData, String>("oldName"));
        newNameColumn.setCellValueFactory(new PropertyValueFactory<RenameData, String>("newName"));

        picTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                picTable.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * 根据参数更新表格
     */
    @SuppressWarnings("DuplicatedCode")
    private void updateTable() {
        if(mode == 0) {
            String name;
            int start;
            int bit;
            try{
                name = num_name.getText().trim();
                start = Integer.parseInt(num_start.getText());
                bit = Integer.parseInt(num_bit.getText());
            } catch(Exception e) {
                Util.showMessage(Alert.AlertType.ERROR, "错误", "非法值，请检查输入",true);
                Util.clearTextField(num_name, num_start, num_bit);
                return;
            }
            int newBit = RenameUtil.preview(picTable, name, start, bit);
            num_bit.setText(String.valueOf(newBit));
        } else {
            RenameUtil.preview(picTable, name_prefix.getText(), name_postfix.getText());
        }
    }

    /*******************************
     * 操作面板                      *
     *******************************/

    private int mode;       //0:编号模式  1:前后缀模式

    @FXML
    private Text countText;

    @FXML
    private AnchorPane textPane;

    @FXML
    private AnchorPane numPane;

    @FXML
    private TextField num_name;

    @FXML
    private TextField num_start;

    @FXML
    private TextField num_bit;

    @FXML
    private AnchorPane namePane;

    @FXML
    private TextField name_prefix;

    @FXML
    private TextField name_postfix;

    @FXML
    private Button modeSwitchButton;

    @FXML
    private Button previewButton;

    @FXML
    private Button confirmButton;

    @FXML
    void modeSwitchAction(ActionEvent event) {
        if(mode == 0) {
            Util.clearTextField(num_name, num_start, num_bit);

            mode = 1;
            updateTable();

            numPane.setVisible(false);
            namePane.setVisible(true);
        } else {
            Util.clearTextField(name_prefix, name_postfix);

            updateTable();
            mode =0;

            numPane.setVisible(true);
            namePane.setVisible(false);
        }
    }

    @FXML
    void previewAction(ActionEvent event) {
        updateTable();
    }

    @FXML
    @SuppressWarnings("DuplicatedCode")
    void confirmAction(ActionEvent event) {
        if(mode == 0) {
            String name;
            int start;
            int bit;
            try{
                name = num_name.getText();
                start = Integer.parseInt(num_start.getText());
                bit = Integer.parseInt(num_bit.getText());
            } catch(Exception e) {
                Util.showMessage(Alert.AlertType.ERROR, "错误", "非法值，请检查输入",true);
                Util.clearTextField(num_name, num_start, num_bit);
                return;
            }
            RenameUtil.rename(picTable, name, start, bit);
        } else {
            RenameUtil.rename(picTable, name_prefix.getText(), name_postfix.getText());
        }
        stage.close();
    }
}
