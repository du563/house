package com.minsu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.minsu.common.Result;
import com.minsu.entity.House;
import com.minsu.entity.Orders;
import com.minsu.service.HouseService;
import com.minsu.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;
    
    @Autowired
    private HouseService houseService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Orders order, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        order.setUserId(userId);
        
        Orders created = ordersService.createOrder(order);
        if (created != null) {
            return Result.success("下单成功", created);
        }
        return Result.error("下单失败，房源不可用或日期无效");
    }

    /**
     * 支付订单
     */
    @PutMapping("/pay/{id}")
    public Result<?> pay(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = ordersService.payOrder(id, userId);
        if (success) {
            return Result.success("支付成功", null);
        }
        return Result.error("支付失败");
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = ordersService.cancelOrder(id, userId);
        if (success) {
            return Result.success("取消成功", null);
        }
        return Result.error("取消失败，订单状态不允许取消");
    }

    /**
     * 完成订单（管理员）
     */
    @PutMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }
        
        boolean success = ordersService.completeOrder(id);
        if (success) {
            return Result.success("订单已完成", null);
        }
        return Result.error("操作失败");
    }

    /**
     * 商户确认入住（仅商户，且订单属于该商户房源）
     */
    @PutMapping("/merchant/checkin/{id}")
    public Result<?> merchantCheckIn(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        boolean success = ordersService.merchantCheckIn(userId, id);
        if (success) {
            return Result.success("已确认入住", null);
        }
        return Result.error("操作失败：订单状态不允许或不属于当前商户");
    }

    /**
     * 商户完成订单（仅商户，且订单属于该商户房源）
     */
    @PutMapping("/merchant/complete/{id}")
    public Result<?> merchantComplete(@PathVariable Long id, HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if (role == null || role != 2) {
            return Result.error(403, "无权限操作");
        }
        boolean success = ordersService.merchantComplete(userId, id);
        if (success) {
            return Result.success("订单已完成", null);
        }
        return Result.error("操作失败：请先确认入住，且仅可操作本商户房源订单");
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        
        Orders order = ordersService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 管理员：可查看所有
        if (role == null || role != 1) {
            // 普通用户：只能查看自己的订单
            if (role == null || role != 2) {
                if (!order.getUserId().equals(userId)) {
                    return Result.error(403, "无权限查看");
                }
            } else {
                // 商户：只能查看自己名下房源的订单
                House house = houseService.getById(order.getHouseId());
                if (house == null || house.getMerchantId() == null || !house.getMerchantId().equals(userId)) {
                    return Result.error(403, "无权限查看");
                }
            }
        }
        
        return Result.success(order);
    }

    /**
     * 查询我的订单
     */
    @GetMapping("/my")
    public Result<?> myOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "-1") Integer status,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        
        if (role != null && role == 2) {
            // 商户：看自己名下房源产生的订单
            IPage<Orders> page = ordersService.pageMerchantOrders(userId, pageNum, pageSize, status);
            return Result.success(page);
        }
        
        // 普通用户：看自己的订单
        IPage<Orders> page = ordersService.pageUserOrders(userId, pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 管理员查询所有订单
     */
    @GetMapping("/admin/list")
    public Result<?> adminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "-1") Integer status,
            @RequestParam(required = false) String orderNo,
            HttpServletRequest request) {
        
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "无权限操作");
        }
        
        IPage<Orders> page = ordersService.pageAllOrders(pageNum, pageSize, status, orderNo);
        return Result.success(page);
    }
}
