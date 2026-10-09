package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.entity.UserWorkComment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserWorkService {

    List<UserWork> findAllPassed();

    List<UserWork> findByUserId(Long userId);

    Optional<UserWork> findById(Long id);

    UserWork publish(Long userId, String title, String description,
                     MultipartFile coverFile, MultipartFile[] imageFiles);
    UserWork publish(UserWork userWork);

    void delete(Long id, Long userId);
    List<UserWork> listApproved();

    boolean isLiked(Long userId, Long workId);
    List<UserWork> listByUser(Long userId);

    long likeCount(Long workId);

    void toggleLike(Long userId, Long workId);

    List<UserWorkComment> findComments(Long workId);

    long commentCount(Long workId);

    UserWorkComment addComment(Long workId, Long userId, String content);

    List<UserWork> findLikedWorks(Long userId);
    void deleteById(Long id);
}