package com.lalema.backend.controller;

import com.lalema.backend.dto.LoginRequest;
import com.lalema.backend.dto.RegisterRequest;
import com.lalema.backend.dto.Result;
import com.lalema.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(userService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<Map<String, Object>> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        var user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return Result.success(Map.of(
            "userId", user.getId(),
            "username", user.getUsername(),
            "nickname", user.getNickname() != null ? user.getNickname() : ""
        ));
    }
}
