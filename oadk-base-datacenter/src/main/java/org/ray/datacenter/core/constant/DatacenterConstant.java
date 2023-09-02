package org.ray.datacenter.core.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatacenterConstant {

    public static String SERVICE_VERSION;
    private String applicationName;

    private Integer port;

    /**
     * 用于读取pom文件获取版本号
     */
    private String groupId;
    /**
     * 用于读取pom文件获取版本号
     */
    private String artifactId;

    public String getApplicationName() {
        return applicationName;
    }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getPort() {
        return port;
    }

    @Value("${server.port}")
    public void setPort(Integer port) {
        this.port = port;
    }

    public String getGroupId() {
        return groupId;
    }

    @Value("${constant.datacenter.group_id}")
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Value("${constant.datacenter.artifact_id}")
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
}
