package application.model.picList;

import javafx.scene.shape.Rectangle;

public class RectangleDragged extends Rectangle {
    private double anchorX;
    private double anchorY;
    private double scrollbarPosX;
    private boolean scrollbarVisible =false;

    public boolean isScrollbarVisible() {
        return scrollbarVisible;
    }

    public void setScrollbarVisible(boolean scrollbarVisible) {
        this.scrollbarVisible = scrollbarVisible;
    }

    public double getScrollbarPosX() {
        return scrollbarPosX;
    }

    public void setScrollbarPosX(double scrollbarPosX) {
        this.scrollbarPosX = scrollbarPosX;
    }

    public double getAnchorX() {
        return anchorX;
    }

    public void setAnchorX(double anchorX) {
        this.anchorX = anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }

    public void setAnchorY(double anchorY) {
        this.anchorY = anchorY;
    }

    public RectangleDragged(double width,double height){
        setWidth(width);
        setHeight(height);
    }
}
