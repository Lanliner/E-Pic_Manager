package application.model.renamePic;

import application.model.picList.ThumbsReview;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;

import application.model.Menu;
import application.model.Util;
import application.model.picList.ImageNode;

public class RenameUtil {

    /**
     * 预览重命名表中文件（前后缀模式）
     * @param picTable 文件表
     * @param prefix 前缀串
     * @param postfix 后缀串
     */
    public static void preview(TableView<RenameData> picTable, String prefix, String postfix) {
        for(RenameData x : picTable.getItems()) {
            String suffix = x.getOldName().substring(x.getOldName().lastIndexOf("."));  //取得扩展名
            String name = x.getOldName().substring(0, x.getOldName().lastIndexOf("."));     //取得文件名(不含扩展名)
            x.setNewName(prefix + name + postfix + suffix);  //拼接新文件名
        }
        picTable.refresh(); //刷新表格
    }

    /**
     * 进行前后缀模式重命名
     */
    public static void rename(TableView<RenameData> picTable, String prefix, String postfix) {
        preview(picTable, prefix, postfix);
        renameFile(picTable);
    }

    /**
     * 预览重命名表中文件（编号模式）
     * @param picTable 文件表
     * @param name 新名称
     * @param start 起始编号
     * @param bit 位数
     * @return 新位数
     */
    public static int preview(TableView<RenameData> picTable, String name, int start, int bit) {
        //值范围判断
        if(name.isEmpty()) {
            Util.showMessage(Alert.AlertType.ERROR, "名称错误", "名称不能为空", true);
        } else if(start < 0) {
            Util.showMessage(Alert.AlertType.ERROR, "起始值错误", "起始编号不能为负数", true);
            return bit;
        } else if(bit <=0) {
            Util.showMessage(Alert.AlertType.ERROR, "位数值错误", "位数不能为0或负数", true);
            return bit;
        } else if(start + picTable.getItems().size() -1 > Math.pow(10, bit) -1) {
            Util.showMessage(Alert.AlertType.WARNING, "范围错误", "位数不足，自动扩充位数", true);
            bit = String.valueOf(start + picTable.getItems().size() -1).length();
        }

        int i = start;
        for(RenameData x : picTable.getItems()) {
            String postfix = String.format("%0" + bit + "d", i);
            String suffix = x.getOldName().substring(x.getOldName().lastIndexOf("."));  //取得扩展名
            x.setNewName(name + postfix + suffix);
            i++;
        }

        picTable.refresh();
        return bit;
    }

    /**
     * 进行编号模式重命名
     */
    public static void rename(TableView<RenameData> picTable, String name, int start, int bit) {
        preview(picTable, name, start, bit);
        renameFile(picTable);
    }

    /**
     * 通过Files.move方法重命名文件
     * @param picTable 文件表
     */
    public static void renameFile(TableView<RenameData> picTable) {
        ThumbsReview thumbsReview = Menu.mainController.getThumbsReview();
        ArrayList<ImageNode> imageNodes = thumbsReview.getImageNodes();
        int skipCount = 0;
        for(RenameData x : picTable.getItems()) {
            File source = x.getFile();
            File target = new File(source.getParent() + File.separator + x.getNewName());
            try{

                //记录展示窗口的重命名情况
                ImageNode[] temp = new ImageNode[thumbsReview.getDisplayWindows().size()];
                boolean[] isRenamed = new boolean[thumbsReview.getDisplayWindows().size()];
                for(int i = 0; i < thumbsReview.getDisplayWindows().size(); i++) {
                    temp[i] = imageNodes.get(thumbsReview.getDisplayWindows().get(i).getCurImageNodeIndex());
                    isRenamed[i] = temp[i].getFile().equals(source);
                }

                Files.move(source.toPath(), target.toPath());
                imageNodes.remove(x.getImageNode());
                ImageNode newImageNode = new ImageNode(target);
                imageNodes.add(newImageNode);
                thumbsReview.imageNodeEventHandling(newImageNode);
                thumbsReview.refresh(thumbsReview.getCurrentOrder());
                thumbsReview.updateInterface();

                //重定位展示窗口
                for(int i = 0; i < thumbsReview.getDisplayWindows().size(); i++) {
                    if(isRenamed[i]) {
                        thumbsReview.getDisplayWindows().get(i).currentUpdate(imageNodes.indexOf(newImageNode));
                    } else {
                        thumbsReview.getDisplayWindows().get(i).currentUpdate(imageNodes.indexOf(temp[i]));
                    }
                }

            } catch(FileAlreadyExistsException ex) {
                skipCount++;    //存在同名文件，跳过此文件
            } catch(Exception e) {
                Util.showMessage(Alert.AlertType.ERROR, "错误", "请检查名称是否合法", true);
                return;
            }
        }
        Util.showMessage(Alert.AlertType.INFORMATION, "批量重命名完成",
                picTable.getItems().size() - skipCount + "个文件重命名成功, " + skipCount + "个被跳过",
                false);
    }
}
