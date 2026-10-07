package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomMessage;
import com.zjxy.intangible_heritage.entity.CustomOrder;
import com.zjxy.intangible_heritage.repository.CustomMessageRepository;
import com.zjxy.intangible_heritage.repository.CustomOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOrderServiceImpl implements CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final CustomMessageRepository customMessageRepository;

    /** 定金比例，默认 30% */
    @Value("${custom.pay.deposit-rate:0.3}")
    private BigDecimal depositRate;

    @Override
    @Transactional
    public CustomOrder apply(CustomOrder order) {
        order.setOrderStatus(0);
        return customOrderRepository.save(order);
    }

    @Override
    public List<CustomOrder> myApply(Long applyUserId) {
        return customOrderRepository.findByApplyUserIdOrderByCreateTimeDesc(applyUserId);
    }

    @Override
    public List<CustomOrder> myCraft(Long craftsmanId) {
        return customOrderRepository.findByCraftsmanIdOrderByCreateTimeDesc(craftsmanId);
    }

    @Override
    @Transactional
    public CustomOrder accept(Long orderId, Long operatorId) {
        CustomOrder order = mustGet(orderId);
        // 只有匠人本人能操作
        if (!order.getCraftsmanId().equals(operatorId)) {
            throw new IllegalStateException("无权操作该申请");
        }
        if (order.getOrderStatus() != 0) {
            throw new IllegalStateException("仅新建状态可接受");
        }
        order.setOrderStatus(2);
        return customOrderRepository.save(order);
    }

    @Override
    @Transactional
    public CustomOrder reject(Long orderId, Long operatorId, String refuseReason) {
        CustomOrder order = mustGet(orderId);
        if (!order.getCraftsmanId().equals(operatorId)) {
            throw new IllegalStateException("无权操作该申请");
        }
        if (order.getOrderStatus() != 0) {
            throw new IllegalStateException("仅新建状态可拒绝");
        }
        order.setOrderStatus(1);
        order.setRefuseReason(refuseReason);
        return customOrderRepository.save(order);
    }

    @Override
    @Transactional
    public CustomOrder finish(Long orderId, Long operatorId) {
        CustomOrder order = mustGet(orderId);
        // 申请人与匠人均可完结
        boolean isParty = order.getApplyUserId().equals(operatorId)
                || order.getCraftsmanId().equals(operatorId);
        if (!isParty) {
            throw new IllegalStateException("无权操作该申请");
        }
        if (order.getOrderStatus() != 2) {
            throw new IllegalStateException("仅沟通中状态可完结");
        }
        order.setOrderStatus(3);
        return customOrderRepository.save(order);
    }

    @Override
    @Transactional
    public CustomOrder getForUserOrCraftsman(Long orderId, Long currentUserId) {
        CustomOrder order = mustGet(orderId);
        boolean isParty = order.getApplyUserId().equals(currentUserId)
                || order.getCraftsmanId().equals(currentUserId);
        if (!isParty) {
            throw new IllegalStateException("无权查看该申请");
        }
        return order;
    }

    @Override
    @Transactional
    public CustomOrder quote(Long orderId, Long craftsmanId, BigDecimal quotePrice, String quoteNote) {
        CustomOrder order = mustGet(orderId);
        if (!order.getCraftsmanId().equals(craftsmanId)) {
            throw new IllegalStateException("只有该单子的匠人可以报价");
        }
        if (order.getOrderStatus() != 2) {
            throw new IllegalStateException("仅沟通中状态可以报价");
        }
        if (quotePrice == null || quotePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("报价金额无效");
        }
        BigDecimal deposit = quotePrice.multiply(depositRate).setScale(2, RoundingMode.HALF_UP);
        order.setQuotePrice(quotePrice);
        order.setDepositAmount(deposit);
        order.setQuoteNote(quoteNote);
        order.setOrderStatus(4);
        CustomOrder saved = customOrderRepository.save(order);
        systemMsg(order, craftsmanId,
                "【报价】总价 ¥" + quotePrice + "（定金 ¥" + deposit + "）"
                        + (quoteNote == null || quoteNote.isBlank() ? "" : "，说明：" + quoteNote));
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder rejectQuote(Long orderId, Long operatorId) {
        CustomOrder order = mustGet(orderId);
        if (!order.getApplyUserId().equals(operatorId)) {
            throw new IllegalStateException("只有申请人可以处理报价");
        }
        if (order.getOrderStatus() != 4) {
            throw new IllegalStateException("当前状态无法拒绝报价");
        }
        order.setOrderStatus(2);
        CustomOrder saved = customOrderRepository.save(order);
        systemMsg(order, operatorId, "【系统】申请人拒绝了本次报价，请继续沟通");
        return saved;
    }

    @Override
    @Transactional
    public CustomOrder craftsmanDone(Long orderId, Long operatorId) {
        CustomOrder order = mustGet(orderId);
        if (!order.getCraftsmanId().equals(operatorId)) {
            throw new IllegalStateException("只有该单子的匠人可以标记完工");
        }
        if (order.getOrderStatus() != 5) {
            throw new IllegalStateException("仅制作中状态可以标记完工");
        }
        order.setOrderStatus(6);
        CustomOrder saved = customOrderRepository.save(order);
        systemMsg(order, operatorId, "【系统】匠人已标记完工，请支付尾款完成交易");
        return saved;
    }

    private CustomOrder mustGet(Long orderId) {
        return customOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("定制申请不存在"));
    }

    /** 交易流转的系统提示消息（绕过状态校验） */
    private void systemMsg(CustomOrder order, Long senderId, String content) {
        Long receiverId = senderId.equals(order.getApplyUserId())
                ? order.getCraftsmanId()
                : order.getApplyUserId();
        CustomMessage msg = new CustomMessage();
        msg.setCustomOrderId(order.getId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        customMessageRepository.save(msg);
    }
}
