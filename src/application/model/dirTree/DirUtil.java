package application.model.dirTree;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.*;

import java.io.File;

public class DirUtil {
    /**
     * 将节点文件夹的子目录装入TreeItem可观察表
     * @param item 操作节点
     * @return 装入完成后的ObservableList
     */
    public static ObservableList<TreeItem<DirItemData>> convertChildrenToItems(TreeItem<DirItemData> item) {
        File dir = item.getValue().getFile();
        ObservableList<TreeItem<DirItemData>> obl = FXCollections.observableArrayList();
        if(dir == null) {   //盘符
            for(File x : File.listRoots()) {
                obl.add(new TreeItem<DirItemData>(new DirItemData(x)));
            }
        }else {
            File[] list = dir.listFiles();
            if(list != null) {
                for (File x : list) {
                    if (x.isDirectory() && !x.isHidden()) {     //只添加非隐藏目录
                        obl.add(new TreeItem<DirItemData>(new DirItemData(x)));
                    }
                }
            }
        }
        return obl;
    }

    /**
     * 遍历子目录节点并注册展开事件监听器，如果这些子节点没有子节点则尝试将这些节点的子节点接入
     * @param item 操作节点
     */
    public static void addChildrenItems(TreeItem<DirItemData> item) {
        //第一次遍历时加入盘符节点
        if(item.getValue().getFile() == null) {
            item.getChildren().addAll(convertChildrenToItems(item));
        }
        for (TreeItem<DirItemData> x : item.getChildren()) {
            x.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeItem.TreeModificationEvent<DirItemData>>() {
                @Override
                public void handle(TreeModificationEvent<DirItemData> event) {
                    TreeItem<DirItemData> item = event.getTreeItem();
                    DirUtil.addChildrenItems(item);
                }
            });
            if(x.isLeaf()) {
                x.getChildren().addAll(convertChildrenToItems(x));
            }
        }
    }

    /**
     * 判断TreeItem对应的文件夹是否仍然存在
     * @param item 判断对象
     * @return 当且仅当仍存在文件夹，返回True
     */
    public static boolean ifExist(TreeItem<DirItemData> item) {
        return (item.getValue().getFile() == null || item.getValue().getFile().exists());
    }
}
