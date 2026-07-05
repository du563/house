package com.minsu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.BrowseHistory;
import com.minsu.mapper.BrowseHistoryMapper;
import com.minsu.service.BrowseHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService {

    @Override
    public void recordBrowse(Long userId, Long houseId) {
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setHouseId(houseId);
        history.setBrowseTime(LocalDateTime.now());
        save(history);
    }

    @Override
    public List<BrowseHistory> getUserHistory(Long userId, int limit) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId)
               .orderByDesc(BrowseHistory::getBrowseTime)
               .last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public List<Long> getRecentHouseIds(Long userId, int limit) {
        List<BrowseHistory> histories = getUserHistory(userId, limit);
        return histories.stream()
                .map(BrowseHistory::getHouseId)
                .distinct()
                .collect(Collectors.toList());
    }
}
