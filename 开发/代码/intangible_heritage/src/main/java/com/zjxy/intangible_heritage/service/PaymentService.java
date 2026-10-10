package com.zjxy.intangible_heritage.service;

import com.zjxy.intangible_heritage.entity.CustomPayment;

import java.util.Map;

public interface PaymentService {

    /** 创建定金支付流水（订单须处于 4 待付定金，付款人须为申请人） */
    CustomPayment createDepositPayment(Long orderId, Long payerId);

    /** 创建尾款支付流水（订单须处于 6 待付尾款，付款人须为申请人） */
    CustomPayment createBalancePayment(Long orderId, Long payerId);

    /** 按商户订单号查询流水 */
    CustomPayment getByOutTradeNo(String outTradeNo);

    /** 是否启用支付宝沙箱网关 */
    boolean isAlipayEnabled();

    /** 生成支付宝收银台表单HTML（仅沙箱模式调用） */
    String createAlipayPayForm(CustomPayment payment);

    /**
     * 标记支付成功并推进订单状态（幂等）：
     *   定金支付 -> 4 待付定金 变为 5 制作中
     *   尾款支付 -> 6 待付尾款 变为 7 交易完成
     */
    void markPaid(String outTradeNo, String tradeNo, String payMethod);

    /** 校验支付宝回调签名（RSA2） */
    boolean isAlipaySignValid(Map<String, String> params);
}
