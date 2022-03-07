package application.model.appearance;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainUIBorder {

    //窗体拉伸属性
    private static boolean isLeft;                  // 是否处于左边界调整窗口状态
    private static boolean isRight;                 // 是否处于右边界调整窗口状态
    private static boolean isTop;                   // 是否处于上边界调整窗口状态
    private static boolean isTopLeft;               // 是否处于左上角调整窗口状态
    private static boolean isTopRight;              // 是否处于右边界调整窗口状态
    private static boolean isBottom;                // 是否处于下边界调整窗口状态
    private static boolean isBottomLeft;            // 是否处于左下角调整窗口状态
    private static boolean isBottomRight;           // 是否处于右下角调整窗口状态

    private final static int RESIZE_WIDTH = 4;     // 判定是否为调整窗口状态的范围与边界距离
    private final static double MIN_WIDTH = 600;    // 窗口最小宽度
    private final static double MIN_HEIGHT = 400;   // 窗口最小高度
    private static double offsetX = 0;
    private static double offsetY = 0;

    private MainUIBorder() {}

    public static void setCustomResize(Stage stage) {
        Parent root = stage.getScene().getRoot();

        root.setOnMouseMoved(event -> {
            event.consume();
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Cursor cursorType = Cursor.DEFAULT;// 鼠标光标初始为默认类型，若未进入调整窗口状态，保持默认类型

            // 先将所有调整窗口状态重置
            isLeft = isRight = isTop = isBottom =
                    isTopLeft = isTopRight = isBottomLeft = isBottomRight =false;
            if (y <= RESIZE_WIDTH) {
                if (x <= RESIZE_WIDTH) {// 左上角调整窗口状态
                    isTopLeft = true;
                    cursorType = Cursor.NW_RESIZE;
                } else if (x >= width - RESIZE_WIDTH) {// 右上角调整窗口状态
                    isTopRight = true;
                    cursorType = Cursor.NE_RESIZE;
                } else {// 上边界调整窗口状态
                    isTop = true;
                    cursorType = Cursor.N_RESIZE;
                }
            } else if (y >= height - RESIZE_WIDTH) {
                if (x <= RESIZE_WIDTH) {// 左下角调整窗口状态
                    isBottomLeft = true;
                    cursorType = Cursor.SW_RESIZE;
                } else if (x >= width - RESIZE_WIDTH) {// 右下角调整窗口状态
                    isBottomRight = true;
                    cursorType = Cursor.SE_RESIZE;
                } else {// 下边界调整窗口状态
                    isBottom = true;
                    cursorType = Cursor.S_RESIZE;
                }
            } else {
                if (x <= RESIZE_WIDTH) {// 左边界调整窗口状态
                    isLeft = true;
                    cursorType = Cursor.W_RESIZE;
                } else if (x >= width - RESIZE_WIDTH) {// 右边界调整窗口状态
                    isRight = true;
                    cursorType = Cursor.E_RESIZE;
                }
            }
            // 最后改变鼠标光标
            root.setCursor(cursorType);
        });

        root.setOnMouseDragged(event -> {
            double x = event.getScreenX() - stage.getX();
            double y = event.getScreenY() - stage.getY();

            double nextX = stage.getX();
            double nextY = stage.getY();
            double nextWidth = stage.getWidth();
            double nextHeight = stage.getHeight();

            if (isLeft || isTopLeft || isBottomLeft) {// 所有左边界调整窗口状态
                nextX = event.getScreenX();
                nextWidth -= x;
            }
            if (isTop || isTopLeft || isTopRight) {// 所有上边界调整窗口状态
                nextY = event.getScreenY();
                nextHeight -= y;
            }
            if (isRight || isTopRight || isBottomRight) {// 所有右边界调整窗口状态
                nextWidth = x;
            }
            if (isBottom || isBottomLeft || isBottomRight) {// 所有下边界调整窗口状态
                nextHeight = y;
            }
            if (nextWidth <= MIN_WIDTH) {// 如果窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
                nextWidth = MIN_WIDTH;
            }
            if (nextHeight <= MIN_HEIGHT) {// 如果窗口改变后的高度小于最小高度，则高度调整到最小高度
                nextHeight = MIN_HEIGHT;
            }
            // 最后统一改变窗口的x、y坐标和宽度、高度
            stage.setX(nextX);
            stage.setY(nextY);
            stage.setWidth(nextWidth);
            stage.setHeight(nextHeight);
            if (!isTop && !isTopLeft && !isTopRight && !isLeft &&
                    !isRight &&!isBottom && !isBottomLeft && !isBottomRight) {
                stage.setX(event.getScreenX() - offsetX);
                stage.setY(event.getScreenY() - offsetY);
            }
        });

        root.setOnMousePressed(event -> {
            offsetX = event.getScreenX() - stage.getX();
            offsetY = event.getScreenY() - stage.getY();
        });
    }
}
