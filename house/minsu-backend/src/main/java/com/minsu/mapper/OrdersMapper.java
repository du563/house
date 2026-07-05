package com.minsu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minsu.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
