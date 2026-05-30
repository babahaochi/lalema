package com.lalema.backend.dto;

import lombok.Data;

@Data
public class LeaderboardItemDTO {
    private Long userId;
    private String nickname;
    private Integer monthRecords;
    private Boolean isMe;
}
