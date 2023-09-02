package org.ray.datacenter.biz.domain.po;

import java.io.Serializable;
import java.util.Date;

public class ApiResource implements Serializable {
    /**
     * 接口资源主键，系统所有接口资源
     */
    private String id;

    /**
     * 接口资源值
     */
    private String path;

    /**
     * 接口资源请求方式（get/post/delete/put）
     */
    private String method;

    /**
     * 接口资源名称
     */
    private String name;

    /**
     * 所属模块ID
     */
    private String moduleId;

    /**
     * 接口资源地址栏参数
     */
    private String paramUrl;

    /**
     * 接口资源请求体参数
     */
    private String paramBody;

    /**
     * 接口资源路径参数
     */
    private String paramPath;

    /**
     * 接口资源描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer dataIndex;

    /**
     * 逻辑状态
     */
    private Byte dataStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public ApiResource() {
    }

    public ApiResource(String id, String path, String method, String name, String moduleId, String paramUrl, String paramBody, String paramPath) {
        this.id = id;
        this.path = path;
        this.method = method;
        this.name = name;
        this.moduleId = moduleId;
        this.paramUrl = paramUrl;
        this.paramBody = paramBody;
        this.paramPath = paramPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method == null ? null : method.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId == null ? null : moduleId.trim();
    }

    public String getParamUrl() {
        return paramUrl;
    }

    public void setParamUrl(String paramUrl) {
        this.paramUrl = paramUrl == null ? null : paramUrl.trim();
    }

    public String getParamBody() {
        return paramBody;
    }

    public void setParamBody(String paramBody) {
        this.paramBody = paramBody == null ? null : paramBody.trim();
    }

    public String getParamPath() {
        return paramPath;
    }

    public void setParamPath(String paramPath) {
        this.paramPath = paramPath == null ? null : paramPath.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Integer getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(Integer dataIndex) {
        this.dataIndex = dataIndex;
    }

    public Byte getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(Byte dataStatus) {
        this.dataStatus = dataStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", path=").append(path);
        sb.append(", method=").append(method);
        sb.append(", name=").append(name);
        sb.append(", moduleId=").append(moduleId);
        sb.append(", paramUrl=").append(paramUrl);
        sb.append(", paramBody=").append(paramBody);
        sb.append(", paramPath=").append(paramPath);
        sb.append(", description=").append(description);
        sb.append(", dataIndex=").append(dataIndex);
        sb.append(", dataStatus=").append(dataStatus);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}