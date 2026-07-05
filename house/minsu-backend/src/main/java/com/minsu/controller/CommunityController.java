package com.minsu.controller;

import com.minsu.common.Result;
import com.minsu.entity.Post;
import com.minsu.entity.Topic;
import com.minsu.service.PostService;
import com.minsu.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private TopicService topicService;
    @Autowired
    private PostService postService;

    @GetMapping("/topic/list")
    public Result<?> topicList() {
        return Result.success(topicService.listEnabled());
    }

    @GetMapping("/post/list")
    public Result<?> postList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long topicId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.pageApprovedPosts(pageNum, pageSize, topicId, userId));
    }

    @PostMapping("/post/add")
    public Result<?> postAdd(@RequestBody Post post, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        if (post == null || !StringUtils.hasText(post.getTitle()) || post.getTopicId() == null) {
            return Result.error("标题和话题不能为空");
        }
        post.setId(null);
        post.setUserId(userId);
        post.setTitle(post.getTitle().trim());
        post.setContent(post.getContent() == null ? null : post.getContent().trim());
        post.setStatus(0);
        post.setAuditRemark(null);
        post.setLikeCount(0);
        post.setViewCount(0);
        boolean ok = postService.save(post);
        return ok ? Result.success("帖子已发布，等待审核", null) : Result.error("发布失败");
    }

    @PutMapping("/post/like/{postId}")
    public Result<?> toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        boolean ok = postService.toggleLike(postId, userId);
        return ok ? Result.success("操作成功", null) : Result.error("操作失败");
    }

    // ===== 管理员 =====
    @GetMapping("/admin/post/list")
    public Result<?> adminPostList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "-1") Integer status,
            HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        return Result.success(postService.pageAdminPosts(pageNum, pageSize, status));
    }

    @PutMapping("/admin/post/audit/{id}")
    public Result<?> adminPostAudit(@PathVariable Long id, @RequestBody Post payload, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        Post exist = postService.getById(id);
        if (exist == null) return Result.error("帖子不存在");
        Integer status = payload == null ? null : payload.getStatus();
        if (status == null || (status != 1 && status != 2)) return Result.error("审核状态无效");
        Post upd = new Post();
        upd.setId(id);
        upd.setStatus(status);
        upd.setAuditRemark(payload == null ? null : payload.getAuditRemark());
        boolean ok = postService.updateById(upd);
        return ok ? Result.success(status == 1 ? "审核通过" : "已驳回", null) : Result.error("审核失败");
    }

    @DeleteMapping("/admin/post/{id}")
    public Result<?> adminDeletePost(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        boolean ok = postService.removeById(id);
        return ok ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    @GetMapping("/admin/topic/list")
    public Result<?> adminTopicList(HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        return Result.success(topicService.lambdaQuery()
                .orderByAsc(Topic::getSortOrder)
                .orderByDesc(Topic::getId)
                .list());
    }

    @PostMapping("/admin/topic")
    public Result<?> adminTopicAdd(@RequestBody Topic body, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        if (body == null || !StringUtils.hasText(body.getName())) return Result.error("话题名称不能为空");
        body.setId(null);
        body.setName(body.getName().trim());
        if (body.getStatus() == null) body.setStatus(1);
        if (body.getSortOrder() == null) body.setSortOrder(0);
        boolean ok = topicService.save(body);
        return ok ? Result.success("新增成功", body) : Result.error("新增失败");
    }

    @PutMapping("/admin/topic")
    public Result<?> adminTopicUpdate(@RequestBody Topic body, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        if (body == null || body.getId() == null) return Result.error("ID不能为空");
        if (!StringUtils.hasText(body.getName())) return Result.error("话题名称不能为空");
        body.setName(body.getName().trim());
        boolean ok = topicService.updateById(body);
        return ok ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    @DeleteMapping("/admin/topic/{id}")
    public Result<?> adminTopicDelete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "无权限操作");
        boolean ok = topicService.removeById(id);
        return ok ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    private boolean isAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        return role != null && role == 1;
    }
}

