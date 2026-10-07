package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User register(String username, String password, String phone, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(role);
        user.setAvatar("/images/default_avatar.png");
        return userRepository.save(user);
    }

    @Override
    public User login(String account, String password) {
        // 先尝试按用户名查，再尝试按手机号查
        User user = userRepository.findByUsername(account).orElse(null);
        if (user == null) {
            user = userRepository.findByPhone(account).orElse(null);
        }
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}