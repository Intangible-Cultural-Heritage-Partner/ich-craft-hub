package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.Tutorial;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TutorialService {

    List<Tutorial> findAll();

    List<Tutorial> findByCategory(String category);

    List<Tutorial> findByCraftsman(Long craftsmanId);

    Optional<Tutorial> findById(Long id);

    Tutorial create(Long craftsmanId, String title, String description, String category, String tags,
                    String content, MultipartFile coverFile, String coverUrl,
                    MultipartFile videoFile, String videoUrl);

    Tutorial update(Long id, Long craftsmanId, String title, String description, String category, String tags,
                    String content, MultipartFile coverFile, String coverUrl,
                    MultipartFile videoFile, String videoUrl);
}