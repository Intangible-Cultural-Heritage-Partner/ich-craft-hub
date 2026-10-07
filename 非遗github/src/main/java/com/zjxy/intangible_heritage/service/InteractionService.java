package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.Comment;
import com.zjxy.intangible_heritage.entity.LikeRecord;

import java.util.List;

public interface InteractionService {

    Comment addComment(Long userId, Integer targetType, Long targetId, String content);

    Comment replyComment(Long userId, Long parentId, String content);

    List<Comment> listComments(Integer targetType, Long targetId);

    List<Comment> listTopComments(Integer targetType, Long targetId);

    List<Comment> listReplies(Long parentId);

    Comment findCommentById(Long id);

    long countComments(Integer targetType, Long targetId);

    boolean toggleLike(Long userId, Integer targetType, Long targetId);

    boolean isLiked(Long userId, Integer targetType, Long targetId);

    long countLikes(Integer targetType, Long targetId);

    List<LikeRecord> listLikesByUser(Long userId, Integer targetType);

    List<Comment> listMyComments(Long userId);
}