package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET：跳转登录页面 浏览器点击登录链接
    @GetMapping("/login")
    public String toLoginPage(){
        return "login";
    }

    // POST：登录表单提交
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model){
        User user = userService.login(username,password);
        if(user == null){
            model.addAttribute("msg","用户名或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        return "redirect:/";
    }

    // GET：跳转注册页面
    @GetMapping("/register")
    public String toRegisterPage(){
        return "register";
    }

    // POST：注册表单提交【只保留这一个注册post方法，删掉重复的】
    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String nickname,
                             @RequestParam(defaultValue = "USER") String role,
                             Model model){
        User user = userService.register(username,password,nickname,role);
        if(user!=null){
            return "redirect:/login";
        }
        model.addAttribute("msg","注册失败");
        return "register";
    }

    //退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }
}