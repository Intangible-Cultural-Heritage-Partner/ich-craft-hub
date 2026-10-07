package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.UserWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserWorkRepository extends JpaRepository<UserWork, Long> {

    // 所有审核通过的作品，按时间倒序
    List<UserWork> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    // 我发布的作品
    List<UserWork> findByUserIdOrderByCreateTimeDesc(Long userId);
}