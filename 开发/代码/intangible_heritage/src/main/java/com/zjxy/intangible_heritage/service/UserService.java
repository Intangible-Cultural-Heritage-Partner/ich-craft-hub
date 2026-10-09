package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.User;

public interface UserService {
    User register(String username,String password,String phone,String role);
    User login(String account, String password);
    User update(Long id, String nickname, String intro, String avatar);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}