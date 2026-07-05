package com.minsu.controller;

import com.minsu.common.Result;
import com.minsu.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{houseId}")
    public Result<?> check(@PathVariable Long houseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean isFavorite = favoriteService.isFavorite(userId, houseId);
        return Result.success(isFavorite);
    }

    /**
     * 收藏房源
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Long houseId = params.get("houseId");
        if (houseId == null) {
            return Result.error("房源ID不能为空");
        }

        boolean added = favoriteService.addFavorite(userId, houseId);
        if (added) {
            return Result.success("收藏成功", null);
        }
        return Result.success("已收藏", null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/delete/{houseId}")
    public Result<?> delete(@PathVariable Long houseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean removed = favoriteService.removeFavorite(userId, houseId);
        if (removed) {
            return Result.success("取消收藏成功", null);
        }
        return Result.error("取消收藏失败");
    }

    /**
     * 收藏列表（分页）
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 返回 IPage<House>，前端按 records/total 结构渲染
        return Result.success(favoriteService.pageFavoriteHouses(userId, pageNum, pageSize));
    }
}

