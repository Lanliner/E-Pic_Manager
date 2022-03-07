package application.model.picList;

import application.model.appearance.CustomStyle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SingleRenameStage extends Stage{

    public enum ErrorType {
        ILLEGAL_CHARS, SAME_NAME_FILE_EXISTS
    }

    private Text errorReport;
    private TextField textField;
    private Text fileExtensions; // 文件扩展名

    private Button cancel;
    private Button confirm;
    private AnchorPane anchorPane;

    public Text getErrorReport() {
        return errorReport;
    }
    public TextField getTextField() {
        return textField;
    }
    public Text getFileExtensions() {
        return fileExtensions;
    }
    public Button getCancel() {
        return cancel;
    }
    public Button getConfirm() {
        return confirm;
    }

    public SingleRenameStage(String oldName){
        super(StageStyle.UTILITY);
        errorReport = new Text();
        errorReport.setTextAlignment(TextAlignment.LEFT);
        errorReport.setFill(Color.RED);

        textField = new TextField(oldName.substring(0,oldName.lastIndexOf(".")));
        textField.setAlignment(Pos.CENTER);

        fileExtensions = new Text(oldName.substring(oldName.lastIndexOf(".")));
        fileExtensions.setTextAlignment(TextAlignment.CENTER);

        cancel = new Button("取消(N)");
        confirm = new Button("确认(Y)");
        CustomStyle.setButtonStyleAlpha(cancel,confirm);

        anchorPane = new AnchorPane();
        anchorPane.setPrefSize(600,200);
        anchorPane.getChildren().addAll(errorReport,textField,fileExtensions,cancel,confirm);

        AnchorPane.setTopAnchor(errorReport,15.0);
        AnchorPane.setLeftAnchor(errorReport,20.0);
        AnchorPane.setRightAnchor(errorReport,230.0);
        AnchorPane.setBottomAnchor(errorReport,166.0);

        AnchorPane.setTopAnchor(textField,50.0);
        AnchorPane.setLeftAnchor(textField,20.0);
        AnchorPane.setRightAnchor(textField,70.0);
        AnchorPane.setBottomAnchor(textField,100.0);

        AnchorPane.setTopAnchor(fileExtensions,65.0);
        AnchorPane.setLeftAnchor(fileExtensions,540.0);
        AnchorPane.setRightAnchor(fileExtensions,15.0);
        AnchorPane.setBottomAnchor(fileExtensions,115.0);

        AnchorPane.setTopAnchor(confirm,130.0);
        AnchorPane.setLeftAnchor(confirm,50.0);
        AnchorPane.setRightAnchor(confirm,400.0);
        AnchorPane.setBottomAnchor(confirm,20.0);

        AnchorPane.setTopAnchor(cancel,130.0);
        AnchorPane.setLeftAnchor(cancel,400.0);
        AnchorPane.setRightAnchor(cancel,50.0);
        AnchorPane.setBottomAnchor(cancel,20.0);

        setScene(new Scene(anchorPane));
        setAlwaysOnTop(true);
        setTitle("重命名");
        requestFocus();
    }
    public void setErrorReport(ErrorType type){
        if(type == ErrorType.ILLEGAL_CHARS){
            errorReport.setText("文件名不能包含下列任何字符之一： \\ / : * ? < > |");
        }else if(type == ErrorType.SAME_NAME_FILE_EXISTS){
            errorReport.setText("该目录下已存在同名文件");
        }
    }
    public void clearErrorReport(){
        errorReport.setText("");
    }
}
