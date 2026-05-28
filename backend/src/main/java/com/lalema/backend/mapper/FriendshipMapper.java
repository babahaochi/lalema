package com.lalema.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lalema.backend.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {
}
