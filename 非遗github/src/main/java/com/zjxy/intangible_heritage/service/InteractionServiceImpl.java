package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.Comment;
import com.zjxy.intangible_heritage.entity.LikeRecord;
import com.zjxy.intangible_heritage.repository.CommentRepository;
import com.zjxy.intangible_heritage.repository.LikeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final CommentRepository commentRepository;
    private final LikeRecordRepository likeRecordRepository;

    @Override
    public Comment addComment(Long userId, Integer targetType, Long targetId, String content) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    public Comment replyComment(Long userId, Long parentId, String content) {
        Comment parent = commentRepository.findById(parentId).orElse(null);
        if (parent == null) {
            return null;
        }
        Comment reply = new Comment();
        reply.setUserId(userId);
        reply.setTargetType(parent.getTargetType());
        reply.setTargetId(parent.getTargetId());
        reply.setParentId(parentId);
        reply.setContent(content);
        reply.setCreateTime(LocalDateTime.now());
        return commentRepository.save(reply);
    }

    @Override
    public List<Comment> listComments(Integer targetType, Long targetId) {
        return commentRepository.findByTargetTypeAndTargetIdOrderByCreateTimeAsc(targetType, targetId);
    }

    @Override
    public List<Comment> listTopComments(Integer targetType, Long targetId) {
        return commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreateTimeAsc(targetType, targetId);
    }

    @Override
    public List<Comment> listReplies(Long parentId) {
        return commentRepository.findByParentIdOrderByCreateTimeAsc(parentId);
    }

    @Override
    public Comment findCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Override
    public long countComments(Integer targetType, Long targetId) {
        return commentRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    @Override
    public boolean toggleLike(Long userId, Integer targetType, Long targetId) {
        var existing = likeRecordRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (existing.isPresent()) {
            likeRecordRepository.delete(existing.get());
            return false;
        }
        LikeRecord like = new LikeRecord();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        like.setCreateTime(LocalDateTime.now());
        likeRecordRepository.save(like);
        return true;
    }

    @Override
    public boolean isLiked(Long userId, Integer targetType, Long targetId) {
        return likeRecordRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .isPresent();
    }

    @Override
    public long countLikes(Integer targetType, Long targetId) {
        return likeRecordRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    @Override
    public List<LikeRecord> listLikesByUser(Long userId, Integer targetType) {
        return likeRecordRepository.findByUserIdAndTargetTypeOrderByCreateTimeDesc(userId, targetType);
    }

    @Override
    public List<Comment> listMyComments(Long userId) {
        return commentRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}