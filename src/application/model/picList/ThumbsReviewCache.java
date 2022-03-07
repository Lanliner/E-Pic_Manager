package application.model.picList;

import application.controller.Display;

import java.io.File;
import java.util.LinkedList;

public class ThumbsReviewCache {

    public static final int MAX_SIZE = 5;   //缓冲区最大长度

    public LinkedList<ThumbsReview> cache = new LinkedList<>();   //缓冲区内容

    public LinkedList<ThumbsReview> getCache() {
        return cache;
    }


    /**
     * 更新缓冲区中的缩略图列表展示窗口状态
     */
    public void update() {
        for(int i = 0; i < cache.size(); i++) {
            for(int j = 0; j < cache.get(i).getDisplayWindows().size(); j++) {
                if(!cache.get(i).getDisplayWindows().get(j).getStage().isShowing()) {
                    cache.get(i).getDisplayWindows().remove(j);
                    j--;
                }
            }
        }
    }

    /**
     * 向缓冲区添加内容
     * @param e 待插入的元素
     */
    public void add(ThumbsReview e) {
        if(cache.size() > MAX_SIZE) {   //缓冲区满
            //通过FIFO置换方法，将一个没有展示窗口的ThumbsReview清除
            for(int i = 0; i < cache.size(); i++) {
                if(cache.get(i).getDisplayWindows().size() == 0) {
                    cache.remove(i);
                    break;
                }
            }
        }

        cache.add(e);   //将元素插入到缓冲区尾部
    }

    /**
     * 在缓冲区中查找目录为f的元素
     * @param f 目录
     * @return 若找到该元素，则返回该元素，否则返回null
     */
    public ThumbsReview search(File f) {
        for(ThumbsReview thumbsReview : cache) {
            if(thumbsReview.getDirectory().equals(f)) {
                return thumbsReview;
            }
        }
        return null;
    }

    /**
     * 清空缓冲区
     */
    public void clear() {
        for (ThumbsReview tr : cache) {
            for(Display dw : tr.getDisplayWindows()) {
                dw.getStage().close();
            }
        }
        cache.clear();
    }

}
