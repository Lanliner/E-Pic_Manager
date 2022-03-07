package application.model;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import application.model.appearance.Config;


public class Util {

    /**
     * 清空多个文本框
     * @param inputControls 文本框
     */
    public static void clearTextField(TextInputControl... inputControls) {
        for (TextInputControl inputControl : inputControls) {
            inputControl.clear();
        }
    }

    /**
     * 设置节点锚定
     * @param node 节点
     * @param top 顶间距
     * @param bottom 底间距
     * @param left 左间距
     * @param right 右间距
     */
    public static void setAnchor(Node node, Double top, Double bottom, Double left, Double right) {
        if(top != null) {
            AnchorPane.setTopAnchor(node, top);
        }
        if(bottom != null) {
            AnchorPane.setBottomAnchor(node, bottom);
        }
        if(left != null) {
            AnchorPane.setLeftAnchor(node, left);
        }
        if(right != null) {
            AnchorPane.setRightAnchor(node, right);
        }
    }

    /**
     * 将Color转换成16位RGB字符串
     * @param color 颜色类对象
     * @return 带#号的16位RGB字符串
     */
    public static String convertColorToString(Color color) {
        return String.format("#%02X%02X%02X", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    /**
     * 将16位RGB字符串转换成RGB数组
     * @param rgb 带#号的16位RGB字符串
     * @return RGB整型数组
     */
    public static int[] convertStringToRGB(String rgb) {
        int[] arrayRGB = new int[3];
        arrayRGB[0] = Integer.parseInt(rgb.substring(1,3), 16);
        arrayRGB[1] = Integer.parseInt(rgb.substring(3,5), 16);
        arrayRGB[2] = Integer.parseInt(rgb.substring(5,7), 16);
        return arrayRGB;
    }

    /**
     * 将long表示的大小转换为字符串
     * @param length 以long表示的日期
     * @return 转换得到的字符串，单位不定
     */
    public static String translateSize(long length) {
        if(length == 0) {
            return "0B";
        }
        double sizeKB = (double)length / 1024;
        if(sizeKB >= 1024) {
            return String.format("%.2f MB", sizeKB / 1024);
        } else {
            return String.format("%.0f KB", sizeKB);
        }
    }

    /**
     * 将long表示的日期转换为字符串
     * @param date 以long表示的日期
     * @return 转换得到的字符串，格式"年-月-日 时:分:秒 星期"
     */
    public static String translateDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss   EEEE");
        return sdf.format(new Date(date));
    }

    /**
     * 弹出警报窗口
     * @param alertType 警报类型
     * @param title 警报标题
     * @param content 警报内容
     * @param beep 是否发声
     * @return 用户选择（只有警报类型为CONFIRMATION时有意义）
     */
    public static boolean showMessage(Alert.AlertType alertType, String title, String content, boolean beep) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if(beep) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
        Optional<ButtonType> opt = alert.showAndWait();
        if(opt.isPresent() && opt.get().equals(ButtonType.CANCEL)) {
            return false;
        }
        return true;
    }

    /**
     * 导入配置文件
     */
    public static void configInput() {
        File f = new File("config.dat");
        try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
            Class<?> clz = Config.class;
            Object obj = clz.getDeclaredConstructor().newInstance();    //反射实例化
            String str;
            while((str = reader.readLine()) != null) {
                setValue(obj, str);     //设置属性
            }
        } catch (FileNotFoundException ex) {
            //配置文件不存在，使用默认主题
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出配置文件
     */
    public static void configOutput() {
        File f = new File("config.dat");
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            Class<?> clz = Config.class;
            Object obj = clz.getDeclaredConstructor().newInstance();    //反射实例化
            for(Field field : obj.getClass().getDeclaredFields()) {
                String getMethodName = "get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
                Method getMethod = obj.getClass().getDeclaredMethod(getMethodName);
                writer.write(field.getName() + ":" + getMethod.invoke(obj).toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过反射批量设置指定对象的属性
     * @param obj 需要设置属性的对象
     * @param values 包含各项属性的字符串，每项格式“属性 + : + 内容 + 说明部分”
     */
    public static void setValue(Object obj, String ... values) {
        for(String x : values) {
            String[] seg = x.split(":|\\s+");
            try{
                //反射调用setter
                Field field = obj.getClass().getDeclaredField(seg[0]);
                String setMethodName = "set" + seg[0].substring(0,1).toUpperCase() + seg[0].substring(1);
                Method setMethod = obj.getClass().getDeclaredMethod(setMethodName, field.getType());
                Object value = convertAttributeValue(field.getType().getName(), seg[1]);
                setMethod.invoke(obj, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实现属性类型转换处理
     * @param type 属性类型
     * @param value 属性内容
     * @return 转换后的目标类型数据
     */
    public static Object convertAttributeValue(String type, String value) {
        if("int".equals(type) || "java.lang.Integer".equals(type)) {
            return Integer.parseInt(value);
        } else if ("double".equals(type) || "java.lang.Double".equals(type)) {
            return Double.parseDouble(value);
        } else {
            return value;
        }
    }

}
