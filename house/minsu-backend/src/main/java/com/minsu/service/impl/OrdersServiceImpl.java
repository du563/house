package com.minsu.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minsu.entity.House;
import com.minsu.entity.Orders;
import com.minsu.mapper.OrdersMapper;
import com.minsu.service.HouseService;
import com.minsu.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private HouseService houseService;

    @Override
    @Transactional
    public Orders createOrder(Orders order) {
        House house = houseService.getById(order.getHouseId());
        if (house == null || house.getStatus() != 1 || house.getStock() <= 0 || !Integer.valueOf(1).equals(house.getAuditStatus())) {
            return null;
        }
        
        // 计算入住天数
        long days = ChronoUnit.DAYS.between(order.getCheckInDate(), order.getCheckOutDate());
        if (days <= 0) {
            return null;
        }
        
        // 生成订单号
        order.setOrderNo(IdUtil.simpleUUID().substring(0, 20).toUpperCase());
        order.setHouseName(house.getName());
        order.setPrice(house.getPrice());
        order.setDays((int) days);
        order.setTotalPrice(house.getPrice().multiply(BigDecimal.valueOf(days)));
        order.setStatus(0);
        
        save(order);
        
        // 减少库存
        house.setStock(house.getStock() - 1);
        houseService.updateById(house);
        
        return order;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long userId) {
        Orders order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        
        if (order.getStatus() != 0) {
            return false;
        }
        
        order.setStatus(3);
        updateById(order);
        
        // 恢复库存
        House house = houseService.getById(order.getHouseId());
        if (house != null) {
            house.setStock(house.getStock() + 1);
            houseService.updateById(house);
        }
        
        return true;
    }

    @Override
    public boolean payOrder(Long orderId, Long userId) {
        Orders order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        
        if (order.getStatus() != 0) {
            return false;
        }
        
        order.setStatus(1);
        return updateById(order);
    }

    @Override
    @Transactional
    public boolean completeOrder(Long orderId) {
        Orders order = getById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        
        order.setStatus(2);
        boolean success = updateById(order);
        if (!success) {
            return false;
        }

        // 订单已完成：恢复一次库存
        House house = houseService.getById(order.getHouseId());
        if (house != null) {
            house.setStock(house.getStock() + 1);
            houseService.updateById(house);
        }

        return true;
    }

    @Override
    public boolean merchantCheckIn(Long merchantId, Long orderId) {
        Orders order = getById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            // 仅已支付订单允许确认入住
            return false;
        }
        if (order.getCheckInConfirmed() != null && order.getCheckInConfirmed() == 1) {
            // 已确认无需重复
            return true;
        }
        House house = houseService.getById(order.getHouseId());
        if (house == null || house.getMerchantId() == null || !house.getMerchantId().equals(merchantId)) {
            return false;
        }
        Orders upd = new Orders();
        upd.setId(orderId);
        upd.setCheckInConfirmed(1);
        upd.setCheckInTime(LocalDateTime.now());
        return updateById(upd);
    }

    @Override
    @Transactional
    public boolean merchantComplete(Long merchantId, Long orderId) {
        Orders order = getById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            // 与现有逻辑保持一致：完成只能从「已支付」流转到「已完成」
            return false;
        }
        if (order.getCheckInConfirmed() == null || order.getCheckInConfirmed() != 1) {
            // 要求商户先确认入住再完成（更符合业务流程）
            return false;
        }
        House house = houseService.getById(order.getHouseId());
        if (house == null || house.getMerchantId() == null || !house.getMerchantId().equals(merchantId)) {
            return false;
        }
        return completeOrder(orderId);
    }

    @Override
    public IPage<Orders> pageUserOrders(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        
        if (status != null && status >= 0) {
            wrapper.eq(Orders::getStatus, status);
        }
        
        wrapper.orderByDesc(Orders::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public IPage<Orders> pageMerchantOrders(Long merchantId, Integer pageNum, Integer pageSize, Integer status) {
        Page<Orders> page = new Page<>(pageNum, pageSize);

        // 先拿到商户名下的房源ID，再查询订单（避免 join，保持现有 MP 写法）
        LambdaQueryWrapper<House> houseWrapper = new LambdaQueryWrapper<>();
        houseWrapper.eq(House::getMerchantId, merchantId);
        List<House> houses = houseService.list(houseWrapper);

        if (houses == null || houses.isEmpty()) {
            // 避免使用空条件 wrapper 导致商户误查到所有订单
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return page;
        }

        List<Long> houseIds = houses.stream().map(House::getId).collect(java.util.stream.Collectors.toList());

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Orders::getHouseId, houseIds);

        if (status != null && status >= 0) {
            wrapper.eq(Orders::getStatus, status);
        }

        wrapper.orderByDesc(Orders::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public IPage<Orders> pageAllOrders(Integer pageNum, Integer pageSize, Integer status, String orderNo) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && status >= 0) {
            wrapper.eq(Orders::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Orders::getOrderNo, orderNo);
        }
        
        wrapper.orderByDesc(Orders::getCreateTime);
        return page(page, wrapper);
    }
}
