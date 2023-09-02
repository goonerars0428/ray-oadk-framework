package org.ray.datacenter.biz.domain.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.ray.oadk.core.utils.Pagination;

import java.io.Serializable;
import java.util.Collection;

/**
 * @auth ray_cong
 * @date 2019/12/21 12:39
 * @description 数据统一返回格式
 */
public class CommonDTO<T> implements Serializable {

    /**
     * 分页信息
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pageIndex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pageSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalCount;

    /**
     * 返回列表数据
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<T> results;
    /**
     * 返回对象数据
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    /**
     * 返回数据版本
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String version;

    public CommonDTO() {
    }

    /**
     * 标准返回
     *
     * @param pageIndex
     * @param pageSize
     * @param totalCount
     * @param results
     * @param result
     * @param version
     */
    public CommonDTO(Integer pageIndex, Integer pageSize, Integer totalCount, Collection<T> results, T result,
                     String version) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.results = results;
        this.result = result;
        this.version = version;
    }

    /**
     * 返回列表对象
     *
     * @param pageIndex
     * @param pageSize
     * @param totalCount
     * @param results
     * @param version
     */
    public CommonDTO(Integer pageIndex, Integer pageSize, Integer totalCount, Collection<T> results, String version) {
        this(pageIndex, pageSize, totalCount, results, null, version);
    }

    public CommonDTO(Integer pageIndex, Integer pageSize, Integer totalCount, Collection<T> results) {
        this(pageIndex, pageSize, totalCount, results, null);
    }

    public CommonDTO(Pagination pagination, Integer totalCount, Collection<T> results, String version) {
        this(pagination != null ? pagination.getPageIndex() : null, pagination != null ? pagination.getPageSize() :
                null, totalCount, results, version);
    }

    public CommonDTO(Pagination pagination, Integer totalCount, Collection<T> results) {
        this(pagination != null ? pagination.getPageIndex() : null, pagination != null ? pagination.getPageSize() :
                null, totalCount, results, null);
    }

    public CommonDTO(Collection<T> results) {
        this(null, null, results);
    }

    /**
     * 返回单个对象
     *
     * @param result
     * @param version
     */
    public CommonDTO(T result, String version) {
        this(null, null, null, null, result, version);
    }

    public CommonDTO(T result) {
        this(result, null);
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Collection<T> getResults() {
        return results;
    }

    public void setResults(Collection<T> results) {
        this.results = results;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
