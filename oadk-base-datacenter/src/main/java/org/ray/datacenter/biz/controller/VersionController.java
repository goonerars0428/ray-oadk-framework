package org.ray.datacenter.biz.controller;

import org.ray.datacenter.core.constant.DatacenterConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("oadk-versionController")
@RequestMapping("oadk/version")
public class VersionController {

    @GetMapping(value = "info", name = "获取服务版本号")
    public String version() {
        return DatacenterConstant.SERVICE_VERSION;
    }
}
