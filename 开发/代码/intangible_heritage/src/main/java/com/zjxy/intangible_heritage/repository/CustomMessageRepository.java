package com.zjxy.intangible_heritage.repository;

import com.zjxy.intangible_heritage.entity.CustomMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomMessageRepository extends JpaRepository<CustomMessage, Long> {

    /** 某定制申请下的全部消息，按时间正序 */
    List<CustomMessage> findByCustomOrderIdOrderBySendTimeAsc(Long customOrderId);
}
