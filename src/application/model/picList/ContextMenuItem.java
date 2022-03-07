package application.model.picList;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class ContextMenuItem extends ContextMenu {

    public enum ContextMenuItemType {
        /**
         *图片上和空白处的的右键菜单
         */
        IMAGE,BLANK
    }
    public enum Order {
        NAME,DATE,TYPE,FILE_SIZE,REVERSE
    }

    //private final MenuItem cut=new MenuItem("剪切(T)");// T
    private final MenuItem copy=new MenuItem("复制(C)");// C
    private final MenuItem delete=new MenuItem("删除(D)");// D
    private final MenuItem rename=new MenuItem("重命名(M)");// M


    private final Menu view = new Menu("查看");
        private final MenuItem bigView=new MenuItem("大图标");
        private final MenuItem smallView=new MenuItem("小图标");
    private final Menu sortOrder = new Menu("排序方式");
        private final MenuItem sortOrderName = new MenuItem("名称");
        private final MenuItem sortOrderDate = new MenuItem("日期");
        private final MenuItem sortOrderType = new MenuItem("类型");
        private final MenuItem sortOrderFileSize = new MenuItem("大小");
        private final MenuItem sortReverseOrder = new MenuItem("逆序");
    private final MenuItem paste = new MenuItem("粘贴(V)");// V
    private final MenuItem refresh = new MenuItem("刷新(E)");// E


    public ContextMenuItem(ContextMenuItemType type){
        super();
        if(type == ContextMenuItemType.IMAGE){
            this.getItems().addAll(/*cut,*/copy,delete,rename);

        }else {
            view.getItems().addAll(bigView,smallView);
            sortOrder.getItems().addAll(sortOrderName,sortOrderDate,
                    sortOrderType,sortOrderFileSize,sortReverseOrder);
            this.getItems().addAll(view,sortOrder,refresh,paste);
        }
    }
    /*public MenuItem getCut() {
        return cut;
    }*/
    public MenuItem getCopy() {
        return copy;
    }
    public MenuItem getDelete() {
        return delete;
    }
    public MenuItem getRename() {
        return rename;
    }
    public Menu getView() {
        return view;
    }
        public MenuItem getBigView() {
            return bigView;
        }
        public MenuItem getSmallView() {
            return smallView;
        }
    public Menu getSortOrder() {
        return sortOrder;
    }
        public MenuItem getSortOrderName() {
            return sortOrderName;
        }
        public MenuItem getSortOrderDate() {
            return sortOrderDate;
        }
        public MenuItem getSortOrderType() {
            return sortOrderType;
        }
        public MenuItem getSortOrderFileSize() {
            return sortOrderFileSize;
        }
        public MenuItem getSortReverseOrder() {
            return sortReverseOrder;
        }
    public MenuItem getPaste() {
        return paste;
    }
    public MenuItem getRefresh() {
        return refresh;
    }
}
