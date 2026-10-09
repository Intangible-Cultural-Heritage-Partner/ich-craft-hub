package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.Favorite;

import java.util.List;

public interface FavoriteService {

    boolean isFavorited(Long userId, Long targetId, String targetType);

    void favorite(Long userId, Long targetId, String targetType);

    void unfavorite(Long userId, Long targetId, String targetType);

    List<Favorite> findByUserAndType(Long userId, String targetType);

    List<Favorite> findByUser(Long userId);
}