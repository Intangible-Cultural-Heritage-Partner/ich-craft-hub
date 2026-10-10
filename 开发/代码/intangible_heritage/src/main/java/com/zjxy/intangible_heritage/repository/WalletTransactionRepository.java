package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByUserIdOrderByCreateTimeDescIdDesc(Long userId);

    List<WalletTransaction> findByTxTypeAndStatusOrderByCreateTimeDescIdDesc(String txType, Integer status);
}
