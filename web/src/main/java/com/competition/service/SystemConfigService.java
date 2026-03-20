package com.competition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.competition.common.Result;
import com.competition.entity.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    Result getConfigList();
    Result getConfigByKey(String key);
    Result saveOrUpdateConfig(SystemConfig config);
    Result deleteConfig(Integer id);
}





