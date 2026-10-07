package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeritageWorkRepository extends JpaRepository<HeritageWork, Long> {

    @Query("select w from HeritageWork w left join fetch w.craftsman where w.auditStatus = :status and (:category is null or w.category = :category) order by w.createTime desc")
    List<HeritageWork> findPublicByCategory(@Param("status") Integer status, @Param("category") String category);

    @Query("select w from HeritageWork w left join fetch w.craftsman where w.craftsman.id = :owner and (:category is null or w.category = :category) order by w.createTime desc")
    List<HeritageWork> findOwnedByCategory(@Param("owner") Long craftsmanId, @Param("category") String category);

    @Query("select distinct w.category from HeritageWork w where w.auditStatus = 1 order by w.category")
    List<String> findPublicCategories();

    @Query("select distinct w.category from HeritageWork w where w.craftsman.id = :owner order by w.category")
    List<String> findOwnedCategories(@Param("owner") Long craftsmanId);

    List<HeritageWork> findAllByOrderByCreateTimeDesc();

    @Query("select w from HeritageWork w left join fetch w.craftsman where w.auditStatus = :status order by w.createTime desc")
    List<HeritageWork> findByAuditStatusWithCraftsman(@Param("status") Integer auditStatus);

    @Query("select w from HeritageWork w left join fetch w.craftsman where w.craftsman.id = :craftsmanId order by w.createTime desc")
    List<HeritageWork> findByCraftsmanIdWithCraftsman(@Param("craftsmanId") Long craftsmanId);

    List<HeritageWork> findByAuditStatusOrderByCreateTimeDesc(Integer auditStatus);

    List<HeritageWork> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);

    Optional<HeritageWork> findByIdAndCraftsmanId(Long id, Long craftsmanId);
}
