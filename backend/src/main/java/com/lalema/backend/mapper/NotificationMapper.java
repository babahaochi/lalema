package com.lalema.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lalema.backend.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
