package com.lalema.backend.controller;

import com.lalema.backend.dto.NotificationDTO;
import com.lalema.backend.dto.Result;
import com.lalema.backend.entity.Notification;
import com.lalema.backend.mapper.NotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "通知接口")
public class NotificationController {
    private final NotificationMapper notificationMapper;

    @GetMapping("/list")
    @Operation(summary = "通知列表")
    public Result<List<NotificationDTO>> list(Authentication auth,
                                               @RequestParam(defaultValue = "0") Long sinceId) {
        Long userId = (Long) auth.getPrincipal();
        List<Notification> list = notificationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>()
                .eq("user_id", userId)
                .gt("id", sinceId)
                .orderByDesc("id")
                .last("LIMIT 50")
        );
        return Result.success(list.stream().map(NotificationDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读通知数")
    public Result<Integer> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long count = notificationMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>()
                .eq("user_id", userId)
                .eq("is_read", false)
        );
        return Result.success(count.intValue());
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "标记已读")
    public Result<Void> markRead(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        Notification n = notificationMapper.selectById(id);
        if (n == null || !n.getUserId().equals(userId)) {
            throw new RuntimeException("通知不存在");
        }
        n.setIsRead(true);
        notificationMapper.updateById(n);
        return Result.success();
    }

    @PostMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Notification update = new Notification();
        update.setIsRead(true);
        notificationMapper.update(update,
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Notification>()
                .eq("user_id", userId)
                .eq("is_read", false)
        );
        return Result.success();
    }
}
