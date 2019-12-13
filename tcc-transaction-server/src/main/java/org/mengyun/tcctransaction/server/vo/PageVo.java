package org.mengyun.tcctransaction.server.vo;

import java.util.List;

/**
 * 分页 VO
 *
 * Created by cheng.zeng on 2016/9/2.
 */
@Deprecated
public class PageVo<T> {

    /**
     * 数据数组
     */
    private List<T> items;
    /**
     * 第几页
     */
    private Integer pageNum;
    /**
     * 每页条件
     */
    private Integer pageSize;
    /**
     * 总页数
     */
    private int pages;

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
