package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.CraftsmanApply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CraftsmanApplyRepository extends JpaRepository<CraftsmanApply, Long> {
    // 判断用户是否已有待审核的申请
    boolean existsByUserIdAndAuditStatus(Long userId, Integer auditStatus);

    // 查询所有申请，按时间倒序
    List<CraftsmanApply> findAllByOrderByCreateTimeDesc();

    // 查询用户最新的一条申请（按创建时间倒序取第一条）
    CraftsmanApply findFirstByUserIdOrderByCreateTimeDesc(Long userId);
}