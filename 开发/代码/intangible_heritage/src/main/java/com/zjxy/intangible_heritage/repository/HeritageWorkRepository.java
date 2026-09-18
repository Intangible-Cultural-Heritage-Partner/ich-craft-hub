package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeritageWorkRepository extends JpaRepository<HeritageWork, Long> {

    /** 按创建时间倒序查询全部 */
    List<HeritageWork> findAllByOrderByCreateTimeDesc();

    /** 按审核状态查询 */
    List<HeritageWork> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    /** 按匠人id查询 */
    List<HeritageWork> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);
}