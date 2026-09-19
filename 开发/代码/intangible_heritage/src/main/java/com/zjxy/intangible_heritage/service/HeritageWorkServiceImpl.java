package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.HeritageWorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HeritageWorkServiceImpl implements HeritageWorkService {

    private final HeritageWorkRepository heritageWorkRepository;
    private final Path uploadDirectory;

    public HeritageWorkServiceImpl(HeritageWorkRepository heritageWorkRepository) {
        this.heritageWorkRepository = heritageWorkRepository;
        this.uploadDirectory = Paths.get(System.getProperty("user.dir"), "uploads", "heritage");
    }

    @Override
    public List<HeritageWork> findAll() {
        return heritageWorkRepository.findByAuditStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public Optional<HeritageWork> findById(Long id) {
        return heritageWorkRepository.findById(id);
    }

    @Override
    public List<HeritageWork> findByCraftsman(Long craftsmanId) {
        return heritageWorkRepository.findByCraftsmanIdOrderByCreateTimeDesc(craftsmanId);
    }

    @Override
    public HeritageWork create(User craftsman, String title, String description,
                               MultipartFile coverFile, String coverUrl,
                               MultipartFile[] imageFiles, String imageUrls) {
        return create(craftsman, title, description, null, coverFile, coverUrl, imageFiles, imageUrls);
    }

    @Override
    public HeritageWork create(User craftsman, String title, String description, String skillBackground,
                               MultipartFile coverFile, String coverUrl,
                               MultipartFile[] imageFiles, String imageUrls) {
        requireCraftsman(craftsman);
        HeritageWork work = new HeritageWork();
        work.setCraftsman(craftsman);
        work.setAuditStatus(0);
        applyFields(work, title, description, skillBackground, coverFile, coverUrl, imageFiles, imageUrls);
        return heritageWorkRepository.save(work);
    }

    @Override
    public HeritageWork update(Long id, User craftsman, String title, String description,
                               MultipartFile coverFile, String coverUrl,
                               MultipartFile[] imageFiles, String imageUrls) {
        return update(id, craftsman, title, description, null, coverFile, coverUrl, imageFiles, imageUrls);
    }

    @Override
    public HeritageWork update(Long id, User craftsman, String title, String description, String skillBackground,
                               MultipartFile coverFile, String coverUrl,
                               MultipartFile[] imageFiles, String imageUrls) {
        requireCraftsman(craftsman);
        HeritageWork work = heritageWorkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("展品不存在"));
        if (work.getCraftsman() == null || !Objects.equals(work.getCraftsman().getId(), craftsman.getId())) {
            throw new IllegalStateException("只能编辑自己发布的展品");
        }
        applyFields(work, title, description, skillBackground, coverFile, coverUrl, imageFiles, imageUrls);
        return heritageWorkRepository.save(work);
    }

    private void requireCraftsman(User user) {
        String role = user.getRole();
        if (user == null || (!"CRAFTSMAN".equalsIgnoreCase(role) && !"1".equals(role))) {
            throw new IllegalStateException("只有匠人可以管理非遗展品");
        }
    }

    private void applyFields(HeritageWork work, String title, String description, String skillBackground,
                             MultipartFile coverFile, String coverUrl,
                             MultipartFile[] imageFiles, String imageUrls) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("展品标题不能为空");
        }
        work.setTitle(title.trim());
        work.setDescription(trimToNull(description));
        work.setSkillBackground(trimToNull(skillBackground));
        work.setCategory("其他");
        String uploadedCover = saveFile(coverFile);
        String submittedCoverUrl = trimToNull(coverUrl);
        if (uploadedCover != null) {
            work.setCoverImg(uploadedCover);
        } else if (submittedCoverUrl != null) {
            work.setCoverImg(submittedCoverUrl);
        }

        List<String> images = new ArrayList<>();
        if (imageFiles != null) {
            for (MultipartFile imageFile : imageFiles) {
                String path = saveFile(imageFile);
                if (path != null) {
                    images.add(path);
                }
            }
        }
        if (imageUrls != null) {
            images.addAll(Arrays.stream(imageUrls.split("[,\\r\\n]+"))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList()));
        }
        if (!images.isEmpty()) {
            work.setImageList(String.join(",", images));
        }
    }

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = "";
        int extensionIndex = originalName.lastIndexOf('.');
        if (extensionIndex >= 0 && extensionIndex < originalName.length() - 1) {
            extension = originalName.substring(extensionIndex).replaceAll("[^a-zA-Z0-9.]", "");
        }
        String fileName = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(file.getInputStream(), uploadDirectory.resolve(fileName));
            return "/uploads/heritage/" + fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("图片保存失败", exception);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
