package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.CustomOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {

    /** 我作为普通用户发起的申请 */
    List<CustomOrder> findByApplyUserIdOrderByCreateTimeDesc(Long applyUserId);

    /** 我作为匠人收到的申请 */
    List<CustomOrder> findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId);
}
