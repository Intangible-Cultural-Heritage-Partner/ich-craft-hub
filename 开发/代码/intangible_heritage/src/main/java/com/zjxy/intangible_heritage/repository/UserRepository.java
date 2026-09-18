package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);

    /** 按角色查用户，用于发起定制时列出可选匠人 */
    List<User> findByRole(String role);
}