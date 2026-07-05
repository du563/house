package com.minsu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 浏览历史实体类
 */
@Data
@TableName("browse_history")
public class BrowseHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long houseId;

    private LocalDateTime browseTime;
}
