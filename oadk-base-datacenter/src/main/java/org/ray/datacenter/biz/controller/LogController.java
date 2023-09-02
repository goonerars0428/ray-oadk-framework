package org.ray.datacenter.biz.controller;

import org.ray.datacenter.biz.domain.dto.LogUtilDTO;
import org.ray.oadk.core.utils.LogUtil;
import org.springframework.web.bind.annotation.*;

@RestController("oadk-logController")
@RequestMapping("oadk")
public class LogController {


    @GetMapping(value = "logUtil/list",name = "获取已注册的方法和对应日志级别列表")
    public Object logList() {
        return LogUtil.LOG_MAP;
    }

    @PostMapping(value = "logUtil/update",name = "修改已注册的方法和对应日志级别")
    public void logUpdate(@RequestBody LogUtilDTO logUtilDTO) {
        LogUtil.LOG_MAP.put(logUtilDTO.getKey(), logUtilDTO.getValue());
    }
}
