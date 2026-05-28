package com.lalema.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friendships")
public class Friendship {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long friendId;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
