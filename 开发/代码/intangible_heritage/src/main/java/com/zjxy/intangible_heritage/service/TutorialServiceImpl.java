package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.Tutorial;
import com.zjxy.intangible_heritage.repository.TutorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final Path coverUploadDir;
    private final Path videoUploadDir;
    private final Path contentImageUploadDir;

    public TutorialServiceImpl(TutorialRepository tutorialRepository) {
        this.tutorialRepository = tutorialRepository;
        this.coverUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "tutorial", "cover");
        this.videoUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "tutorial", "video");
        this.contentImageUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "tutorial", "content");
    }

    @Override
    public List<Tutorial> findAll() {
        return tutorialRepository.findByAuditStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public List<Tutorial> findByCategory(String category) {
        if (category == null || category.isBlank() || "全部".equals(category)) {
            return findAll();
        }
        return tutorialRepository.findByAuditStatusAndCategoryOrderByCreateTimeDesc(1, category);
    }

    @Override
    public List<Tutorial> findByCraftsman(Long craftsmanId) {
        return tutorialRepository.findByCraftsmanIdOrderByCreateTimeDesc(craftsmanId);
    }

    @Override
    public List<Tutorial> findByCraftsmanAndCategory(Long craftsmanId, String category) {
        return tutorialRepository.findByCraftsmanIdAndCategoryOrderByCreateTimeDesc(craftsmanId, category);
    }

    @Override
    public Optional<Tutorial> findById(Long id) {
        return tutorialRepository.findById(id);
    }

    @Override
    public Tutorial create(Long craftsmanId, String title, String description, String category, String tags,
                           String content, MultipartFile[] contentImageFiles, String contentImages,
                           MultipartFile coverFile, String coverUrl,
                           MultipartFile videoFile, String videoUrl) {
        Tutorial tutorial = new Tutorial();
        tutorial.setCraftsmanId(craftsmanId);
        tutorial.setAuditStatus(0);
        applyFields(tutorial, title, description, category, tags, content,
                contentImageFiles, contentImages, coverFile, coverUrl, videoFile, videoUrl);
        return tutorialRepository.save(tutorial);
    }

    @Override
    public Tutorial update(Long id, Long craftsmanId, String title, String description, String category, String tags,
                           String content, MultipartFile[] contentImageFiles, String contentImages,
                           MultipartFile coverFile, String coverUrl,
                           MultipartFile videoFile, String videoUrl) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("教程不存在"));
        if (!Objects.equals(tutorial.getCraftsmanId(), craftsmanId)) {
            throw new IllegalStateException("只能编辑自己发布的教程");
        }
        applyFields(tutorial, title, description, category, tags, content,
                contentImageFiles, contentImages, coverFile, coverUrl, videoFile, videoUrl);
        tutorial.setAuditStatus(0);
        tutorial.setAuditRemark(null);
        return tutorialRepository.save(tutorial);
    }

    private void applyFields(Tutorial tutorial, String title, String description, String category, String tags,
                             String content, MultipartFile[] contentImageFiles, String contentImages,
                             MultipartFile coverFile, String coverUrl,
                             MultipartFile videoFile, String videoUrl) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("教程标题不能为空");
        }
        tutorial.setTitle(title.trim());
        tutorial.setDescription(trimToNull(description));
        tutorial.setCategory(category == null || category.isBlank() ? "其他" : category.trim());
        tutorial.setTags(trimToNull(tags));
        tutorial.setContent(trimToNull(content));

        java.util.List<String> allImageUrls = new java.util.ArrayList<>();
        if (contentImages != null && !contentImages.isBlank()) {
            for (String url : contentImages.split(",")) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) allImageUrls.add(trimmed);
            }
        }
        if (contentImageFiles != null) {
            for (MultipartFile f : contentImageFiles) {
                String saved = saveFile(f, contentImageUploadDir, "/uploads/tutorial/content/");
                if (saved != null) allImageUrls.add(saved);
            }
        }
        tutorial.setContentImages(allImageUrls.isEmpty() ? null : String.join(",", allImageUrls));

        String uploadedCover = saveFile(coverFile, coverUploadDir, "/uploads/tutorial/cover/");
        String submittedCoverUrl = trimToNull(coverUrl);
        if (uploadedCover != null) {
            tutorial.setCoverImg(uploadedCover);
        } else if (submittedCoverUrl != null) {
            tutorial.setCoverImg(submittedCoverUrl);
        }

        String uploadedVideo = saveFile(videoFile, videoUploadDir, "/uploads/tutorial/video/");
        String submittedVideoUrl = trimToNull(videoUrl);
        if (uploadedVideo != null) {
            tutorial.setVideoUrl(uploadedVideo);
        } else if (submittedVideoUrl != null) {
            tutorial.setVideoUrl(submittedVideoUrl);
        }
    }

    private String saveFile(MultipartFile file, Path dir, String urlPrefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}