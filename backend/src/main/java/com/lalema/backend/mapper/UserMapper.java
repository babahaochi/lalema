package com.lalema.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lalema.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
