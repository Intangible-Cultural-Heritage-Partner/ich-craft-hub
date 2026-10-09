package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.LikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {

    // 查询某人是否点赞过某对象
    Optional<LikeRecord> findByUserIdAndTargetTypeAndTargetId(Long userId, Integer targetType, Long targetId);

    // 统计某对象的点赞数
    long countByTargetTypeAndTargetId(Integer targetType, Long targetId);

    // 我点赞过的记录
    List<LikeRecord> findByUserIdAndTargetTypeOrderByCreateTimeDesc(Long userId, Integer targetType);
}