package com.minsu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Orders implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long houseId;

    private String houseName;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer days;

    private Integer guests;

    private String contactName;

    private String contactPhone;

    /** 状态：0-待支付，1-已支付，2-已完成，3-已取消 */
    private Integer status;

    /** 是否确认入住：0-未确认，1-已确认（商户操作） */
    private Integer checkInConfirmed;

    /** 确认入住时间（商户操作） */
    private LocalDateTime checkInTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
