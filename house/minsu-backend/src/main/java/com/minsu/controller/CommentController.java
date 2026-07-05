package com.minsu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minsu.common.Result;
import com.minsu.entity.Comment;
import com.minsu.entity.House;
import com.minsu.entity.Orders;
import com.minsu.service.OrdersService;
import com.minsu.service.CommentService;
import com.minsu.service.HouseService;
import com.minsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    /**
     * 获取房源评价列表
     */
    @GetMapping("/list/{houseId}")
    public Result<?> list(@PathVariable Long houseId) {
        List<Comment> list = commentService.getHouseComments(houseId);
        return Result.success(list);
    }

    /**
     * 添加评价
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Comment comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        if (comment == null) {
            return Result.error("参数不能为空");
        }

        comment.setUserId(userId);
        comment.setAuditStatus(0);
        comment.setAuditRemark(null);
        comment.setReplyContent(null);
        comment.setReplyTime(null);
        comment.setReplyUserId(null);
        
        if (comment.getScore() == null || comment.getScore() < 1 || comment.getScore() > 5) {
            return Result.error("评分必须在1-5之间");
        }

        // 优先按订单评价：必须本人且订单已完成
        if (comment.getOrderId() != null) {
            Orders order = ordersService.getById(comment.getOrderId());
            if (order == null || !userId.equals(order.getUserId())) {
                return Result.error(403, "订单不存在或无权限评价");
            }
            if (!Integer.valueOf(2).equals(order.getStatus())) {
                return Result.error(403, "需完成订单后才能评价");
            }

            LambdaQueryWrapper<Comment> orderCommentWrapper = new LambdaQueryWrapper<>();
            orderCommentWrapper.eq(Comment::getOrderId, order.getId());
            if (commentService.count(orderCommentWrapper) > 0) {
                return Result.error(400, "该订单已评价");
            }

            // 房源ID以订单为准，防止篡改
            comment.setHouseId(order.getHouseId());
        } else {
            // 兼容旧流程：按房源评价
            if (comment.getHouseId() == null) {
                return Result.error("房源ID不能为空");
            }

            LambdaQueryWrapper<Comment> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(Comment::getUserId, userId)
                    .eq(Comment::getHouseId, comment.getHouseId());
            if (commentService.count(existWrapper) > 0) {
                return Result.error(400, "该房源已评价过");
            }

            LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Orders::getUserId, userId)
                    .eq(Orders::getHouseId, comment.getHouseId())
                    .eq(Orders::getStatus, 2)
                    .orderByDesc(Orders::getCreateTime)
                    .last("LIMIT 1");
            Orders completedOrder = ordersService.getOne(orderWrapper);
            if (completedOrder == null) {
                return Result.error(403, "需完成订单后才能评价");
            }
            comment.setOrderId(completedOrder.getId());
        }

        boolean success = commentService.addComment(comment);
        if (success) {
            return Result.success("评价已提交，待审核后展示", null);
        }
        return Result.error("评价失败");
    }

    /**
     * 管理员分页查看评论
     */
    @GetMapping("/admin/list")
    public Result<?> adminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "-1") Integer auditStatus,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null && auditStatus >= 0) {
            wrapper.eq(Comment::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> commentPage = commentService.page(page, wrapper);
        commentPage.getRecords().forEach(c -> {
            if (c.getUserId() != null && userService.getById(c.getUserId()) != null) {
                c.setUsername(userService.getById(c.getUserId()).getUsername());
            }
            if (c.getHouseId() != null && houseService.getById(c.getHouseId()) != null) {
                c.setHouseName(houseService.getById(c.getHouseId()).getName());
            }
        });
        return Result.success(commentPage);
    }

    /**
     * 管理员审核评论
     */
    @PutMapping("/admin/audit/{id}")
    public Result<?> adminAudit(@PathVariable Long id, @RequestBody(required = false) Comment payload, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }
        Comment exist = commentService.getById(id);
        if (exist == null) {
            return Result.error("评论不存在");
        }
        Integer status = payload == null ? null : payload.getAuditStatus();
        if (status == null || (status != 1 && status != 2)) {
            return Result.error(400, "审核状态无效");
        }
        Comment upd = new Comment();
        upd.setId(id);
        upd.setAuditStatus(status);
        upd.setAuditRemark(payload == null ? null : payload.getAuditRemark());
        boolean ok = commentService.updateById(upd);
        if (ok && status == 1 && !Integer.valueOf(1).equals(exist.getAuditStatus())) {
            // 仅在首次通过时计入房源评分
            houseService.updateScore(exist.getHouseId(), exist.getScore());
        }
        return ok ? Result.success(status == 1 ? "审核通过" : "已驳回", null) : Result.error("审核失败");
    }

    /**
     * 商户分页查看自己房源评论
     */
    @GetMapping("/merchant/list")
    public Result<?> merchantList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "-1") Integer auditStatus,
            HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }

        List<House> houses = houseService.lambdaQuery().eq(House::getMerchantId, userId).list();
        if (houses.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("records", java.util.Collections.emptyList());
            empty.put("total", 0);
            return Result.success(empty);
        }
        List<Long> houseIds = houses.stream().map(House::getId).collect(java.util.stream.Collectors.toList());
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Comment::getHouseId, houseIds);
        if (auditStatus != null && auditStatus >= 0) {
            wrapper.eq(Comment::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> commentPage = commentService.page(page, wrapper);
        commentPage.getRecords().forEach(c -> {
            if (c.getUserId() != null && userService.getById(c.getUserId()) != null) {
                c.setUsername(userService.getById(c.getUserId()).getUsername());
            }
            if (c.getHouseId() != null && houseService.getById(c.getHouseId()) != null) {
                c.setHouseName(houseService.getById(c.getHouseId()).getName());
            }
        });
        return Result.success(commentPage);
    }

    /**
     * 商户回复评论（仅本人房源）
     */
    @PutMapping("/merchant/reply/{id}")
    public Result<?> merchantReply(@PathVariable Long id, @RequestBody Comment payload, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        if (payload == null || !StringUtils.hasText(payload.getReplyContent())) {
            return Result.error("回复内容不能为空");
        }
        Comment exist = commentService.getById(id);
        if (exist == null) {
            return Result.error("评论不存在");
        }
        House house = houseService.getById(exist.getHouseId());
        if (house == null || house.getMerchantId() == null || !house.getMerchantId().equals(userId)) {
            return Result.error(403, "只能回复自己房源的评论");
        }
        Comment upd = new Comment();
        upd.setId(id);
        upd.setReplyContent(payload.getReplyContent().trim());
        upd.setReplyUserId(userId);
        upd.setReplyTime(java.time.LocalDateTime.now());
        boolean ok = commentService.updateById(upd);
        return ok ? Result.success("回复成功", null) : Result.error("回复失败");
    }

    /**
     * 删除评价（管理员或本人）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评价不存在");
        }
        
        // 只有管理员或本人可以删除
        if (role != 1 && !comment.getUserId().equals(userId)) {
            return Result.error(403, "无权限删除");
        }
        
        boolean success = commentService.removeById(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
