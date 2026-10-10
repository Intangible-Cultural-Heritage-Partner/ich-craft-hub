package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.WalletTransaction;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员充值审核：
 *   GET  /admin/recharge/list         待审核充值列表
 *   POST /admin/recharge/audit/{id}   通过/驳回（approved=1/0）
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/recharge")
public class AdminRechargeController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    @GetMapping("/list")
    public String list(Model model) {
        List<WalletTransaction> pendings = walletService.pendingRecharges();
        Map<Long, String> userNames = new HashMap<>();
        for (WalletTransaction t : pendings) {
            userNames.computeIfAbsent(t.getUserId(),
                    id -> userRepository.findById(id).map(User::getUsername).orElse("未知用户"));
        }
        model.addAttribute("pendings", pendings);
        model.addAttribute("userNames", userNames);
        return "admin/recharge/list";
    }

    @PostMapping("/audit/{id}")
    public String audit(@PathVariable Long id,
                        @RequestParam Integer approved,
                        @RequestParam(required = false) String rejectReason,
                        HttpSession session, RedirectAttributes ra) {
        User admin = (User) session.getAttribute("loginUser");
        try {
            walletService.auditRecharge(id, admin.getId(), approved == 1, rejectReason);
            ra.addFlashAttribute("msg", approved == 1 ? "已通过，余额已到账" : "已驳回该充值申请");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("msg", "审核失败：" + e.getMessage());
        }
        return "redirect:/admin/recharge/list";
    }
}
