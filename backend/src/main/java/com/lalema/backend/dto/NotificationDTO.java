package com.lalema.backend.dto;

import com.lalema.backend.entity.Notification;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private Boolean isRead;
    private String createdAt;

    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = n.getId();
        dto.type = n.getType();
        dto.title = n.getTitle();
        dto.content = n.getContent();
        dto.relatedId = n.getRelatedId();
        dto.isRead = n.getIsRead();
        dto.createdAt = n.getCreatedAt() != null
            ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            : "";
        return dto;
    }
}
