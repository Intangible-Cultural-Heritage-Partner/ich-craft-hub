package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.UserWorkComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserWorkCommentRepository extends JpaRepository<UserWorkComment, Long> {

    List<UserWorkComment> findByWorkIdOrderByCreateTimeDesc(Long workId);

    long countByWorkId(Long workId);
}