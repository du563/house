package com.minsu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minsu.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT c.*, u.username FROM comment c LEFT JOIN user u ON c.user_id = u.id WHERE c.house_id = #{houseId} AND c.audit_status = 1 ORDER BY c.create_time DESC")
    List<Comment> selectByHouseIdWithUser(Long houseId);
}
