package org.ray.datacenter.biz.service.function;

import org.ray.datacenter.biz.domain.po.ApiResource;

public interface ApiResourceService {

    void upsert(ApiResource apiResource);
}
