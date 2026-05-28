package com.lalema.backend.controller;

import com.lalema.backend.dto.Result;
import com.lalema.backend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Tag(name = "好友接口")
public class FriendController {
    private final FriendService service;

    @GetMapping("/search")
    @Operation(summary = "搜索用户")
    public Result<List<Map<String, Object>>> search(Authentication auth, @RequestParam String keyword) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.searchUsers(userId, keyword));
    }

    @PostMapping("/request")
    @Operation(summary = "发送好友请求")
    public Result<Void> sendRequest(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String message = body.get("message") != null ? body.get("message").toString() : null;
        service.sendRequest(userId, receiverId, message);
        return Result.success();
    }

    @PostMapping("/accept/{requestId}")
    @Operation(summary = "接受好友请求")
    public Result<Void> accept(Authentication auth, @PathVariable Long requestId) {
        Long userId = (Long) auth.getPrincipal();
        service.acceptRequest(userId, requestId);
        return Result.success();
    }

    @PostMapping("/reject/{requestId}")
    @Operation(summary = "拒绝好友请求")
    public Result<Void> reject(Authentication auth, @PathVariable Long requestId) {
        Long userId = (Long) auth.getPrincipal();
        service.rejectRequest(userId, requestId);
        return Result.success();
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "删除好友")
    public Result<Void> remove(Authentication auth, @PathVariable Long friendId) {
        Long userId = (Long) auth.getPrincipal();
        service.removeFriend(userId, friendId);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "好友列表")
    public Result<List<Map<String, Object>>> getFriends(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getFriends(userId));
    }

    @GetMapping("/requests")
    @Operation(summary = "待处理请求")
    public Result<List<Map<String, Object>>> getRequests(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getPendingRequests(userId));
    }

    @GetMapping("/requests/count")
    @Operation(summary = "待处理请求数")
    public Result<Integer> getRequestCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getPendingCount(userId));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "好友排行榜")
    public Result<List<Map<String, Object>>> getLeaderboard(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getLeaderboard(userId));
    }

    @GetMapping("/stats/{friendId}")
    @Operation(summary = "好友统计")
    public Result<Map<String, Object>> getStats(Authentication auth, @PathVariable Long friendId) {
        return Result.success(service.getFriendStats(friendId));
    }
}
