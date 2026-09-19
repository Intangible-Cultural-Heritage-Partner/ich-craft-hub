package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomOrder;
import com.zjxy.intangible_heritage.repository.CustomOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOrderServiceImpl implements CustomOrderService {

    private final CustomOrderRepository customOrderRepository;

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
    public CustomOrder getForUserOrCraftsman(Long orderId, Long currentUserId) {
        CustomOrder order = mustGet(orderId);
        boolean isParty = order.getApplyUserId().equals(currentUserId)
                || order.getCraftsmanId().equals(currentUserId);
        if (!isParty) {
            throw new IllegalStateException("无权查看该申请");
        }
        return order;
    }

    private CustomOrder mustGet(Long orderId) {
        return customOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("定制申请不存在"));
    }
}
