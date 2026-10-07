package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired private HeritageWorkRepository heritageWorkRepository;
    @Autowired private TutorialRepository tutorialRepository;
    @Autowired private UserWorkRepository userWorkRepository;
    @Autowired private UserRepository userRepository;

    // 权限检查：必须是管理员(role=2) 或 对应角色
    private boolean isAdmin(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        return loginUser != null && "2".equals(loginUser.getRole());
    }

    /**
     * 展示审核列表
     */
    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        // 查询所有数据（实际开发可以加审核状态过滤，这里为了方便展示全部）
        model.addAttribute("heritageList", heritageWorkRepository.findAllByOrderByCreateTimeDesc());
        model.addAttribute("tutorialList", tutorialRepository.findAllByOrderByCreateTimeDesc());
        model.addAttribute("userWorkList", userWorkRepository.findAllByOrderByCreateTimeDesc());

        return "audit/list"; // 对应 templates/audit/list.html
    }

    /**
     * 统一审核接口
     * @param type 1-展品 2-教程 3-用户作品
     * @param id 记录ID
     * @param status 1-通过 2-驳回
     * @param auditRemark 驳回理由
     */
    @PostMapping("/doAudit")
    public String doAudit(@RequestParam Integer type,
                          @RequestParam Long id,
                          @RequestParam Integer status,
                          @RequestParam(required = false) String auditRemark,
                          HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        if (type == 1) {
            heritageWorkRepository.findById(id).ifPresent(item -> {
                item.setAuditStatus(status);
                if (status == 2) item.setAuditRemark(auditRemark);
                heritageWorkRepository.save(item);
            });
        } else if (type == 2) {
            tutorialRepository.findById(id).ifPresent(item -> {
                item.setAuditStatus(status);
                if (status == 2) item.setAuditRemark(auditRemark);
                tutorialRepository.save(item);
            });
        } else if (type == 3) {
            userWorkRepository.findById(id).ifPresent(item -> {
                item.setAuditStatus(status);
                if (status == 2) item.setAuditRemark(auditRemark);
                userWorkRepository.save(item);
            });
        }
        return "redirect:/audit/list";
    }
}