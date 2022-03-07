package application.model.appearance;

import javafx.scene.control.ButtonBase;

import application.model.Util;

public class CustomStyle {

    public static <E extends ButtonBase> void setButtonStyleAlpha(E ... buttons) {
        for(ButtonBase x : buttons) {
            x.setStyle(null);

            int[] arrayRGB = Util.convertStringToRGB(Config.getThemeColor());
            //按钮背景颜色
            x.setStyle("-fx-background-color: TRANSPARENT");
            x.setOnMouseEntered(event -> {
                x.setStyle(String.format(
                        "-fx-background-color: rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.4));
            });
            x.setOnMouseExited(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
            x.setOnMousePressed(event -> {
                x.setStyle(String.format(
                        "-fx-background-color: rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.8));
            });
            x.setOnMouseReleased(event -> {
                x.setStyle(String.format(
                        "-fx-background-color: rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.4));
            });
        }
    }

    public static <E extends ButtonBase> void setButtonStyleBeta(E ... buttons) {
        for(ButtonBase x : buttons) {
            x.setStyle(null);

            int[] arrayRGB = Util.convertStringToRGB(Config.getThemeColor());
            //设置背景颜色
            x.setStyle("-fx-background-color: TRANSPARENT");
            x.setOnMouseEntered(event -> {
                x.setStyle(String.format(
                        "-fx-background-color: rgba(%d, %d, %d, %f)", arrayRGB[0], arrayRGB[1], arrayRGB[2], 0.4));
            });
            x.setOnMouseExited(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
            x.setOnMouseClicked(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
        }
    }


}
