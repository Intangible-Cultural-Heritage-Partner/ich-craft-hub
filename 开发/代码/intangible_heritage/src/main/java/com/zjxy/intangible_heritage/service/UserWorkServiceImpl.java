package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.entity.UserWorkComment;
import com.zjxy.intangible_heritage.entity.UserWorkLike;
import com.zjxy.intangible_heritage.repository.UserWorkCommentRepository;
import com.zjxy.intangible_heritage.repository.UserWorkLikeRepository;
import com.zjxy.intangible_heritage.repository.UserWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserWorkServiceImpl implements UserWorkService {

    private final UserWorkRepository userWorkRepository;
    private final UserWorkLikeRepository likeRepository;
    private final UserWorkCommentRepository commentRepository;
    private final Path coverDir;
    private final Path imageDir;

    public UserWorkServiceImpl(UserWorkRepository userWorkRepository,
                               UserWorkLikeRepository likeRepository,
                               UserWorkCommentRepository commentRepository) {
        this.userWorkRepository = userWorkRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.coverDir = Paths.get(System.getProperty("user.dir"), "uploads", "userwork", "cover");
        this.imageDir = Paths.get(System.getProperty("user.dir"), "uploads", "userwork", "images");
    }

    @Override
    public List<UserWork> findAllPassed() {
        return userWorkRepository.findByAuditStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public List<UserWork> findByUserId(Long userId) {
        return userWorkRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public Optional<UserWork> findById(Long id) {
        return userWorkRepository.findById(id);
    }

    @Override
    public UserWork publish(Long userId, String title, String description,
                            MultipartFile coverFile, MultipartFile[] imageFiles) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("标题不能为空");
        UserWork work = new UserWork();
        work.setUserId(userId);
        work.setTitle(title.trim());
        work.setDescription(description == null || description.isBlank() ? null : description.trim());
        work.setAuditStatus(0);

        String coverUrl = saveFile(coverFile, coverDir, "/uploads/userwork/cover/");
        if (coverUrl != null) work.setCoverImg(coverUrl);

        if (imageFiles != null) {
            StringBuilder sb = new StringBuilder();
            for (MultipartFile f : imageFiles) {
                String url = saveFile(f, imageDir, "/uploads/userwork/images/");
                if (url != null) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(url);
                }
            }
            if (sb.length() > 0) work.setImageList(sb.toString());
        }

        return userWorkRepository.save(work);
    }

    @Override
    public void delete(Long id, Long userId) {
        UserWork work = userWorkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("作品不存在"));
        if (!work.getUserId().equals(userId)) throw new IllegalStateException("只能删除自己的作品");
        userWorkRepository.delete(work);
    }

    @Override
    public boolean isLiked(Long userId, Long workId) {
        return likeRepository.existsByUserIdAndWorkId(userId, workId);
    }

    @Override
    public long likeCount(Long workId) {
        return likeRepository.countByWorkId(workId);
    }

    @Override
    public void toggleLike(Long userId, Long workId) {
        if (likeRepository.existsByUserIdAndWorkId(userId, workId)) {
            likeRepository.deleteByUserIdAndWorkId(userId, workId);
        } else {
            likeRepository.save(new UserWorkLike(userId, workId));
        }
    }

    @Override
    public List<UserWorkComment> findComments(Long workId) {
        return commentRepository.findByWorkIdOrderByCreateTimeDesc(workId);
    }

    @Override
    public long commentCount(Long workId) {
        return commentRepository.countByWorkId(workId);
    }

    @Override
    public UserWorkComment addComment(Long workId, Long userId, String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("评论内容不能为空");
        return commentRepository.save(new UserWorkComment(workId, userId, content.trim()));
    }

    @Override
    public List<UserWork> findLikedWorks(Long userId) {
        List<UserWorkLike> likes = likeRepository.findByUserId(userId);
        List<UserWork> result = new java.util.ArrayList<>();
        for (UserWorkLike lk : likes) {
            userWorkRepository.findById(lk.getWorkId()).ifPresent(result::add);
        }
        return result;
    }

    private String saveFile(MultipartFile file, Path dir, String urlPrefix) {
        if (file == null || file.isEmpty()) return null;
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = "";
        int idx = originalName.lastIndexOf('.');
        if (idx >= 0 && idx < originalName.length() - 1) {
            extension = originalName.substring(idx).replaceAll("[^a-zA-Z0-9.]", "");
        }
        String fileName = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(fileName));
            return urlPrefix + fileName;
        } catch (IOException e) {
            throw new IllegalStateException("文件保存失败", e);
        }
    }
}