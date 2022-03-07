package application.model.appearance;

public class Config {
    private static String themeColor = "#232323";   //主题颜色

    private static String titleColor = "#ffffff";   //标题颜色

    private static double sliceDelay = 2.0;         //幻灯片播放间隔

    public static void setThemeColor(String color) {
        themeColor = color;
    }

    public static void setTitleColor(String color) {
        titleColor = color;
    }

    public static void setSliceDelay(double sliceDelay) {
        Config.sliceDelay = sliceDelay;
    }

    public static String getThemeColor() {
        return themeColor;
    }

    public static String getTitleColor() {
        return titleColor;
    }

    public static double getSliceDelay() {
        return sliceDelay;
    }
}
