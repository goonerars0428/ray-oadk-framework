package org.ray.datacenter.biz.service.function;

import org.ray.datacenter.biz.dao.ApiModuleDao;
import org.ray.datacenter.biz.domain.po.ApiModule;
import org.ray.datacenter.biz.domain.po.ApiModuleQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiModuleServiceImpl implements ApiModuleService {

    @Autowired
    private ApiModuleDao apiModuleDao;

    @Override
    public Integer insert(String id, String applicationName) {
        ApiModule apiModule = new ApiModule();
        apiModule.setId(id);
        apiModule.setName(applicationName);
        return apiModuleDao.insertSelective(apiModule);
    }

    @Override
    public ApiModule select(String applicationName) {
        ApiModuleQuery apiModuleQuery = new ApiModuleQuery();
        ApiModuleQuery.Criteria criteria = apiModuleQuery.createCriteria();
        criteria.andNameEqualTo(applicationName);
        List<ApiModule> apiModules = apiModuleDao.selectByExample(apiModuleQuery);
        return (apiModules == null || apiModules.isEmpty()) ? null : apiModules.get(0);
    }
}
