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
    @Override
    public User update(Long id, String nickname, String intro, String avatar) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (intro != null) {
            user.setIntro(intro);
        }
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }
        return userRepository.save(user);
    }
}