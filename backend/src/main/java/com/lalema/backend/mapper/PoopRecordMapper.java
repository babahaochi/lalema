package com.lalema.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lalema.backend.entity.PoopRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PoopRecordMapper extends BaseMapper<PoopRecord> {
    @Select("SELECT date, COUNT(*) as count FROM poop_records WHERE user_id = #{userId} AND date LIKE #{monthPattern} AND deleted = 0 GROUP BY date")
    List<Map<String, Object>> getMonthRecordDays(@Param("userId") Long userId, @Param("monthPattern") String monthPattern);

    @Select("SELECT time_hour as hour, COUNT(*) as count FROM poop_records WHERE user_id = #{userId} AND deleted = 0 GROUP BY time_hour ORDER BY count DESC LIMIT 1")
    Map<String, Object> getMostCommonHour(@Param("userId") Long userId);
}
