package com.lalema.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lalema.backend.dto.Result;
import com.lalema.backend.entity.PoopRecord;
import com.lalema.backend.service.PoopRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
@Tag(name = "排便记录接口")
public class PoopRecordController {
    private final PoopRecordService service;

    @PostMapping
    @Operation(summary = "保存记录")
    public Result<PoopRecord> save(Authentication auth, @RequestBody PoopRecord record) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.save(userId, record));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步记录")
    public Result<List<PoopRecord>> sync(Authentication auth, @RequestBody List<PoopRecord> records) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.syncRecords(userId, records));
    }

    @GetMapping
    @Operation(summary = "分页查询记录")
    public Result<Page<PoopRecord>> getPage(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getPage(userId, page, size));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "按日期查询")
    public Result<List<PoopRecord>> getByDate(Authentication auth, @PathVariable String date) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getByDate(userId, date));
    }

    @GetMapping("/range")
    @Operation(summary = "按日期范围查询")
    public Result<List<PoopRecord>> getByRange(Authentication auth,
            @RequestParam String startDate, @RequestParam String endDate) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getByDateRange(userId, startDate, endDate));
    }

    @GetMapping("/stats")
    @Operation(summary = "月度统计")
    public Result<Map<String, Object>> getMonthStats(Authentication auth,
            @RequestParam int year, @RequestParam int month) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(service.getMonthStats(userId, year, month));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除记录")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        service.delete(userId, id);
        return Result.success();
    }
}
