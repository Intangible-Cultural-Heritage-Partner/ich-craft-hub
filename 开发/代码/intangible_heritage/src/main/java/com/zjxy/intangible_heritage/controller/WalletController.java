package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.WalletAccount;
import com.zjxy.intangible_heritage.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * 用户钱包：
 *   GET  /wallet           我的钱包（余额+流水+充值表单）
 *   POST /wallet/recharge  提交充值申请（等待管理员审核）
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public String wallet(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        WalletAccount account = walletService.getOrCreateAccount(loginUser.getId());
        model.addAttribute("account", account);
        model.addAttribute("transactions", walletService.listTransactions(loginUser.getId()));
        return "wallet/index";
    }

    @PostMapping("/recharge")
    public String recharge(@RequestParam BigDecimal amount,
                           HttpSession session, RedirectAttributes ra) {
        User loginUser = (User) session.getAttribute("loginUser");
        try {
            walletService.applyRecharge(loginUser.getId(), amount);
            ra.addFlashAttribute("msg", "充值申请已提交，请等待管理员审核到账");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "充值失败：" + e.getMessage());
        }
        return "redirect:/wallet";
    }
}
