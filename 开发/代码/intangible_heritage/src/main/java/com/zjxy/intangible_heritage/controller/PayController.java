package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.CustomPayment;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.WalletAccount;
import com.zjxy.intangible_heritage.service.PaymentService;
import com.zjxy.intangible_heritage.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 定制订单支付：
 *   GET  /custom/pay/{outTradeNo}        收银台（默认钱包支付；开启沙箱则跳支付宝）
 *   POST /custom/walletPay/{outTradeNo}  钱包余额支付（扣买家、入匠人）
 *   POST /payment/alipay/notify          支付宝异步回调（需公网可达）
 *   GET  /payment/alipay/return          支付宝同步跳转
 */
@Controller
@RequiredArgsConstructor
public class PayController {

    private final PaymentService paymentService;
    private final WalletService walletService;

    @GetMapping("/custom/pay/{outTradeNo}")
    public Object cashier(@PathVariable String outTradeNo,
                          HttpSession session, Model model,
                          HttpServletResponse response) throws IOException {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        CustomPayment payment;
        try {
            payment = paymentService.getByOutTradeNo(outTradeNo);
        } catch (IllegalStateException e) {
            response.sendError(404, e.getMessage());
            return null;
        }
        if (!payment.getPayerId().equals(loginUser.getId())) {
            response.sendError(403, "无权访问该笔支付");
            return null;
        }
        if (payment.getStatus() != 0) {
            return "redirect:/custom/chat/" + payment.getOrderId();
        }
        // 沙箱模式：输出支付宝自动提交表单，跳转真实收银台
        if (paymentService.isAlipayEnabled()) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(paymentService.createAlipayPayForm(payment));
            response.getWriter().flush();
            return null;
        }
        // 默认：钱包收银台
        WalletAccount account = walletService.getOrCreateAccount(loginUser.getId());
        boolean enough = account.getBalance().compareTo(payment.getAmount()) >= 0;
        model.addAttribute("payment", payment);
        model.addAttribute("balance", account.getBalance());
        model.addAttribute("enough", enough);
        return "custom/walletCashier";
    }

    @PostMapping("/custom/walletPay/{outTradeNo}")
    public String walletPay(@PathVariable String outTradeNo,
                            HttpSession session, RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        CustomPayment payment;
        try {
            payment = paymentService.getByOutTradeNo(outTradeNo);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "支付失败：" + e.getMessage());
            return "redirect:/custom/myApply";
        }
        try {
            walletService.payByWallet(outTradeNo, loginUser.getId());
            ra.addFlashAttribute("msg", "支付成功，款项已划转至匠人钱包");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "支付失败：" + e.getMessage());
        }
        return "redirect:/custom/chat/" + payment.getOrderId();
    }

    @PostMapping("/payment/alipay/notify")
    @ResponseBody
    public String alipayNotify(HttpServletRequest request) {
        return handleAlipayCallback(request) ? "success" : "fail";
    }

    @GetMapping("/payment/alipay/return")
    public String alipayReturn(HttpServletRequest request, RedirectAttributes ra) {
        String outTradeNo = request.getParameter("out_trade_no");
        if (handleAlipayCallback(request)) {
            ra.addFlashAttribute("msg", "支付成功！");
            try {
                CustomPayment payment = paymentService.getByOutTradeNo(outTradeNo);
                return "redirect:/custom/chat/" + payment.getOrderId();
            } catch (IllegalStateException ignored) {
                return "redirect:/";
            }
        }
        ra.addFlashAttribute("msg", "支付验签失败，请勿重复操作");
        return "redirect:/";
    }

    /** 验签 + 幂等入账 */
    private boolean handleAlipayCallback(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) params.put(k, v[0]);
        });
        if (!paymentService.isAlipaySignValid(params)) {
            return false;
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return true; // 验签通过但未支付完成，应答 success 让支付宝停止重发
        }
        try {
            paymentService.markPaid(params.get("out_trade_no"), params.get("trade_no"), "ALIPAY_SANDBOX");
            return true;
        } catch (Exception e) {
            return false; // 业务异常应答 fail，支付宝会重试
        }
    }
}
