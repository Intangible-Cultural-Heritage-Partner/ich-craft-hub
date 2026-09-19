package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeritageWorkRepository extends JpaRepository<HeritageWork, Long> {

    List<HeritageWork> findAllByOrderByCreateTimeDesc();

    @Query("select w from HeritageWork w left join fetch w.craftsman where w.auditStatus = :status order by w.createTime desc")
    List<HeritageWork> findByAuditStatusWithCraftsman(@Param("status") Integer auditStatus);

    @Query("select w from HeritageWork w left join fetch w.craftsman where w.craftsman.id = :craftsmanId order by w.createTime desc")
    List<HeritageWork> findByCraftsmanIdWithCraftsman(@Param("craftsmanId") Long craftsmanId);

    List<HeritageWork> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    List<HeritageWork> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);

    Optional<HeritageWork> findByIdAndCraftsmanId(Long id, Long craftsmanId);
}
