package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.CustomPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomPaymentRepository extends JpaRepository<CustomPayment, Long> {

    Optional<CustomPayment> findByOutTradeNo(String outTradeNo);

    List<CustomPayment> findByOrderIdAndPayTypeAndStatus(Long orderId, Integer payType, Integer status);
}
