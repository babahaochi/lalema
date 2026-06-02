package com.lalema.backend.controller;

import com.lalema.backend.dto.FriendRequestDTO;
import com.lalema.backend.dto.FriendUserDTO;
import com.lalema.backend.dto.LeaderboardItemDTO;
import com.lalema.backend.dto.Result;
import com.lalema.backend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Tag(name = "好友接口")
public class FriendController {
    private final FriendService service;

    @GetMapping("/search")
    @Operation(summary = "搜索用户")
    public Result<List<FriendUserDTO>> search(Authentication auth, @RequestParam String keyword) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.searchUsers(userId, keyword));
    }

    @PostMapping("/request")
    @Operation(summary = "发送好友请求")
    public Result<Void> sendRequest(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            Long userId = (Long) auth.getPrincipal();
            Long receiverId = Long.valueOf(body.get("receiverId").toString());
            String message = body.get("message") != null ? body.get("message").toString() : null;
            service.sendRequest(userId, receiverId, message);
            return Result.success();
        } catch (RuntimeException e) {
            log.error("发送好友请求失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送好友请求异常", e);
            return Result.error("操作失败，请稍后重试");
        }
    }

    @PostMapping("/accept/{requestId}")
    @Operation(summary = "接受好友请求")
    public Result<Void> accept(Authentication auth, @PathVariable Long requestId) {
        try {
            Long userId = (Long) auth.getPrincipal();
            service.acceptRequest(userId, requestId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reject/{requestId}")
    @Operation(summary = "拒绝好友请求")
    public Result<Void> reject(Authentication auth, @PathVariable Long requestId) {
        try {
            Long userId = (Long) auth.getPrincipal();
            service.rejectRequest(userId, requestId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "删除好友")
    public Result<Void> remove(Authentication auth, @PathVariable Long friendId) {
        try {
            Long userId = (Long) auth.getPrincipal();
            service.removeFriend(userId, friendId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "好友列表")
    public Result<List<FriendUserDTO>> getFriends(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getFriends(userId));
    }

    @GetMapping("/requests")
    @Operation(summary = "待处理请求")
    public Result<List<FriendRequestDTO>> getRequests(Authentication auth) {
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
    public Result<List<LeaderboardItemDTO>> getLeaderboard(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getLeaderboard(userId));
    }

    @GetMapping("/stats/{friendId}")
    @Operation(summary = "好友统计")
    public Result<Map<String, Object>> getStats(Authentication auth, @PathVariable Long friendId) {
        return Result.success(service.getFriendStats(friendId));
    }
}
