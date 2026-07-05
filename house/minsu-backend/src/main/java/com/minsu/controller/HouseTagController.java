package com.minsu.controller;

import com.minsu.common.Result;
import com.minsu.entity.HouseTag;
import com.minsu.service.HouseTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 房源标签管理
 */
@RestController
@RequestMapping("/house-tag")
public class HouseTagController {

    @Autowired
    private HouseTagService houseTagService;

    /**
     * 前台/商户/管理员可用：仅返回启用标签
     */
    @GetMapping("/list")
    public Result<List<HouseTag>> listEnabled() {
        return Result.success(houseTagService.listEnabled());
    }

    /**
     * 管理员查看全部标签
     */
    @GetMapping("/admin/list")
    public Result<?> adminList(HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        return Result.success(houseTagService.lambdaQuery()
                .orderByAsc(HouseTag::getSortOrder)
                .orderByDesc(HouseTag::getId)
                .list());
    }

    @PostMapping("/admin")
    public Result<?> adminAdd(@RequestBody HouseTag body, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        if (body == null || !StringUtils.hasText(body.getName())) {
            return Result.error("标签名称不能为空");
        }
        body.setId(null);
        body.setName(body.getName().trim());
        if (body.getStatus() == null) body.setStatus(1);
        if (body.getSortOrder() == null) body.setSortOrder(0);
        boolean ok = houseTagService.save(body);
        return ok ? Result.success("新增成功", body) : Result.error("新增失败");
    }

    @PutMapping("/admin")
    public Result<?> adminUpdate(@RequestBody HouseTag body, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        if (body == null || body.getId() == null) return Result.error("ID不能为空");
        if (!StringUtils.hasText(body.getName())) return Result.error("标签名称不能为空");
        body.setName(body.getName().trim());
        boolean ok = houseTagService.updateById(body);
        return ok ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    @DeleteMapping("/admin/{id}")
    public Result<?> adminDelete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        boolean ok = houseTagService.removeById(id);
        return ok ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    private boolean isAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        return role != null && role == 1;
    }
}

