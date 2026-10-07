package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface HeritageWorkService {

    List<HeritageWork> findAll();

    Optional<HeritageWork> findById(Long id);

    List<HeritageWork> findByCraftsman(Long craftsmanId);

    HeritageWork create(User craftsman, String title, String description,
                        MultipartFile coverFile, String coverUrl,
                        MultipartFile[] imageFiles, String imageUrls);

    HeritageWork create(User craftsman, String title, String description, String skillBackground,
                        MultipartFile coverFile, String coverUrl,
                        MultipartFile[] imageFiles, String imageUrls);

    HeritageWork update(Long id, User craftsman, String title, String description,
                        MultipartFile coverFile, String coverUrl,
                        MultipartFile[] imageFiles, String imageUrls);

    HeritageWork update(Long id, User craftsman, String title, String description, String skillBackground,
                        MultipartFile coverFile, String coverUrl,
                        MultipartFile[] imageFiles, String imageUrls);
}
