package com.zjxy.intangible_heritage.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zjxy.intangible_heritage.entity.CustomMessage;
import com.zjxy.intangible_heritage.entity.CustomOrder;
import com.zjxy.intangible_heritage.entity.CustomPayment;
import com.zjxy.intangible_heritage.repository.CustomMessageRepository;
import com.zjxy.intangible_heritage.repository.CustomOrderRepository;
import com.zjxy.intangible_heritage.repository.CustomPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CustomOrderService customOrderService;
    private final CustomOrderRepository customOrderRepository;
    private final CustomPaymentRepository customPaymentRepository;
    private final CustomMessageRepository customMessageRepository;

    @Value("${custom.pay.alipay.enabled:false}")
    private boolean alipayEnabled;
    @Value("${custom.pay.alipay.gateway:}")
    private String gateway;
    @Value("${custom.pay.alipay.app-id:}")
    private String appId;
    @Value("${custom.pay.alipay.private-key:}")
    private String privateKey;
    @Value("${custom.pay.alipay.public-key:}")
    private String alipayPublicKey;
    @Value("${custom.pay.alipay.notify-url:}")
    private String notifyUrl;
    @Value("${custom.pay.alipay.return-url:}")
    private String returnUrl;

    private AlipayClient alipayClient;

    @Override
    @Transactional
    public CustomPayment createDepositPayment(Long orderId, Long payerId) {
        CustomOrder order = customOrderService.getForUserOrCraftsman(orderId, payerId);
        if (!order.getApplyUserId().equals(payerId)) {
            throw new IllegalStateException("只有申请人可以支付");
        }
        if (order.getOrderStatus() != 4) {
            throw new IllegalStateException("当前状态不可支付定金");
        }
        if (order.getDepositAmount() == null) {
            throw new IllegalStateException("订单缺少定金信息");
        }
        // 关闭同类型旧待支付流水，避免重复支付
        List<CustomPayment> pendings = customPaymentRepository
                .findByOrderIdAndPayTypeAndStatus(orderId, 1, 0);
        pendings.forEach(p -> { p.setStatus(2); customPaymentRepository.save(p); });
        CustomPayment payment = new CustomPayment();
        payment.setOrderId(orderId);
        payment.setPayerId(payerId);
        payment.setAmount(order.getDepositAmount());
        payment.setPayType(1);
        payment.setOutTradeNo(genTradeNo(orderId, "D"));
        payment.setStatus(0);
        return customPaymentRepository.save(payment);
    }

    @Override
    @Transactional
    public CustomPayment createBalancePayment(Long orderId, Long payerId) {
        CustomOrder order = customOrderService.getForUserOrCraftsman(orderId, payerId);
        if (!order.getApplyUserId().equals(payerId)) {
            throw new IllegalStateException("只有申请人可以支付");
        }
        if (order.getOrderStatus() != 6) {
            throw new IllegalStateException("当前状态不可支付尾款");
        }
        if (order.getQuotePrice() == null || order.getDepositAmount() == null) {
            throw new IllegalStateException("订单缺少报价信息");
        }
        List<CustomPayment> pendings = customPaymentRepository
                .findByOrderIdAndPayTypeAndStatus(orderId, 2, 0);
        pendings.forEach(p -> { p.setStatus(2); customPaymentRepository.save(p); });
        CustomPayment payment = new CustomPayment();
        payment.setOrderId(orderId);
        payment.setPayerId(payerId);
        payment.setAmount(order.getQuotePrice().subtract(order.getDepositAmount()));
        payment.setPayType(2);
        payment.setOutTradeNo(genTradeNo(orderId, "B"));
        payment.setStatus(0);
        return customPaymentRepository.save(payment);
    }

    @Override
    public CustomPayment getByOutTradeNo(String outTradeNo) {
        return customPaymentRepository.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new IllegalStateException("支付流水不存在"));
    }

    @Override
    public boolean isAlipayEnabled() {
        return alipayEnabled;
    }

    @Override
    public String createAlipayPayForm(CustomPayment payment) {
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(gateway, appId, privateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
        }
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (notifyUrl != null && !notifyUrl.isBlank()) request.setNotifyUrl(notifyUrl);
        if (returnUrl != null && !returnUrl.isBlank()) request.setReturnUrl(returnUrl);
        JSONObject biz = new JSONObject();
        biz.put("out_trade_no", payment.getOutTradeNo());
        biz.put("total_amount", payment.getAmount().toPlainString());
        biz.put("subject", "非遗定制订单#" + payment.getOrderId() + "-" + payment.getPayTypeText());
        biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(biz.toJSONString());
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (Exception e) {
            throw new IllegalStateException("生成支付宝支付页面失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void markPaid(String outTradeNo, String tradeNo, String payMethod) {
        CustomPayment payment = getByOutTradeNo(outTradeNo);
        if (payment.getStatus() == 1) {
            return; // 幂等：已支付直接返回
        }
        payment.setStatus(1);
        payment.setTradeNo(tradeNo);
        payment.setPayMethod(payMethod);
        payment.setPayTime(LocalDateTime.now());
        customPaymentRepository.save(payment);

        CustomOrder order = customOrderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalStateException("关联订单不存在"));
        if (payment.getPayType() == 1) {
            if (order.getOrderStatus() != 4) {
                throw new IllegalStateException("订单状态异常，无法确认定金到账");
            }
            order.setOrderStatus(5); // 制作中
            customOrderRepository.save(order);
            systemMsg(order, payment.getPayerId(),
                    "【系统】定金 ¥" + payment.getAmount() + " 支付成功，订单进入【制作中】");
        } else {
            if (order.getOrderStatus() != 6) {
                throw new IllegalStateException("订单状态异常，无法确认尾款到账");
            }
            order.setOrderStatus(7); // 交易完成
            customOrderRepository.save(order);
            systemMsg(order, payment.getPayerId(),
                    "【系统】尾款 ¥" + payment.getAmount() + " 支付成功，交易完成！");
        }
    }

    @Override
    public boolean isAlipaySignValid(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        } catch (Exception e) {
            return false;
        }
    }

    /** 生成商户订单号：C+订单id+D/B+时间戳+3位随机数 */
    private String genTradeNo(Long orderId, String phase) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = new Random().nextInt(900) + 100;
        return "C" + orderId + phase + time + rand;
    }

    /** 发送系统提示消息（绕过状态校验，仅供支付/报价流转使用） */
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
