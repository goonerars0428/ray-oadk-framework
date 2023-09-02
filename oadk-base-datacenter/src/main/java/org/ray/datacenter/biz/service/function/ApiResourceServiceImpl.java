package org.ray.datacenter.biz.service.function;

import org.ray.datacenter.biz.dao.ApiResourceDao;
import org.ray.datacenter.biz.domain.po.ApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiResourceServiceImpl implements ApiResourceService {

    @Autowired
    private ApiResourceDao apiResourceDao;

    @Override
    public void upsert(ApiResource apiResource) {
        apiResourceDao.upsert(apiResource);
    }
}
