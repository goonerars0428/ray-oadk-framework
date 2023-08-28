package org.ray.oadk.core.utils;

/**
 * @auth congr@inspur.com
 * @date 2018/3/21
 * @description 分页工具
 */
public class Pagination {

    /**
     * 默认分页参数
     */
    public static final int DEFAULT_PAGEINDEX = 1;
    public static final int DEFAULT_PAGESIZE = 20;

    /**
     * 查询起始点
     */
    private int startRow;
    /**
     * 分页，页号
     */
    private int pageIndex;
    /**
     * 分页，每页个数
     */
    private int pageSize;

    public Pagination() {
        this(DEFAULT_PAGEINDEX, DEFAULT_PAGESIZE);
    }

    public Pagination(Integer pageIndex, Integer pageSize) {
        //处理起始页
        pageIndex = pageIndex != null ? (pageIndex != 0 ? pageIndex : DEFAULT_PAGEINDEX) : DEFAULT_PAGEINDEX;
        pageSize = pageSize != null ? pageSize : DEFAULT_PAGESIZE;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        //处理分页起始点
        startRow = startRow(pageIndex, pageSize);
    }

    /**
     * 计算查询起始点
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public static int startRow(int pageIndex, int pageSize) {
        return (pageIndex - 1) * pageSize;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
