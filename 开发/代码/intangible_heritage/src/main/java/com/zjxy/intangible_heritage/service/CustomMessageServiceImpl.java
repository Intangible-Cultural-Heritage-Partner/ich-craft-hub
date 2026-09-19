package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomMessage;
import com.zjxy.intangible_heritage.entity.CustomOrder;
import com.zjxy.intangible_heritage.repository.CustomMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomMessageServiceImpl implements CustomMessageService {

    private final CustomMessageRepository customMessageRepository;
    private final CustomOrderService customOrderService;

    @Override
    @Transactional
    public CustomMessage send(Long orderId, Long senderId, String content) {
        // 必须先校验：发送方是该单子申请方或匠人之一，且单子处于沟通中
        CustomOrder order = customOrderService.getForUserOrCraftsman(orderId, senderId);
        if (order.getOrderStatus() != 2) {
            throw new IllegalStateException("仅沟通中状态允许发消息");
        }
        // receiver = 单子的另一方
        Long receiverId = senderId.equals(order.getApplyUserId())
                ? order.getCraftsmanId()
                : order.getApplyUserId();

        CustomMessage msg = new CustomMessage();
        msg.setCustomOrderId(orderId);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        return customMessageRepository.save(msg);
    }

    @Override
    public List<CustomMessage> listByOrder(Long orderId) {
        return customMessageRepository.findByCustomOrderIdOrderBySendTimeAsc(orderId);
    }
}
