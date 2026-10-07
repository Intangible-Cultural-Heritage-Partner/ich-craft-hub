package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.UserWork;

import java.util.List;

public interface UserWorkService {

    UserWork publish(UserWork userWork);

    List<UserWork> listApproved();

    List<UserWork> listByUser(Long userId);

    UserWork findById(Long id);

    void deleteById(Long id);
}