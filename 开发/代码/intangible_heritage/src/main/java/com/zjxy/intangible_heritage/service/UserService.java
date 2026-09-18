package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.User;

public interface UserService {
    User register(String username,String password,String nickname,String role);
    User login(String username,String password);
}