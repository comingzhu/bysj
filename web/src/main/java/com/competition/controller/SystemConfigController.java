package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.SystemConfig;
import com.competition.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/config")
public class SystemConfigController {
    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/list")
    public Result getConfigList() {
        return systemConfigService.getConfigList();
    }

    @GetMapping("/key/{key}")
    public Result getConfigByKey(@PathVariable String key) {
        return systemConfigService.getConfigByKey(key);
    }

    @PostMapping("/save")
    public Result saveOrUpdateConfig(@RequestBody SystemConfig config) {
        return systemConfigService.saveOrUpdateConfig(config);
    }

    @DeleteMapping("/{id}")
    public Result deleteConfig(@PathVariable Integer id) {
        return systemConfigService.deleteConfig(id);
    }
}





