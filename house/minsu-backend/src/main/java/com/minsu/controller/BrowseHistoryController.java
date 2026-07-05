package com.minsu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minsu.common.Result;
import com.minsu.entity.BrowseHistory;
import com.minsu.entity.House;
import com.minsu.service.BrowseHistoryService;
import com.minsu.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 浏览历史控制器
 */
@RestController
@RequestMapping("/history")
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @Autowired
    private HouseService houseService;

    /**
     * 获取浏览历史列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        
        Page<BrowseHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId)
               .orderByDesc(BrowseHistory::getBrowseTime);
        
        IPage<BrowseHistory> historyPage = browseHistoryService.page(page, wrapper);
        
        // 获取房源信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (BrowseHistory history : historyPage.getRecords()) {
            House house = houseService.getById(history.getHouseId());
            if (house != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", history.getId());
                item.put("houseId", history.getHouseId());
                item.put("browseTime", history.getBrowseTime());
                item.put("house", house);
                result.add(item);
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result);
        data.put("total", historyPage.getTotal());
        data.put("current", historyPage.getCurrent());
        data.put("size", historyPage.getSize());
        
        return Result.success(data);
    }

    /**
     * 删除浏览记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        BrowseHistory history = browseHistoryService.getById(id);
        if (history == null || !history.getUserId().equals(userId)) {
            return Result.error("记录不存在或无权删除");
        }
        
        boolean success = browseHistoryService.removeById(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 清空浏览历史
     */
    @DeleteMapping("/clear")
    public Result<?> clear(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId);
        browseHistoryService.remove(wrapper);
        
        return Result.success("清空成功", null);
    }
}
