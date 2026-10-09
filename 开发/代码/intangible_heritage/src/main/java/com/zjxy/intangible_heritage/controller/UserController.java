package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.FileUploadUtil;
import com.zjxy.intangible_heritage.service.UserService;
import com.zjxy.intangible_heritage.util.CaptchaUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public String doLogin(@RequestParam String account,
                          @RequestParam String password,
                          HttpSession session,
                          Model model){
        User user = userService.login(account, password);
        if(user == null){
            model.addAttribute("msg","用户名/手机号或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        return "redirect:/";
    }

    // GET：跳转注册页面（与登录页合并，默认显示注册tab）
    @GetMapping("/register")
    public String toRegisterPage(Model model){
        model.addAttribute("showRegister", true);
        return "login";
    }

    // GET：生成验证码图片
    @GetMapping("/captcha")
    public void captcha(HttpSession session, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache");
        String code = CaptchaUtil.generate(response.getOutputStream());
        session.setAttribute("captchaCode", code);
    }

    // POST：注册表单提交【只保留这一个注册post方法，删掉重复的】
    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String phone,
                             @RequestParam String captcha,
                             HttpSession session,
                             Model model){
        // 验证码校验
        Object storedCode = session.getAttribute("captchaCode");
        if (storedCode == null || !storedCode.toString().equalsIgnoreCase(captcha)) {
            model.addAttribute("msg", "验证码错误");
            model.addAttribute("registerError", true);
            return "login";
        }
        // 校验后立即清除，防止重复使用
        session.removeAttribute("captchaCode");

        if (phone == null || !phone.matches("\\d{11}")) {
            model.addAttribute("msg", "手机号必须为11位数字");
            model.addAttribute("registerError", true);
            return "login";
        }
        if (userService.existsByUsername(username)) {
            model.addAttribute("msg", "用户名已存在");
            model.addAttribute("registerError", true);
            return "login";
        }
        if (userService.existsByPhone(phone)) {
            model.addAttribute("msg", "该手机号已被注册");
            model.addAttribute("registerError", true);
            return "login";
        }
        // 注册默认普通用户，不接受前端传入的 role
        User user = userService.register(username, password, phone, "0");
        if(user!=null){
            return "redirect:/login";
        }
        model.addAttribute("msg","注册失败");
        model.addAttribute("registerError", true);
        return "login";
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