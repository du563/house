package com.minsu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.minsu.entity.Orders;

public interface OrdersService extends IService<Orders> {
    
    /**
     * 创建订单
     */
    Orders createOrder(Orders order);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, Long userId);
    
    /**
     * 支付订单
     */
    boolean payOrder(Long orderId, Long userId);
    
    /**
     * 完成订单
     */
    boolean completeOrder(Long orderId);

    /**
     * 商户确认入住（仅商户，且订单属于该商户房源）
     */
    boolean merchantCheckIn(Long merchantId, Long orderId);

    /**
     * 商户完成订单（仅商户，且订单属于该商户房源）
     */
    boolean merchantComplete(Long merchantId, Long orderId);
    
    /**
     * 分页查询用户订单
     */
    IPage<Orders> pageUserOrders(Long userId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 分页查询商户订单（商户视角：根据房源 merchantId 归属）
     */
    IPage<Orders> pageMerchantOrders(Long merchantId, Integer pageNum, Integer pageSize, Integer status);
    
    /**
     * 分页查询所有订单（管理员）
     */
    IPage<Orders> pageAllOrders(Integer pageNum, Integer pageSize, Integer status, String orderNo);
}
