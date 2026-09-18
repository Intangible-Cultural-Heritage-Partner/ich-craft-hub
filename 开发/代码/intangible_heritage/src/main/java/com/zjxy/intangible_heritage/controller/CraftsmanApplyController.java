package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.CraftsmanApply;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.CraftsmanApplyRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/craftsman")
public class CraftsmanApplyController {

    @Autowired
    private CraftsmanApplyRepository craftsmanApplyRepository;

    /**
     * 跳转到申请页面
     */
    @GetMapping("/apply")
    public String applyPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        // 未登录或不是普通用户（role != 0）则跳转登录
        if (loginUser == null || !"0".equals(loginUser.getRole())) {
            return "redirect:/login";
        }
        // 检查是否已有待审核的申请（auditStatus = 0）
        boolean hasPending = craftsmanApplyRepository.existsByUserIdAndAuditStatus(loginUser.getId(), 0);
        model.addAttribute("hasPending", hasPending);
        return "craftsman/apply";
    }

    /**
     * 处理申请提交（包含图片上传）
     */
    @PostMapping("/apply")
    public String submitApply(CraftsmanApply apply,
                              @RequestParam(value = "files", required = false) MultipartFile[] files,
                              HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || !"0".equals(loginUser.getRole())) {
            return "redirect:/login";
        }

        // 防止重复提交
        if (craftsmanApplyRepository.existsByUserIdAndAuditStatus(loginUser.getId(), 0)) {
            model.addAttribute("msg", "您已有待审核的申请，请耐心等待！");
            return "craftsman/apply";
        }

        // ====== 处理图片上传 ======
        List<String> imageUrls = new ArrayList<>();
        // 上传目录：项目根目录下的 src/main/resources/static/uploads/craftsman/
        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/craftsman/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录
        }

        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // 生成唯一文件名，防止重名覆盖
                        String originalFilename = file.getOriginalFilename();
                        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String newFileName = UUID.randomUUID().toString() + suffix;

                        // 保存文件到磁盘
                        File dest = new File(dir, newFileName);
                        file.transferTo(dest);

                        // 存入可直接通过浏览器访问的 URL 路径
                        imageUrls.add("/uploads/craftsman/" + newFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        model.addAttribute("msg", "图片上传失败：" + e.getMessage());
                        return "craftsman/apply";
                    }
                }
            }
        }

        // 将图片路径用逗号拼接后存入实体
        if (!imageUrls.isEmpty()) {
            apply.setProofImages(String.join(",", imageUrls));
        }

        // 设置申请人信息与初始状态
        apply.setUserId(loginUser.getId());
        apply.setAuditStatus(0); // 待审核

        craftsmanApplyRepository.save(apply);

        model.addAttribute("msg", "申请提交成功，请等待管理员审核！");
        return "craftsman/apply";
    }
}