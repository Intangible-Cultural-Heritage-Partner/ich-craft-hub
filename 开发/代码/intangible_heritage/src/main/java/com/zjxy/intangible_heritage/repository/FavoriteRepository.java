package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);

    List<Favorite> findByUserIdAndTargetTypeOrderByCreateTimeDesc(Long userId, String targetType);

    List<Favorite> findByUserIdOrderByCreateTimeDesc(Long userId);

    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);

    void deleteByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
}