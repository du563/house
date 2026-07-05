package com.minsu.controller;

import com.minsu.common.Result;
import com.minsu.entity.House;
import com.minsu.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 智能推荐控制器
 */
@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /**
     * 获取推荐房源列表
     */
    @GetMapping("/list")
    public Result<?> getRecommendList(
            @RequestParam(defaultValue = "8") Integer limit,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        List<House> list = recommendService.getRecommendList(userId, limit);
        return Result.success(list);
    }
}
