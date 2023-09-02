package org.ray.datacenter.core.init;

import org.ray.datacenter.biz.service.business.DatacenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class OadkInit {

    @Autowired
    private DatacenterService datacenterService;

    @PostConstruct
    public void init() throws Exception {
        datacenterService.init();
    }

}
