package com.minsu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区帖子
 */
@Data
@TableName("post")
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long topicId;
    private String title;
    private String content;
    private String images;
    /** 0-待审核，1-通过，2-驳回 */
    private Integer status;
    private String auditRemark;
    private Integer likeCount;
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String topicName;

    @TableField(exist = false)
    private Boolean liked;
}

