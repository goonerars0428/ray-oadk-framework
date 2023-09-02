package org.ray.datacenter.biz.controller;

import org.ray.datacenter.core.constant.DatacenterConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("oadk-versionController")
@RequestMapping("oadk")
public class VersionController {

    @GetMapping(value = "version", name = "获取服务版本号")
    public String version() {
        return DatacenterConstant.SERVICE_VERSION;
    }
}
