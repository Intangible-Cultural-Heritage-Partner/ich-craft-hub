package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HeritageWorkRepository extends JpaRepository<HeritageWork, Long> {

    List<HeritageWork> findAllByOrderByCreateTimeDesc();

    List<HeritageWork> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);

    Optional<HeritageWork> findByIdAndCraftsmanId(Long id, Long craftsmanId);
}
