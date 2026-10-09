package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.UserWorkLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserWorkLikeRepository extends JpaRepository<UserWorkLike, Long> {

    boolean existsByUserIdAndWorkId(Long userId, Long workId);

    long countByWorkId(Long workId);

    void deleteByUserIdAndWorkId(Long userId, Long workId);

    List<UserWorkLike> findByUserId(Long userId);
}