package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.FileUploadUtil;
import com.zjxy.intangible_heritage.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;    // ← 这一行必须有

    @GetMapping("/login")
    public String toLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        User user = userService.login(username, password);
        if (user == null) {
            model.addAttribute("msg", "用户名或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String toRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String nickname,
                             @RequestParam(defaultValue = "0") String role,
                             Model model) {
        User user = userService.register(username, password, nickname, role);
        if (user != null) {
            return "redirect:/login";
        }
        model.addAttribute("msg", "注册失败：用户名已存在");
        return "register";
    }

    @PostMapping("/user/updateProfile")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam(required = false) String intro,
                                @RequestParam(required = false) MultipartFile avatarFile,
                                HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        String avatarPath = null;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            avatarPath = fileUploadUtil.save(avatarFile, "avatar");
        }

        User updated = userService.update(loginUser.getId(), nickname, intro, avatarPath);
        if (updated != null) {
            session.setAttribute("loginUser", updated);
        }
        return "redirect:/user/userCenter";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}