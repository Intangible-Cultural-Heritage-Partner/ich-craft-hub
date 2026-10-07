package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.HeritageWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final HeritageWorkService heritageWorkService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/tutorial/list")
    public String tutorialList() {
        return "tutorial/list";
    }

    //定制对接页面：已迁移到 CustomOrderController.applyForm()
    // /custom/apply 由 CustomOrderController 处理（同时列出可选匠人）

    //用户作品分享页
    @GetMapping("/userWork/share")
    public String userWorkShare() {
        return "share";
    }

    @GetMapping("/user/userCenter")
    public String userUserCenter(HttpSession session, Model model) {
        Object value = session.getAttribute("loginUser");
        if (value instanceof User user) {
            model.addAttribute("user", user);
            if (user.getId() != null && isCraftsman(user)) {
                model.addAttribute("myWorks", heritageWorkService.findByCraftsman(user.getId()));
            }
        }
        return "user/userCenter";
    }

    private boolean isCraftsman(User user) {
        return "CRAFTSMAN".equalsIgnoreCase(user.getRole()) || "1".equals(user.getRole());
    }

    // 更新个人信息
    @PostMapping("/user/updateProfile")
    public String updateProfile(@RequestParam(required = false) MultipartFile avatarFile,
                                @RequestParam String username,
                                @RequestParam String phone,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) String intro,
                                HttpSession session,
                                Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        User user = userRepository.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // 手机号格式校验
        if (phone == null || !phone.matches("\\d{11}")) {
            model.addAttribute("profileMsg", "手机号必须为11位数字");
            return "redirect:/user/userCenter";
        }
        // 用户名唯一性校验
        if (userRepository.existsByUsername(username) && !username.equals(user.getUsername())) {
            model.addAttribute("profileMsg", "用户名已被使用");
            return "redirect:/user/userCenter";
        }
        // 手机号唯一性校验
        if (userRepository.existsByPhone(phone) && !phone.equals(user.getPhone())) {
            model.addAttribute("profileMsg", "该手机号已被注册");
            return "redirect:/user/userCenter";
        }

        user.setUsername(username);
        user.setPhone(phone);
        if (intro != null) {
            user.setIntro(intro.trim().isEmpty() ? null : intro.trim());
        }
        // 只有填写了新密码才更新
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }

        // 头像上传
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "avatar");
                Files.createDirectories(uploadDir);
                String originalName = avatarFile.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.lastIndexOf('.') >= 0) {
                    ext = originalName.substring(originalName.lastIndexOf('.'));
                }
                String fileName = UUID.randomUUID() + ext;
                Files.copy(avatarFile.getInputStream(), uploadDir.resolve(fileName));
                user.setAvatar("/uploads/avatar/" + fileName);
            } catch (IOException e) {
                model.addAttribute("profileMsg", "头像上传失败");
                return "redirect:/user/userCenter";
            }
        }

        userRepository.save(user);
        session.setAttribute("loginUser", user); // 刷新 session
        model.addAttribute("profileMsg", "修改成功");
        return "redirect:/user/userCenter";
    }
}
