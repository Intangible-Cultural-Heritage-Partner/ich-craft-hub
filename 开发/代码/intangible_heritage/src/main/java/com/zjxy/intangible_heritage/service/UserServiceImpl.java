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
    public User register(String username, String password, String nickname, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setRole(role);
        user.setAvatar("/images/default_avatar.png");
        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u->u.getPassword().equals(password))
                .orElse(null);
    }
}