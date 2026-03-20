package com.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.competition.entity.Competition;
import com.competition.entity.vo.CompetitionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
    
    @Select("SELECT c.*, u.real_name as publisher_name " +
            "FROM competition c " +
            "LEFT JOIN user u ON c.publisher_id = u.id " +
            "WHERE c.deleted = 0 " +
            "ORDER BY c.create_time DESC")
    List<CompetitionVO> selectCompetitionWithPublisher();
}
