package com.lalema.backend.dto;

import lombok.Data;

@Data
public class FriendRequestDTO {
    private Long requestId;
    private Long senderId;
    private String username;
    private String nickname;
    private String message;
    private String createdAt;
}
