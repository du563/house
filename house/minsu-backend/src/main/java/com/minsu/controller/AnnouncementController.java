package com.minsu.controller;

import com.minsu.common.Result;
import com.minsu.entity.Announcement;
import com.minsu.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 公告：前台列表无需登录；增删改查仅管理员
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/list")
    public Result<List<Announcement>> listForHome() {
        return Result.success(announcementService.listActiveForHome());
    }

    @GetMapping("/admin/list")
    public Result<List<Announcement>> adminList(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        return Result.success(announcementService.lambdaQuery()
                .orderByAsc(Announcement::getSortOrder)
                .orderByDesc(Announcement::getId)
                .list());
    }

    @PostMapping("/admin")
    public Result<?> adminAdd(@RequestBody Announcement body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        if (body == null || !StringUtils.hasText(body.getContent())) {
            return Result.error("公告内容不能为空");
        }
        body.setId(null);
        if (body.getStatus() == null) {
            body.setStatus(1);
        }
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
        trimFields(body);
        announcementService.save(body);
        return Result.success("添加成功", body);
    }

    @PutMapping("/admin")
    public Result<?> adminUpdate(@RequestBody Announcement body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        if (body == null || body.getId() == null) {
            return Result.error("ID不能为空");
        }
        if (!StringUtils.hasText(body.getContent())) {
            return Result.error("公告内容不能为空");
        }
        trimFields(body);
        announcementService.updateById(body);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/admin/{id}")
    public Result<?> adminDelete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        announcementService.removeById(id);
        return Result.success("删除成功", null);
    }

    private static boolean isAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        return role != null && role == 1;
    }

    private static void trimFields(Announcement a) {
        if (a.getTitle() != null) {
            a.setTitle(a.getTitle().trim());
        }
        if (a.getContent() != null) {
            a.setContent(a.getContent().trim());
        }
    }
}
