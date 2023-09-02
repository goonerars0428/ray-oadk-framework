package org.ray.datacenter.biz.service.function;

public interface ApiInstanceService {
    void upsert(String moduleId, String serviceAddress, Integer port, String version);
}
