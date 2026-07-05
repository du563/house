package com.minsu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("comment")
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long houseId;

    private Long orderId;

    private String content;

    /** 评分（1-5） */
    private Integer score;

    private String images;

    /** 审核状态：0-待审核，1-审核通过，2-审核驳回 */
    private Integer auditStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 商户回复 */
    private String replyContent;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 回复人ID（商户） */
    private Long replyUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 用户名，非数据库字段 */
    @TableField(exist = false)
    private String username;

    /** 房源名称，非数据库字段 */
    @TableField(exist = false)
    private String houseName;
}
