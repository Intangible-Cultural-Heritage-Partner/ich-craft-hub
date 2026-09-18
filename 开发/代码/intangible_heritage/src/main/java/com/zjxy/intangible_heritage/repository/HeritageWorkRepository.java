package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeritageWorkRepository extends JpaRepository<HeritageWork, Long> {

    //按审核状态查询并按创建时间倒序（列表页取审核通过的）
    List<HeritageWork> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    //某匠人发布的全部展品，按创建时间倒序（个人中心）
    List<HeritageWork> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);
}
