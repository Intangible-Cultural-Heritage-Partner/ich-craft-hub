package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    /** 按创建时间倒序查询全部 */
    List<Tutorial> findAllByOrderByCreateTimeDesc();

    /** 按审核状态查询 */
    List<Tutorial> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    /** 按匠人id查询 */
    List<Tutorial> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);
}