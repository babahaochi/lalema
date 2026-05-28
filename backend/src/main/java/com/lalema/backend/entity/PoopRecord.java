package com.lalema.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("poop_records")
public class PoopRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String date;
    private Integer timeHour;
    private Integer timeMinute;
    private String amount;
    private String consistency;
    private String color;
    private String smell;
    private Integer painLevel;
    private Boolean blood;
    private Boolean mucus;
    private String notes;
    private Long localId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
