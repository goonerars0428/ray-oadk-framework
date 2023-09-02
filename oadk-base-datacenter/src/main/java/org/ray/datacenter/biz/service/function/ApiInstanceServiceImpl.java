package org.ray.datacenter.biz.service.function;

import org.ray.datacenter.biz.dao.ApiInstanceDao;
import org.ray.datacenter.biz.domain.po.ApiInstance;
import org.ray.oadk.core.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiInstanceServiceImpl implements ApiInstanceService {

    @Autowired
    private ApiInstanceDao apiInstanceDao;

    @Override
    public void upsert(String moduleId, String serviceAddress, Integer port, String version) {
        ApiInstance apiInstance = new ApiInstance(UUIDUtil.uuid(),moduleId, serviceAddress, port, version);
        apiInstanceDao.upsert(apiInstance);
    }
}
