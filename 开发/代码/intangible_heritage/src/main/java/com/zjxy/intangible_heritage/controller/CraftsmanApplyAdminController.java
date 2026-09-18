package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.CraftsmanApply;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.CraftsmanApplyRepository;
import com.zjxy.intangible_heritage.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/craftsmanApply")
public class CraftsmanApplyAdminController {

    @Autowired
    private CraftsmanApplyRepository craftsmanApplyRepository;

    @Autowired
    private UserRepository userRepository;

    // 权限检查：必须是管理员
    private boolean isAdmin(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        return loginUser != null && "2".equals(loginUser.getRole());
    }

    /**
     * 展示申请列表
     */
    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<CraftsmanApply> applyList = craftsmanApplyRepository.findAllByOrderByCreateTimeDesc();

        // 准备一个 Map<userId, User> 避免在 Thymeleaf 中频繁查询
        Map<Long, User> userMap = new HashMap<>();
        for (CraftsmanApply apply : applyList) {
            userMap.putIfAbsent(apply.getUserId(), userRepository.findById(apply.getUserId()).orElse(null));
        }

        model.addAttribute("applyList", applyList);
        model.addAttribute("userMap", userMap);
        return "admin/craftsmanApply/list"; // 对应 templates/admin/craftsmanApply/list.html
    }

    /**
     * 处理审核（通过或驳回）
     */
    @PostMapping("/audit")
    public String audit(@RequestParam Long applyId,
                        @RequestParam Integer status, // 1通过，2驳回
                        @RequestParam(required = false) String auditRemark,
                        HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        CraftsmanApply apply = craftsmanApplyRepository.findById(applyId).orElse(null);
        if (apply == null || apply.getAuditStatus() != 0) {
            return "redirect:/admin/craftsmanApply/list"; // 申请不存在或已处理
        }

        apply.setAuditStatus(status);
        if (status == 2) {
            apply.setAuditRemark(auditRemark); // 驳回时保存理由
        }
        craftsmanApplyRepository.save(apply);

        // 如果审核通过，更新用户的角色为匠人(1)
        if (status == 1) {
            User user = userRepository.findById(apply.getUserId()).orElse(null);
            if (user != null) {
                user.setRole("1"); // 0普通 1匠人 2管理员
                user.setIntro(apply.getIntroduce()); // 把申请的简介同步到用户资料
                userRepository.save(user);
            }
        }
        return "redirect:/admin/craftsmanApply/list";
    }
}