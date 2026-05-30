package com.lalema.backend.dto;

import lombok.Data;

@Data
public class FriendUserDTO {
    private Long userId;
    private String username;
    private String nickname;
    private Boolean isFriend = false;
    private Boolean requestSent = false;
    private String remark = "";
}
