package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomOrder;
import java.util.List;

public interface CustomOrderService {

    /** 普通用户发起定制申请，初始状态=0 新建 */
    CustomOrder apply(CustomOrder order);

    /** 用户：我发起的申请 */
    List<CustomOrder> myApply(Long applyUserId);

    /** 匠人：发给我的申请 */
    List<CustomOrder> myCraft(Long craftsmanId);

    /** 匠人接受申请：0 -> 2 沟通中 */
    CustomOrder accept(Long orderId, Long operatorId);

    /** 匠人拒绝申请：0 -> 1 已拒绝 */
    CustomOrder reject(Long orderId, Long operatorId, String refuseReason);

    /** 任一方完结：2 -> 3 需求完结 */
    CustomOrder finish(Long orderId, Long operatorId);

    /** 取单子详情，同时校验调用方是该单子的申请人或匠人之一 */
    CustomOrder getForUserOrCraftsman(Long orderId, Long currentUserId);
}
