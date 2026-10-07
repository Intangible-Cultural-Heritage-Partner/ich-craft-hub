package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.User;

public interface UserService {
    User register(String username,String password,String phone,String role);
    User login(String account, String password);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}