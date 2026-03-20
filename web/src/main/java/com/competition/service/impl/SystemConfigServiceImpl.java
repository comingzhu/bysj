package com.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.common.Result;
import com.competition.entity.SystemConfig;
import com.competition.mapper.SystemConfigMapper;
import com.competition.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    @Override
    public Result getConfigList() {
        List<SystemConfig> list = this.list();
        return Result.success(list);
    }

    @Override
    public Result getConfigByKey(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = this.getOne(wrapper);
        return Result.success(config);
    }

    @Override
    public Result saveOrUpdateConfig(SystemConfig config) {
        if (config.getId() == null) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, config.getConfigKey());
            if (this.count(wrapper) > 0) {
                return Result.error("配置键已存在");
            }
        }
        this.saveOrUpdate(config);
        return Result.success("保存成功");
    }

    @Override
    public Result deleteConfig(Integer id) {
        this.removeById(id);
        return Result.success("删除成功");
    }
}





