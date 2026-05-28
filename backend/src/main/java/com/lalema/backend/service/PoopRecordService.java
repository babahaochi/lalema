package com.lalema.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lalema.backend.entity.PoopRecord;
import com.lalema.backend.mapper.PoopRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PoopRecordService {
    private final PoopRecordMapper mapper;

    public PoopRecord save(Long userId, PoopRecord record) {
        record.setUserId(userId);
        if (record.getId() != null) {
            PoopRecord existing = mapper.selectById(record.getId());
            if (existing != null && existing.getUserId().equals(userId)) {
                mapper.updateById(record);
                return record;
            }
        }
        mapper.insert(record);
        return record;
    }

    public List<PoopRecord> syncRecords(Long userId, List<PoopRecord> records) {
        List<PoopRecord> result = new ArrayList<>();
        for (PoopRecord record : records) {
            record.setUserId(userId);
            if (record.getLocalId() != null) {
                PoopRecord existing = mapper.selectOne(
                    new LambdaQueryWrapper<PoopRecord>()
                        .eq(PoopRecord::getUserId, userId)
                        .eq(PoopRecord::getLocalId, record.getLocalId())
                );
                if (existing != null) {
                    record.setId(existing.getId());
                    mapper.updateById(record);
                } else {
                    mapper.insert(record);
                }
            } else {
                mapper.insert(record);
            }
            result.add(record);
        }
        return result;
    }

    public List<PoopRecord> getByDate(Long userId, String date) {
        return mapper.selectList(
            new LambdaQueryWrapper<PoopRecord>()
                .eq(PoopRecord::getUserId, userId)
                .eq(PoopRecord::getDate, date)
                .orderByDesc(PoopRecord::getTimeHour)
                .orderByDesc(PoopRecord::getTimeMinute)
        );
    }

    public Page<PoopRecord> getPage(Long userId, int page, int size) {
        return mapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<PoopRecord>()
                .eq(PoopRecord::getUserId, userId)
                .orderByDesc(PoopRecord::getDate)
                .orderByDesc(PoopRecord::getTimeHour)
        );
    }

    public List<PoopRecord> getByDateRange(Long userId, String startDate, String endDate) {
        return mapper.selectList(
            new LambdaQueryWrapper<PoopRecord>()
                .eq(PoopRecord::getUserId, userId)
                .ge(PoopRecord::getDate, startDate)
                .le(PoopRecord::getDate, endDate)
                .orderByDesc(PoopRecord::getDate)
                .orderByDesc(PoopRecord::getTimeHour)
        );
    }

    public Map<String, Object> getMonthStats(Long userId, int year, int month) {
        String monthPrefix = String.format("%04d-%02d", year, month);
        List<Map<String, Object>> recordDays = mapper.getMonthRecordDays(userId, monthPrefix);
        int totalRecords = mapper.selectCount(
            new LambdaQueryWrapper<PoopRecord>()
                .eq(PoopRecord::getUserId, userId)
                .likeRight(PoopRecord::getDate, monthPrefix)
        ).intValue();
        Map<String, Object> commonHour = mapper.getMostCommonHour(userId);

        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        int today = LocalDate.now().getDayOfMonth();
        int daysPassed = (year == LocalDate.now().getYear() && month == LocalDate.now().getMonthValue()) ? today : daysInMonth;

        Map<String, Object> stats = new HashMap<>();
        stats.put("recordDays", recordDays.size());
        stats.put("totalRecords", totalRecords);
        stats.put("daysInMonth", daysInMonth);
        stats.put("daysPassed", daysPassed);
        stats.put("checkInRate", daysPassed > 0 ? (double) recordDays.size() / daysPassed : 0);
        stats.put("mostCommonHour", commonHour != null ? commonHour.get("hour") : null);
        return stats;
    }

    public void delete(Long userId, Long recordId) {
        PoopRecord record = mapper.selectById(recordId);
        if (record != null && record.getUserId().equals(userId)) {
            mapper.deleteById(recordId);
        }
    }
}
