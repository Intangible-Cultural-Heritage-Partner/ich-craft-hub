package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.repository.UserWorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWorkServiceImpl implements UserWorkService {

    private final UserWorkRepository userWorkRepository;

    @Override
    public UserWork publish(UserWork userWork) {
        userWork.setAuditStatus(0);
        userWork.setCreateTime(LocalDateTime.now());
        return userWorkRepository.save(userWork);
    }

    @Override
    public List<UserWork> listApproved() {
        return userWorkRepository.findByAuditStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public List<UserWork> listByUser(Long userId) {
        return userWorkRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public UserWork findById(Long id) {
        return userWorkRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        userWorkRepository.deleteById(id);
    }


}