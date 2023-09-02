package org.ray.datacenter.biz.service.function;

import org.ray.datacenter.biz.domain.po.ApiModule;

public interface ApiModuleService {
    Integer insert(String id, String applicationName);

    ApiModule select(String applicationName);
}
