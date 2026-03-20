package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据竞赛分类查询可用的评分员列表
     */
    @Select("SELECT u.* FROM `user` u " +
            "JOIN judge_category jc ON u.id = jc.judge_id " +
            "WHERE u.role = 'judge' AND u.status = 1 AND u.deleted = 0 " +
            "AND jc.category = #{category}")
    List<User> findJudgesByCategory(String category);
}





