package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 某对象的顶层评论（parent_id 为 NULL）
    List<Comment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreateTimeAsc(Integer targetType, Long targetId);

    // 某对象的全部评论（含回复，老方法保留）
    List<Comment> findByTargetTypeAndTargetIdOrderByCreateTimeAsc(Integer targetType, Long targetId);

    // 某条评论的所有回复
    List<Comment> findByParentIdOrderByCreateTimeAsc(Long parentId);

    // 评论数统计
    long countByTargetTypeAndTargetId(Integer targetType, Long targetId);

    // 我发过的所有评论
    List<Comment> findByUserIdOrderByCreateTimeDesc(Long userId);
}