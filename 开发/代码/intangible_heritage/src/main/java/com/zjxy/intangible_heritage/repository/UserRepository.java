package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
}