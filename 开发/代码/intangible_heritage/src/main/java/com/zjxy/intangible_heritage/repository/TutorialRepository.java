package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    List<Tutorial> findAllByOrderByCreateTimeDesc();

    List<Tutorial> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    List<Tutorial> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);

    List<Tutorial> findByCraftsmanIdAndCategoryOrderByCreateTimeDesc(Long craftsmanId, String category);

    List<Tutorial> findByAuditStatusAndCategoryOrderByCreateTimeDesc(Integer auditStatus, String category);

    List<Tutorial> findByCategoryOrderByCreateTimeDesc(String category);
}