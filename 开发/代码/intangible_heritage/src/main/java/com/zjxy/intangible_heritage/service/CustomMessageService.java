package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomMessage;
import java.util.List;

public interface CustomMessageService {

    /** 发消息：必须单子状态=2 沟通中 */
    CustomMessage send(Long orderId, Long senderId, String content);

    /** 拉取某单子的全部消息（按时间正序） */
    List<CustomMessage> listByOrder(Long orderId);
}
