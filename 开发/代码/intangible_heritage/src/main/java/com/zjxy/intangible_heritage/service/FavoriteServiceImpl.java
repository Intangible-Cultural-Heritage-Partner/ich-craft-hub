package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.Favorite;
import com.zjxy.intangible_heritage.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public boolean isFavorited(Long userId, Long targetId, String targetType) {
        return favoriteRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }

    @Override
    @Transactional
    public void favorite(Long userId, Long targetId, String targetType) {
        if (!isFavorited(userId, targetId, targetType)) {
            favoriteRepository.save(new Favorite(userId, targetId, targetType));
        }
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long targetId, String targetType) {
        favoriteRepository.deleteByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }

    @Override
    public List<Favorite> findByUserAndType(Long userId, String targetType) {
        return favoriteRepository.findByUserIdAndTargetTypeOrderByCreateTimeDesc(userId, targetType);
    }

    @Override
    public List<Favorite> findByUser(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}