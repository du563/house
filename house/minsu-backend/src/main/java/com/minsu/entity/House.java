package com.minsu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源实体类
 */
@Data
@TableName("house")
public class House implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    /** 所在区域 */
    private String area;

    private String address;

    /** 经度（高德坐标 GCJ-02） */
    private BigDecimal longitude;

    /** 纬度（高德坐标 GCJ-02） */
    private BigDecimal latitude;

    /** 户型 */
    private String type;

    /** 可住人数 */
    private Integer capacity;

    /** 评分 */
    private BigDecimal score;

    /** 评分人数 */
    private Integer scoreCount;

    /** 图片列表，逗号分隔 */
    private String images;

    /** 设施，逗号分隔 */
    private String facilities;

    /** 标签ID列表（逗号分隔），关联 house_tag.id */
    private String tagIds;

    /** 状态：0-下架，1-上架 */
    private Integer status;
    
    /** 商户ID（发布者） */
    private Long merchantId;
    
    /** 审核状态：0-待审核，1-审核通过，2-审核驳回 */
    private Integer auditStatus;
    
    /** 审核备注 */
    private String auditRemark;

    /** 库存数量 */
    private Integer stock;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
