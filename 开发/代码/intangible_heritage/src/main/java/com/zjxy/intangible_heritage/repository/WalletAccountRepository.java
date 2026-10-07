package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {

    Optional<WalletAccount> findByUserId(Long userId);

    /** 原子扣款：余额不足时更新 0 行 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WalletAccount a set a.balance = a.balance - :amount " +
           "where a.userId = :userId and a.balance >= :amount")
    int deduct(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /** 原子加款 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WalletAccount a set a.balance = a.balance + :amount where a.userId = :userId")
    int credit(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
